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
package net.sf.mpaxs.api;

import java.awt.Container;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.sf.mpaxs.api.event.IJobEventListener;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Progress;
import org.apache.commons.configuration.Configuration;

/**
 * External API to execute Jobs.
 *
 * @author Kai Bernd Stadermann
 */
public interface Impaxs {

	/**
	 * Starts a new instance of the MasterServer.
	 * This method does the same thing the main method does.
	 */
	public void startMasterServer();

	/**
	 * Starts a new instance of the MasterServer which will use the given
	 * configuration file.
	 *
	 * @param configFile configuration file the MasterServer should use.
	 */
	public void startMasterServer(String configFile);

	/**
	 * Starts a new instance of the MasterServer which will use the given
	 * configuration object.
	 *
	 * @param config configuration the MasterServer should use.
	 */
	public void startMasterServer(Configuration config);

	/**
	 * Starts a new instance of the MasterServer which will use the given
	 * configuration object.
	 *
	 * @param config configuration the MasterServer should use.
	 * @param c      Container to add the MasterServer ui to
	 */
	public void startMasterServer(Configuration config, Container c);

	/**
	 * Starts a new instance of the MasterServer which will use the given
	 * configuration file.
	 *
	 * @param configFile configuration file the MasterServer should use.
	 * @param c          Container to add the MasterServer ui to
	 */
	public void startMasterServer(String configFile, Container c);

	/**
	 * Starts a new instance of the MasterServer which will use the given
	 * configuration file.
	 *
	 * @param c Container to add the MasterServer ui to
	 */
	public void startMasterServer(Container c);

	/**
	 * Stops the current instance of master server immediately.
	 */
	public void stopMasterServer();

	/**
	 * Resubmit a Job, for example if you want to change the priority
	 * of the job.
	 *
	 * @param job the job to resubmit
	 */
	public void resubmitJob(IJob job);

	/**
	 * Submit a new Job.
	 *
	 * @param job of type shared.Job
	 */
	public void submitJob(IJob job);

	/**
	 * Submit a new scheduled Job.
	 *
	 * @param job            of type shared.Job
	 * @param timeUntilStart time to wait before first execution
	 * @param scheduleAt     interval at which to schedule repeated invocations of this job
	 * @param timeUnit       the time unit for timeUntilStart and scheduleAt
	 */
	public void submitScheduledJob(IJob job, long timeUntilStart, long scheduleAt, TimeUnit timeUnit);

	/**
	 * Return the progress of a job currently beeing computed.
	 *
	 * @param jobId UUID of the job you want to get the progress from.
	 * @return Progress Object of type Progress.
	 */
	public Progress getJobProgress(UUID jobId);

	/**
	 * Cancel the job given by jobId
	 *
	 * @param jobId
	 * @return whether the job was cancelled
	 */
	public boolean cancelJob(UUID jobId);

	/**
	 * Adds the specified Listener. The Listener will be informed about
	 * status changes off all Jobs.
	 *
	 * @param listener IJobEventListener that should be added.
	 */
	public void addJobEventListener(IJobEventListener listener);

	/**
	 * Adds the specified Listener for the given job id. The Listener will be informed about
	 * status changes off all Jobs.
	 *
	 * @param listener IJobEventListener that should be added.
	 * @param jobId    the jobId for which to listen
	 */
	public void addJobEventListener(IJobEventListener listener, UUID jobId);

	/**
	 * Removes the specified Listener.
	 *
	 * @param listener IJobEventListener that should be removed.
	 */
	public void removeJobEventListener(IJobEventListener listener);

	/**
	 * Removes the specified Listener for the given jobId.
	 *
	 * @param listener IJobEventListener that should be removed.
	 * @param jobId    the jobId for which to listen
	 */
	public void removeJobEventListener(IJobEventListener listener, UUID jobId);

	/**
	 * Returns the unique authentication token of this server instance.
	 *
	 * @return the authentication token
	 */
	public UUID getAuthenticationToken();

}
