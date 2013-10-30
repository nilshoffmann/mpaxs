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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.computeHost.IComputeHost;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Status;
import net.sf.mpaxs.spi.server.logging.EventLogger;
import net.sf.mpaxs.spi.server.messages.IComputeHostEventListener;
import net.sf.mpaxs.spi.server.messages.Reporter;
import net.sf.mpaxs.spi.server.settings.Settings;

/**
 * Retrieves pending jobs from the {@link MasterServer}, tries to find a free
 * host to run the job on and submits it for remote execution.
 *
 * @author Kai Bernd Stadermann
 */
public class JobScheduler implements Runnable, IComputeHostEventListener {

	private final MasterServer master;
	private final HostRegister register;
	private final Settings settings = Settings.getInstance();
	private final Reporter reporter = Reporter.getInstance();
	private IJob current;
	private final ExecutorService submissionService = Executors.newSingleThreadExecutor();

	/**
	 * Create a new JobScheduler.
	 *
	 * @param master   the master server to use
	 * @param register the host register to use
	 */
	public JobScheduler(MasterServer master, HostRegister register) {
		this.master = master;
		this.register = register;
	}

	/**
	 * Initiate an orderly shutdown of the job submission service.
	 *
	 * @param timeout  the maximum time to wait for shutdown
	 * @param timeUnit the time unit of the timeout
	 * @throws InterruptedException
	 */
	public void shutdown(long timeout, TimeUnit timeUnit) throws InterruptedException {
		submissionService.shutdown();
		try {
			if (!submissionService.awaitTermination(timeout, timeUnit)) {
				submissionService.shutdownNow();
			}
		} catch (InterruptedException ie) {
			submissionService.shutdownNow();
			Thread.currentThread().interrupt();
			throw ie;
		}
	}

	@Override
	public void run() {
		if (current == null) {
			MyConcurrentLinkedJobQueue queue = master.getPendingJobs();
			if (!queue.isEmpty() && Logger.getLogger(JobScheduler.class.getName()).isLoggable(Level.FINE)) {
				Logger.getLogger(JobScheduler.class.getName()).log(Level.FINE, "Pending jobs: {0}", queue);
			}
			current = master.getPendingJob();
		}
		if (current != null && !current.getStatus().equals(Status.CANCELED)) {
			Host host = register.getFreeHost();
			if (host != null) {
				IComputeHost remRef = null;
				String connectionString = "";
				try {
					connectionString = "//" + host.getIP()
						+ ":" + settings.getLocalPort() + "/" + host.getName();
					remRef = (IComputeHost) Naming.lookup(
						connectionString);
					remRef.stillAlive(UUID.fromString(settings.getString(
						ConfigurationKeys.KEY_AUTH_TOKEN)));
					master.jobOnHost(current, host);
					SubmitThread submitter = new SubmitThread(current,
						remRef, master, host);
					submissionService.submit(submitter);
					current = null;
				} catch (NotBoundException ex) {
					reporter.report(
						"Error during job submission! ComputeHost may be down.");
					master.removeHost(host.getId());
					EventLogger.getInstance().getLogger().log(Level.SEVERE,
						null, ex);
				} catch (MalformedURLException ex) {
					reporter.report(
						"Error during job submission! Please check connection details: " + connectionString);
					master.removeHost(host.getId());
					EventLogger.getInstance().getLogger().log(Level.SEVERE,
						null, ex);
				} catch (RemoteException ex) {
					reporter.report(
						"Error during job submission! ComputeHost may be down.");
					master.removeHost(host.getId());
					EventLogger.getInstance().getLogger().log(Level.SEVERE,
						null, ex);
				}
			}
		}
	}

	/**
	 *
	 * @param host
	 */
	@Override
	public void hostAdded(Host host) {
//		freeHosts.add(host);
	}

	/**
	 *
	 * @param host
	 */
	@Override
	public void hostRemoved(Host host) {
	}

	/**
	 *
	 * @param host
	 */
	@Override
	public void hostFree(Host host) {
//		freeHosts.add(host);
	}
}
