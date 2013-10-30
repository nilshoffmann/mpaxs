/*
 * Mpaxs, modular parallel execution system.
 * Copyright (C) 2010-2013, The authors of Mpaxs. All rights reserved.
 *
 * Project website: http://mpaxs.sf.net
 *
 * Mpaxs may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Mpaxs, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Mpaxs is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package net.sf.mpaxs.spi.server;

import java.awt.Container;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.computeHost.IComputeHost;
import net.sf.mpaxs.api.event.IJobEventListener;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Progress;
import net.sf.mpaxs.api.job.ScheduledJob;
import net.sf.mpaxs.api.job.Status;
import net.sf.mpaxs.api.server.IRemoteServer;
import net.sf.mpaxs.spi.server.consoleInput.Input;
import net.sf.mpaxs.spi.server.dirWatcher.DirWatcher;
import net.sf.mpaxs.spi.server.gui.MainFrame;
import net.sf.mpaxs.spi.server.logging.EventLogger;
import net.sf.mpaxs.spi.server.messages.Reporter;
import net.sf.mpaxs.spi.server.settings.Settings;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class MasterServer implements Thread.UncaughtExceptionHandler {

	private final HostRegister register;
	private final Settings settings;
	private final Reporter reporter;
	private final JobScheduler jobScheduler;
	private final DirWatcher watcher;
	private final ExecutorService jobEventNotifier = Executors.newCachedThreadPool();
	private final HashMap<UUID, IJob> runningJobs = new HashMap<UUID, IJob>();
	private final HashMap<UUID, IJob> doneJobs = new HashMap<UUID, IJob>();
	private final HashMap<UUID, IJob> canceledJobs = new HashMap<UUID, IJob>();
	private final HashMap<UUID, Host> jobRunningOnHost = new HashMap<UUID, Host>();
	private final ArrayList<String> failedJobs = new ArrayList<String>();
	private final MyConcurrentLinkedJobQueue pendingJobs = new MyConcurrentLinkedJobQueue();
//	private final MyConcurrentLinkedJobQueue scheduledJobs = new MyConcurrentLinkedJobQueue();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private final ConcurrentHashMap<UUID, Set<IJobEventListener>> listeners = new ConcurrentHashMap<UUID, Set<IJobEventListener>>();
	private final List<IJobEventListener> generalListeners = new ArrayList<IJobEventListener>();
	private MainFrame main;
	private Input input;
	private Thread inputThread = null;
	private boolean isShutdown = false;
	private boolean exitOnShutdown = false;
	//generate a random id for this master server
	private UUID authToken = UUID.randomUUID();

	/**
	 *
	 * @param c
	 */
	public MasterServer(Container c) {
		settings = Settings.getInstance();
		settings.setOption(ConfigurationKeys.KEY_AUTH_TOKEN, authToken.toString());
		register = new HostRegister();
		reporter = Reporter.getInstance();
		bindHostRegister(authToken);
		jobScheduler = new JobScheduler(this, register);
		register.addListener(jobScheduler);
		watcher = new DirWatcher(this);
		scheduler.scheduleAtFixedRate(watcher, 500, settings.getScheduleWaitingTime(), TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(jobScheduler, 100, 50, TimeUnit.MILLISECONDS);
		if (settings.getGuiMode()) {
			main = new MainFrame(this, c);
			reporter.addListener(main);
			this.addListener(main);
			register.addListener(main);
		} else {
//            input = new Input(this);
//            reporter.addListener(input);
//            this.addListener(input);
//            register.addListener(input);
//            inputThread = new Thread(input);
//            inputThread.start();
		}
		this.exitOnShutdown = Boolean.parseBoolean(settings.getString(ConfigurationKeys.KEY_MASTER_SERVER_EXIT_ON_SHUTDOWN));
	}

	/**
	 * Creates a RMI-Registry and binds the host register on the given Port. This
	 * facilitates registration of compute host.
	 */
	private void bindHostRegister(UUID authToken) {
		try {
			//Creates a RMI-Registry on the given Port.
			createRegistry(settings.getLocalPort());
			// Create Remote object.
			IRemoteServer remObj = new ServerImpl(register, this, authToken);
			EventLogger.getInstance().getLogger().log(Level.FINE, "Binding server at {0}:{1} with name {2}", new Object[]{settings.getLocalIP(), settings.getLocalPort(), settings.getName()});
			// Naming remote object.
			Naming.bind("//" + settings.getLocalIP() + ":" + settings.getLocalPort() + "/" + settings.getName(), remObj);
		} catch (AlreadyBoundException ex) {
			EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
		} catch (MalformedURLException ex) {
			EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
		} catch (RemoteException ex) {
			EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
		}
	}

	private void createRegistry(int LocalPort) throws RemoteException {
		try {
			LocateRegistry.createRegistry(LocalPort);
			settings.setLocalPort(LocalPort);
		} catch (java.rmi.server.ExportException ex) {
			createRegistry(LocalPort + 1);
		}
	}

	/**
	 * Submit a job immediately to the execution queue.
	 *
	 * @param job the job to submit to the queue
	 */
	public void submitJob(IJob job) {
		if (!isShutdown) {
			if (job instanceof ScheduledJob) {
				final ScheduledJob sj = (ScheduledJob) job;
				Runnable submitter = new Runnable() {

					@Override
					public void run() {
						Logger.getLogger(
							MasterServer.class.getName()).log(
								Level.INFO,
								"Placing scheduled job {0} on queue!", sj);
						sj.setThrowable(null);
						sj.setStatus(Status.WAITING);
						pendingJobs.offer(sj);
						jobChanged(sj);
						Logger.getLogger(
							MasterServer.class.getName()).log(
								Level.INFO,
								"Placed scheduled job {0} on queue!", sj);
					}

				};
				scheduler.scheduleAtFixedRate(submitter, sj.getInitialDelay(), sj.getPeriod(), sj.getTimeUnit());
			} else {
				job.setStatus(Status.WAITING);
				pendingJobs.offer(job);
				jobChanged(job);
			}
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	/**
	 * Submit a scheduled job with the specified time until it is first put onto the queue,
	 * with a repetition interval of period. You can either submit plain {@link IJob} instances,
	 * which will then be wrapped in a {@link ScheduledJob} instance, or directly supply
	 * {@link ScheduledJob} instances.
	 *
	 * Note that the period should be larger than the estimated execution time of the
	 * job. Otherwise, the queue will at some point be overloaded with non-finished
	 * instances of {@link ScheduledJob}.
	 *
	 * @param job          the job instance to submit
	 * @param initialDelay the initial delay, before the job is submitted to the queue
	 * @param period       the period between executions of the job
	 * @param timeUnit     the time unit for initialDelay and period
	 */
	public void submitJob(IJob job, long initialDelay, long period, TimeUnit timeUnit) {
		if (!isShutdown) {
			job.setStatus(Status.WAITING);
			if (job instanceof ScheduledJob) {
				final ScheduledJob sj = (ScheduledJob) job;
				Runnable submitter = new Runnable() {

					@Override
					public void run() {
						Logger.getLogger(
							MasterServer.class.getName()).log(
								Level.INFO,
								"Placing scheduled job {0} on queue!", sj);
						sj.setThrowable(null);
						sj.setStatus(Status.WAITING);
						pendingJobs.offer(sj);
						jobChanged(sj);
						Logger.getLogger(
							MasterServer.class.getName()).log(
								Level.INFO,
								"Placed scheduled job {0} on queue!", sj);
					}

				};
				scheduler.scheduleAtFixedRate(submitter, sj.getInitialDelay(), sj.getPeriod(), sj.getTimeUnit());
			} else {
				final ScheduledJob sj = new ScheduledJob(job, initialDelay, period, timeUnit);
				Runnable submitter = new Runnable() {

					@Override
					public void run() {
						Logger.getLogger(
							MasterServer.class.getName()).log(
								Level.INFO,
								"Placing scheduled job {0} on queue!", sj);
						sj.setThrowable(null);
						sj.setStatus(Status.WAITING);
						pendingJobs.offer(sj);
						jobChanged(sj);
						Logger.getLogger(
							MasterServer.class.getName()).log(
								Level.INFO,
								"Placed scheduled job {0} on queue!", sj);
					}

				};
				scheduler.scheduleAtFixedRate(submitter, sj.getInitialDelay(), sj.getPeriod(), sj.getTimeUnit());
			}

		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	/**
	 *
	 * @return
	 */
	public HashMap<UUID, Host> getHosts() {
		return register.getHosts();
	}

	/**
	 *
	 * @return
	 */
	public HashMap<UUID, IJob> getRunningJobs() {
		return runningJobs;
	}

	/**
	 *
	 * @return
	 */
	public HashMap<UUID, IJob> getDoneJobs() {
		return doneJobs;
	}

	/**
	 *
	 * @return
	 */
	public MyConcurrentLinkedJobQueue getPendingJobs() {
		return pendingJobs;
	}

	/**
	 *
	 * @return
	 */
	public HashMap<UUID, IJob> getCanceledJobs() {
		return canceledJobs;
	}

	/**
	 * Initiates an orderly shutdown of all thread pools allocated by the master
	 * server and referenced objects.
	 */
	public synchronized void shutdown() {
		isShutdown = true;
		jobEventNotifier.shutdown();
		scheduler.shutdown();
		try {
			register.shutdown(5, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			Logger.getLogger(MasterServer.class.getName()).log(Level.SEVERE, null, ex);
			Thread.currentThread().interrupt();
		}

		try {
			Logger.getLogger(MasterServer.class.getName()).log(Level.INFO, "Waiting for job scheduler to shut down!");
			jobScheduler.shutdown(5, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			EventLogger.getInstance().getLogger().log(Level.SEVERE, "Interrupted while waiting for job submission termination!", ex);
			Thread.currentThread().interrupt();
		}

		try {
			Logger.getLogger(MasterServer.class.getName()).log(Level.INFO, "Waiting for MasterServer to shut down!");
			scheduler.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			EventLogger.getInstance().getLogger().log(Level.SEVERE, "Interrupted while waiting for scheduler termination!", ex);
			Thread.currentThread().interrupt();
		}

		if (input != null) {
			try {
				input.cancel();
			} catch (InterruptedException ex) {
				EventLogger.getInstance().getLogger().log(Level.SEVERE, "Interrupted while waiting for scheduler termination!", ex);
				Thread.currentThread().interrupt();
			}
		}

		try {
			Logger.getLogger(MasterServer.class.getName()).log(Level.INFO, "Waiting for jobEventNotifier to shut down!");
			jobEventNotifier.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			EventLogger.getInstance().getLogger().log(Level.SEVERE, "Interrupted while waiting for jobEventNotifier termination!", ie);
			Thread.currentThread().interrupt();
		}

		if (exitOnShutdown) {
			EventLogger.getInstance().getLogger().log(Level.INFO, "exitOnShutdown: " + exitOnShutdown);
			System.exit(0);
		}
	}

	/**
	 *
	 * @param jobID
	 * @return
	 */
	public Progress getJobProgress(UUID jobID) {
		if (!isShutdown) {
			Host host = getHostJobIsRunningOn(jobID);
			Progress ret;
			IComputeHost remRef;
			try {

				remRef = (IComputeHost) Naming.lookup("//" + host.getIP()
					+ ":" + settings.getLocalPort() + "/" + host.getName());
				ret = remRef.getJobProgress(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)), jobID);
				/*
				 * All errors must be caught! If not, a poor programmed run
				 * method in a job could crash the whole server!
				 */
			} catch (Exception ex) {
				ret = null;
			}
			if (ret == null) {
				ret = new Progress();
				ret.setProgress(0);
			}
			return ret;
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	/**
	 *
	 * @return
	 */
	public List<String> getFailedJobs() {
		return failedJobs;
	}

	/**
	 *
	 * @param job
	 */
	public void addDoneJob(IJob job) {
		if (!this.isShutdown) {
			Host host = getHostJobIsRunningOn(job.getId());
			File tmp = new File(job.getJobConfigFile());
			tmp.renameTo(new File(settings.getDoneDir() + File.separator + tmp.getName()));
			job.setJobConfigFile(settings.getDoneDir() + File.separator + tmp.getName());
			runningJobs.remove(job.getId());
			doneJobs.put(job.getId(), job);
			job.setStatus(Status.DONE);
			jobRunningOnHost.remove(job.getId());
			jobChanged(job);
			register.releaseHost(host);
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	/**
	 *
	 * @param chunksize
	 * @return
	 */
	public Collection<IJob> getPendingJobsChunk(int chunksize) {
		if (!this.isShutdown) {
			Collection<IJob> ret = pendingJobs.poll(chunksize);
			return ret;
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	/**
	 *
	 * @return
	 */
	public IJob getPendingJob() {
		if (!this.isShutdown) {
			IJob ret = pendingJobs.poll();
			return ret;
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	/**
	 *
	 */
	public void printDoneJobs() {
		for (Iterator<UUID> i = doneJobs.keySet().iterator(); i.hasNext();) {
			try {
				System.out.println(doneJobs.get(i.next()).getClassToExecute().get());
			} catch (InterruptedException ex) {
				Logger.getLogger(MasterServer.class.getName()).log(Level.SEVERE, null, ex);
			} catch (ExecutionException ex) {
				Logger.getLogger(MasterServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Return the host with the given ID or null if there is no host with such
	 * an ID.
	 *
	 * @param hostID UUID of the host
	 * @return host with the given ID
	 */
	public Host getHost(UUID hostID) {
		return register.getHost(hostID);
	}

	/**
	 * Shuts down the host with the given ID.
	 *
	 * @param hostID UUID of the host that should be shutted down.
	 */
	public void shutdownHost(UUID hostID) {
		try {
			Host host = register.getHost(hostID);
			register.removeHost(hostID);
			IComputeHost remRef = null;
			remRef = (IComputeHost) Naming.lookup("//" + host.getIP()
				+ ":" + settings.getLocalPort() + "/" + host.getName());
			remRef.stillAlive(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)));
			remRef.masterServerShuttingDown(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)));
		} catch (NotBoundException ex) {
			reporter.report("An error occurred while shutting down Compute Host " + hostID.toString()
				+ "Seems like the Compute Host is already down");
			EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
		} catch (MalformedURLException ex) {
			reporter.report("An error occurred while shutting down Compute Host " + hostID.toString()
				+ "Seems like the Compute Host is already down");
			EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
		} catch (RemoteException ex) {
			reporter.report("An error occurred while shutting down Compute Host " + hostID.toString()
				+ "Seems like the Compute Host is already down");
			EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
		} catch (NullPointerException ex) {
			reporter.report("An error occurred while shutting down Compute Host " + hostID.toString()
				+ "Seems like the Compute Host is already down");
			EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
		}
	}

	/**
	 *
	 * @param jobId
	 * @return
	 */
	public boolean cancelJob(UUID jobId) {
		if (!this.isShutdown) {
			Host host = getHostJobIsRunningOn(jobId);
			IJob job = findJob(jobId);
			job.setStatus(Status.CANCELED);
			if (host != null) {
				IComputeHost remRef = null;
				try {
					remRef = (IComputeHost) Naming.lookup("//" + host.getIP()
						+ ":" + settings.getLocalPort() + "/" + host.getName());
					remRef.cancelJob(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)), jobId);
					register.releaseHost(host);
					afterCancel(job);
					return true;
				} catch (NotBoundException ex) {
					EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
					return false;
				} catch (MalformedURLException ex) {
					EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
					return false;
				} catch (RemoteException ex) {
					EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
					return false;
				}
			}
			if (pendingJobs.containsJobWithID(jobId)) {
				pendingJobs.remove(jobId);
				afterCancel(job);
				return true;
			}
			if (runningJobs.containsKey(jobId)) {
				runningJobs.remove(jobId);
				afterCancel(job);
				return true;
			}
			return false;
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	/**
	 *
	 * @param job
	 */
	public void jobComputationFailed(IJob job) {
		if (!this.isShutdown) {
			UUID jobId = job.getId();
			runningJobs.remove(job.getId());
			job.setStatus(Status.ERROR);
			Host host = getHostJobIsRunningOn(jobId);
			if (host != null) {
				register.releaseHost(host);
			}
			afterCancel(job);
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	private void afterCancel(IJob job) {
		File location = new File(job.getJobConfigFile());
		File to = new File(settings.getErrorDir() + File.separator + location.getName());
		location.renameTo(to);
		job.setJobConfigFile(to.getAbsolutePath());
		canceledJobs.put(job.getId(), job);
		jobChanged(job);
	}

	/**
	 * Removes the host with the given id from the register.
	 *
	 * @param id Host ID of the Host that should be remove
	 * @return true if remove action was succesfull, false if not
	 */
	public boolean removeHost(final UUID id) {
		if (!this.isShutdown) {
			boolean ret = register.removeHost(id);
			return ret;
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	/**
	 *
	 * @param jobId
	 * @return
	 */
	public IJob findJob(UUID jobId) {
		if (pendingJobs.containsJobWithID(jobId)) {
			return pendingJobs.getJob(jobId);
		}
		if (runningJobs.containsKey(jobId)) {
			return runningJobs.get(jobId);
		}
		if (doneJobs.containsKey(jobId)) {
			return doneJobs.get(jobId);
		}
		if (canceledJobs.containsKey(jobId)) {
			return canceledJobs.get(jobId);
		}
		return null;
	}

	/**
	 *
	 * @param job
	 * @param host
	 */
	public void jobOnHost(IJob job, Host host) {
		if (!isShutdown) {
			jobRunningOnHost.put(job.getId(), host);
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	public Host getHostJobIsRunningOn(UUID jobID) {
		if (!isShutdown) {
			return jobRunningOnHost.get(jobID);
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	/**
	 *
	 * @param name
	 */
	public void addFailedJob(String name) {
		if (!isShutdown) {
			failedJobs.add(name);
			main.updateFailedJobs(name);
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	/**
	 *
	 * @param job
	 */
	public void jobChanged(final IJob job) {
		if (!isShutdown) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					for (IJobEventListener listener : listeners.get(job.getId())) {
						listener.jobChanged(job);
					}
					for (IJobEventListener listener : generalListeners) {
						listener.jobChanged(job);
					}
				}
			};
			jobEventNotifier.submit(r);
		} else {
			throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
		}
	}

	/**
	 *
	 * @param listener
	 * @return
	 */
	public final boolean addListener(IJobEventListener listener) {
		return generalListeners.add(listener);
	}

	/**
	 *
	 * @param listener
	 * @param jobId
	 * @return
	 */
	public final boolean addListener(IJobEventListener listener, UUID jobId) {
		boolean b = false;
		if (listeners.containsKey(jobId)) {
			Set<IJobEventListener> jobEventListeners = listeners.get(jobId);
			b = jobEventListeners.add(listener);
		} else {
			Set<IJobEventListener> jobEventListeners = Collections.newSetFromMap(new ConcurrentHashMap<IJobEventListener, Boolean>());
			b = jobEventListeners.add(listener);
			listeners.put(jobId, jobEventListeners);
		}
		return b;
	}

	/**
	 *
	 * @param listener
	 * @return
	 */
	public boolean removeListener(IJobEventListener listener) {
		return generalListeners.remove(listener);
	}

	/**
	 *
	 * @param listener
	 * @param jobId
	 * @return
	 */
	public boolean removeListener(IJobEventListener listener, UUID jobId) {
		boolean b = false;
		if (listeners.containsKey(jobId)) {
			Set<IJobEventListener> jobEventListeners = listeners.get(jobId);
			b = jobEventListeners.remove(listener);
		}
		return b;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		//reporter.report("Error: " + e.getLocalizedMessage());
		EventLogger.getInstance().getLogger().log(Level.SEVERE, "Uncaught exception!", e);
	}
}
