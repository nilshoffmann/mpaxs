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

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.ExecutionFactory;
import net.sf.mpaxs.api.ExecutionType;
import net.sf.mpaxs.api.computeHost.IComputeHost;
import net.sf.mpaxs.api.server.IComputeHostLauncher;
import net.sf.mpaxs.spi.server.logging.EventLogger;
import net.sf.mpaxs.spi.server.messages.IComputeHostEventListener;
import net.sf.mpaxs.spi.server.messages.Reporter;
import net.sf.mpaxs.spi.server.settings.Settings;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Holds the register of hosts currently registered with the MasterServer.
 *
 * Launches hosts on demand, if the maximum number of hosts has not yet been
 * reached and no other host is currently available for processing.
 *
 * @author Kai Bernd Stadermann
 * @author Nils Hoffmann
 */
public class HostRegister {

	private final Settings settings = Settings.getInstance();
	private final Reporter reporter = Reporter.getInstance();
	private final MyConcurrentLinkedHostQueue hosts = new MyConcurrentLinkedHostQueue();
	private final Map<UUID, Host> usedHosts = new ConcurrentHashMap<UUID, Host>();
	private final ArrayList<IComputeHostEventListener> listeners = new ArrayList<IComputeHostEventListener>();
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final ExecutorService eventService = Executors.newCachedThreadPool();
	private final AtomicInteger hostsLaunched = new AtomicInteger(0);
	private final AtomicInteger hostLaunchRetries = new AtomicInteger(0);
	private final AtomicInteger maxHostLaunchRetries = new AtomicInteger(1);

