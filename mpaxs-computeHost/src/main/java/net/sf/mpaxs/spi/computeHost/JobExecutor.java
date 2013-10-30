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
package net.sf.mpaxs.spi.computeHost;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.computeHost.IRemoteHost;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Progress;
import net.sf.mpaxs.api.server.IRemoteServer;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class JobExecutor extends Thread implements Thread.UncaughtExceptionHandler {

	private final IRemoteServer server;
	private final IJob job;
	private final IRemoteHost host;
	private final Map<UUID, JobExecutor> jobLocation;
	private ExecutorService executor = null;
	private boolean jobFailed = false;

	/**
	 *
	 * @param job
	 * @param host
	 * @param server
	 * @param jobLocation
	 */
	public JobExecutor(IJob job, IRemoteHost host, IRemoteServer server, Map<UUID, JobExecutor> jobLocation) {
		this.job = job;
		this.host = host;
		this.server = server;
		this.jobLocation = jobLocation;
	}

	@Override
	public void run() {
		try {
			Logger.getLogger(JobExecutor.class.getName()).log(Level.INFO, "Running job {0}", job);
			jobLocation.put(job.getId(), this);
			executor = Executors.newSingleThreadExecutor(new ExceptionSafeThreadFactory(this));
			Future<?> f = executor.submit(job.getClassToExecute());
			try {
				Object o = f.get();
			} catch (ExecutionException ex) {
				job.setThrowable(ex);
			}
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.MICROSECONDS);
			if (!this.isInterrupted() && !jobFailed) {
				server.addDoneJob(host.getAuthenticationToken(), job);
			}
		} catch (InterruptedException ex) {
			job.setThrowable(ex);
			//Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
		} catch (RemoteException ex) {
			Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
			job.setThrowable(ex);
		} finally {
			jobLocation.remove(job.getId());
		}
	}

	private void returnFailedJob() {
		if (!this.isInterrupted()) {
			try {
				server.addFailedJob(host.getAuthenticationToken(), job);
			} catch (RemoteException ex) {
				Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void interrupt() {
		super.interrupt();
		if (executor != null) {
			executor.shutdownNow();
		}
	}

	/**
	 *
	 * @return
	 */
	public Progress getJobProgress() {
		return job.getClassToExecute().getProgress();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		jobFailed = true;
		job.setThrowable(e);
		returnFailedJob();
		interrupt();
	}
}
