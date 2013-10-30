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
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.logging.Level;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.computeHost.IComputeHost;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Status;
import net.sf.mpaxs.spi.server.logging.EventLogger;
import net.sf.mpaxs.spi.server.messages.Reporter;
import net.sf.mpaxs.spi.server.settings.Settings;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class SubmitThread implements Runnable {

	private IJob job;
	private IComputeHost remRef;
	private Host host;
	private MasterServer master;
	private Settings settings = Settings.getInstance();
	private Reporter reporter = Reporter.getInstance();

	/**
	 *
	 * @param job
	 * @param remRef
	 * @param master
	 * @param host
	 */
	public SubmitThread(IJob job, IComputeHost remRef, MasterServer master, Host host) {
		this.job = job;
		this.remRef = remRef;
		this.master = master;
		this.host = host;
	}

	@Override
	public void run() {
		reporter.report("Placing job in running");
		master.getRunningJobs().put(job.getId(), job);
		reporter.report("Updating job status to running");
		job.setStatus(Status.RUNNING);
		reporter.report("Firing jobChanged");
		master.jobChanged(job);
		try {
			reporter.report("Running job on remote host");
			remRef.runJob(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)), job);
		} catch (RemoteException ex) {
			reporter.report("Error during computation of job! Maybe the ComputeHost is down.");
			reporter.report(ex.getLocalizedMessage());
			master.removeHost(host.getId());
			job.errorOccurred();
			master.submitJob(job);
			EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
			/* All errors must be caught! If not, a poorly programmed run method in a job
			 * could crash the whole server!
			 */
		} catch (Exception ex) {
			if (job.getErrorCounter() <= settings.getMaxErrorsPerJob()) {
				reporter.report("Error during computation of job! Maybe run Method is not OK? Trying again.");
				reporter.report(ex.getLocalizedMessage());
				job.errorOccurred();
				master.submitJob(job);
			} else {
				reporter.report("A job has caused more than " + settings.getMaxErrorsPerJob() + " Errors. Job status changed to ERROR");
				File location = new File(job.getConfigurationFile());
				location.renameTo(new File(settings.getErrorDir() + File.separator + location.getName()));
				master.addFailedJob(location.getName());
			}
			EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
		}
		reporter.report("Done!");
	}
}