	/**
	 * Shutdown the host register.
	 *
	 * @param timeout  maximum time to wait before hard shutdown
	 * @param timeUnit time unit for timeout
	 * @throws InterruptedException
	 */
	public void shutdown(long timeout, TimeUnit timeUnit) throws InterruptedException {
		HashMap<UUID, Host> hosts = getHosts();
		try {
			for (Iterator<UUID> i = hosts.keySet().iterator(); i.hasNext();) {
				Host host = hosts.get(i.next());
				IComputeHost remRef;
				try {

					remRef = (IComputeHost) Naming.lookup("//" + host.getIP()
						+ ":" + settings.getLocalPort() + "/" + host.getName());
					remRef.masterServerShuttingDown(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)));

				} catch (NotBoundException ex) {
					EventLogger.getInstance().getLogger().log(Level.SEVERE, "Not Bound Exception!", ex);
				} catch (MalformedURLException ex) {
					EventLogger.getInstance().getLogger().log(Level.SEVERE, "MalformedURLException!", ex);
				} catch (RemoteException ex) {
					EventLogger.getInstance().getLogger().log(Level.SEVERE, "RemoteException!", ex);
				}
			}
		} catch (Exception e) {
			EventLogger.getInstance().getLogger().log(Level.SEVERE, "Exception while shutting down hosts!", e);
			throw new RuntimeException(e);
		} finally {
			//we need to shut down the executor and event service
			//we can not throw the interrupted exceptions however, since
			//that would
			try {
				EventLogger.getInstance().getLogger().log(Level.INFO, "Shutting down host register executor service");
				executorService.shutdown();
				if (!executorService.awaitTermination(timeout, timeUnit)) {
					executorService.shutdownNow();
				}
			} catch (InterruptedException ie) {
				executorService.shutdownNow();
				Thread.currentThread().interrupt();
			} finally {
				try {
					EventLogger.getInstance().getLogger().log(Level.INFO, "Shutting down host register event service");
					eventService.shutdown();
					if (!eventService.awaitTermination(timeout, timeUnit)) {
						eventService.shutdownNow();
					}
				} catch (InterruptedException ie) {
					eventService.shutdownNow();
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	/**
	 * Adds a new Host to the register.
	 *
	 * @param name  Name of the Host
	 * @param ip    IP of the server the Host is running on
	 * @param cores Numer of available cores
	 * @return the uuid of the new host
	 */
	public UUID newHost(final String name, final String ip, final int cores) {
		UUID hostID = UUID.nameUUIDFromBytes((name + ":" + ip).getBytes());
		if (hosts.containsKey(hostID)) {
			return hostID;
		}
		Host tmp = new Host(name, ip, cores, hostID);
		hosts.offer(tmp);
		reporter.report("New Host added with IP " + ip);
		hostAdded(tmp);
		return hostID;
	}

	/**
	 * Removes the host with the given id from the register.
	 *
	 * @param id Host ID of the Host that should be remove
	 * @return true if remove action was succesfull, false if not
	 */
	public boolean removeHost(final UUID id) {
		if (hosts.containsKey(id)) {
			hostRemoved(hosts.get(id));
			hosts.remove(id);
			hostsLaunched.decrementAndGet();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gives back the instance of an host still having a free core.
	 *
	 * @return instance of the free host
	 */
	public Host getFreeHost() {
		if (hostsLaunched.get() < settings.getMaxNumberOfChosts()) {
			launchNewHost();
		}
		Host host = null;
		try {
			host = hosts.poll(30, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			Logger.getLogger(HostRegister.class.getName()).log(Level.SEVERE, null, ex);
		}
		if (host == null) {
			return null;
		}
		host.oneCoreMoreUsed();
		if (host.getFreeCores() == 0 || host.getNumberOfJobs() >= settings.getMaxJobsPerHost()) {
			usedHosts.put(host.getId(), host);
		} else {
			hosts.offer(host);
		}
		return host;
	}

	/**
	 * Prepares the launch of a new host.
	 */
	private void launchNewHost() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				reporter.report("Starting new compute host");
				reporter.report("Maximum allowed number of compute hosts: " + settings.
					getMaxNumberOfChosts());
				reporter.report(
					"Current number of compute hosts: " + getNumberOfHosts());
				if (settings.getMaxNumberOfChosts() > getNumberOfHosts()) {
					ExecutionType et = settings.getExecutionMode();
					reporter.report("Execution mode: " + et);
					IComputeHostLauncher ichl = ExecutionFactory.getComputeHostLaunchers(et).
						get(0);
					reporter.report("Preparing to launch host " + (getNumberOfHosts() + 1) + "/" + settings.getMaxNumberOfChosts());
					String nativeSpec = "";
					if (settings.getOption(ConfigurationKeys.KEY_NATIVE_SPEC) != null) {
						nativeSpec = settings.getString(ConfigurationKeys.KEY_NATIVE_SPEC);
					}
					reporter.report("Setting up host configuration");
					PropertiesConfiguration hostConfiguration = new PropertiesConfiguration();
					UUID authToken = UUID.fromString(settings.getString(
						ConfigurationKeys.KEY_AUTH_TOKEN));
					hostConfiguration.setProperty(
						ConfigurationKeys.KEY_AUTH_TOKEN,
						authToken.toString());
					hostConfiguration.setProperty(
						ConfigurationKeys.KEY_NATIVE_SPEC,
						nativeSpec);
					hostConfiguration.setProperty(
						ConfigurationKeys.KEY_MASTERSERVER_IP,
						settings.getLocalIP());
					hostConfiguration.setProperty(
						ConfigurationKeys.KEY_MASTERSERVER_PORT,
						settings.getLocalPort());
					hostConfiguration.setProperty(
						ConfigurationKeys.KEY_MASTERSERVER_NAME,
						settings.getName());
					hostConfiguration.setProperty(
						ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR,
						settings.getPathToComputeHostJar());
					hostConfiguration.setProperty(ConfigurationKeys.KEY_COMPUTE_HOST_MAIN_CLASS,
						settings.getComputeHostMainClass());
					hostConfiguration.setProperty(
						ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR,
						new File(settings.getComputeHostWorkingDir(), "" + hostsLaunched).getAbsolutePath());
					hostConfiguration.setProperty(
						ConfigurationKeys.KEY_ERROR_FILE,
						hostConfiguration.getString(ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR) + "/error.txt");
					hostConfiguration.setProperty(
						ConfigurationKeys.KEY_OUTPUT_FILE,
						hostConfiguration.getString(ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR) + "/output.txt");
					hostConfiguration.setProperty(
						ConfigurationKeys.KEY_PATH_TO_JAVA,
						settings.getPathToJava());
					hostConfiguration.setProperty(
						ConfigurationKeys.KEY_CODEBASE,
						settings.getCodebase());
					reporter.report(
						"Starting compute host: " + ichl.getClass());
					ichl.startComputeHost(hostConfiguration);
					hostsLaunched.incrementAndGet();
				} else {
					reporter.report("Not launching new compute host: maximum number of active hosts reached (max: " + settings.getMaxNumberOfChosts() + " current: " + getNumberOfHosts() + ")");
				}
			}
		};
		Future<?> future = executorService.submit(r);
		try {
			future.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			Logger.getLogger(HostRegister.class.getName()).
				log(Level.SEVERE, null, ex);
		} catch (ExecutionException ex) {
			Logger.getLogger(HostRegister.class.getName()).
				log(Level.SEVERE, null, ex);
			System.err.println(
				"Error occurred while waiting to create compute host. Setting fallback mode to local execution!");
			settings.setOption(ConfigurationKeys.KEY_EXECUTION_MODE,
				ExecutionType.LOCAL.toString());

			if (hostLaunchRetries.get() < maxHostLaunchRetries.get()) {
				hostLaunchRetries.incrementAndGet();
				return;
			} else {
				throw new RuntimeException(
					"Failed to launch compute host after " + hostLaunchRetries + " tries!",
					ex);
			}
		} catch (TimeoutException ex) {
			Logger.getLogger(HostRegister.class.getName()).
				log(Level.SEVERE, null, ex);
			System.err.println(
				"Timed out while waiting to create compute host. Setting fallback mode to local execution!");
			settings.setOption(ConfigurationKeys.KEY_EXECUTION_MODE,
				ExecutionType.LOCAL.toString());

			if (hostLaunchRetries.get() < maxHostLaunchRetries.get()) {
				hostLaunchRetries.incrementAndGet();
				return;
			} else {
				throw new RuntimeException(
					"Failed to launch compute host after " + hostLaunchRetries + " tries!",
					ex);
			}
		} catch (RuntimeException ex) {
			Logger.getLogger(HostRegister.class.getName()).
				log(Level.SEVERE, null, ex);
			System.err.println(
				"Caught runtime exception while waiting to create compute host. Setting fallback mode to local execution!");
			settings.setOption(ConfigurationKeys.KEY_EXECUTION_MODE,
				ExecutionType.LOCAL.toString());

			if (hostLaunchRetries.get() < maxHostLaunchRetries.get()) {
				hostLaunchRetries.incrementAndGet();
				return;
			} else {
				throw new RuntimeException(
					"Failed to launch compute host after " + hostLaunchRetries + " tries!",
					ex);
			}
		}
	}

	/**
	 * When the host has completed its calculation this method is called to
	 * indicate that its free again.
	 *
	 * @param host Host that should be released
	 */
	public void releaseHost(Host host) {
		if (usedHosts.containsKey(host.getId())) {
			host.oneCoreUnused();
			usedHosts.remove(host.getId());
			hosts.offer(host);
			hostFree(host);
		} else {
			host.oneCoreUnused();
		}
	}

	/**
	 * Return the host with the given ID or null if there is no host with such
	 * an ID.
	 *
	 * @param hostID UUID of the host
	 * @return host with the given ID or null if no such host exists
	 */
	public Host getHost(UUID hostID) {
		return hosts.get(hostID);
	}

	/**
	 * Returns a HashMap containing all hosts currently associated.
	 *
	 * @return the UUID to host map
	 */
	public HashMap<UUID, Host> getHosts() {
		return hosts.getAll();
	}

	private void hostFree(final Host host) {
		for (int i = 0; i < listeners.size(); i++) {
			final int j = i;
			Runnable r = new Runnable() {
				public void run() {
					listeners.get(j).hostFree(host);
				}
			};
			eventService.submit(r);
		}
	}

	private void hostAdded(final Host host) {
		for (int i = 0; i < listeners.size(); i++) {
			final int j = i;
			Runnable r = new Runnable() {
				public void run() {
					listeners.get(j).hostAdded(host);
				}
			};
			eventService.submit(r);
		}
	}

	private void hostRemoved(final Host host) {
		for (int i = 0; i < listeners.size(); i++) {
			final int j = i;
			Runnable r = new Runnable() {
				public void run() {
					listeners.get(j).hostRemoved(host);
				}
			};
			eventService.submit(r);
		}
	}

	/**
	 * Add a compute host event listener.
	 *
	 * @param listener
	 * @return true if addition of the listener was sucessful, false otherwise
	 */
	public boolean addListener(IComputeHostEventListener listener) {
		return listeners.add(listener);
	}

	/**
	 * Remove a compute host event listener.
	 *
	 * @param listener
	 * @return true if the listener was successfully removed, false otherwise
	 */
	public boolean removeListener(IComputeHostEventListener listener) {
		return listeners.remove(listener);
	}

	/**
	 *
	 * @return
	 */
	public int getNumberOfHosts() {
		return Math.max(hosts.size(), hostsLaunched.get());
	}

	/**
	 *
	 * @return
	 */
	public String[] getHostIps() {
		HashMap<UUID, Host> allHosts = hosts.getAll();
		String[] ret = new String[allHosts.size()];
		int j = 0;
		for (UUID key : allHosts.keySet()) {
			ret[j] = allHosts.get(key).getIP();
			j++;
		}
		return ret;
	}
}
