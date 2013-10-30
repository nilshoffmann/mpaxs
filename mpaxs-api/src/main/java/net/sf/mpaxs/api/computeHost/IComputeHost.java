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
package net.sf.mpaxs.api.computeHost;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Progress;

/**
 * Remote (RMI) interface for interaction with compute hosts.
 *
 * @author Kai Bernd Stadermann
 */
public interface IComputeHost extends Remote {

	/**
	 * Set the remote host reference.
	 *
	 * @param authToken the authentication token
	 * @param host      the remote host
	 * @throws RemoteException
	 */
	void setHost(UUID authToken, IRemoteHost host) throws RemoteException;

	/**
	 * Schedule a job to run on this host.
	 *
	 * @param authToken the authentication token
	 * @param job       the job to schedule
	 * @throws RemoteException
	 */
	void runJob(UUID authToken, IJob job) throws RemoteException;

	/**
	 * Terminate this host.
	 *
	 * @param authToken the authentication token
	 * @throws RemoteException
	 */
	void terminateHost(UUID authToken) throws RemoteException;

	/**
	 * Notify the compute host of master server shutdown.
	 *
	 * @param authToken the authentication token
	 * @throws RemoteException
	 */
	void masterServerShuttingDown(UUID authToken) throws RemoteException;

	/**
	 * Returns whether this host is still alive.
	 *
	 * @param authToken the authentication token
	 * @return true if still alive, false otherwise
	 * @throws RemoteException
	 */
	boolean stillAlive(UUID authToken) throws RemoteException;

	/**
	 * Returns the job progress of the given job.
	 *
	 * @param authToken the authentication token
	 * @param jobID     the job id
	 * @return the job progress
	 * @throws RemoteException
	 */
	Progress getJobProgress(UUID authToken, UUID jobID) throws RemoteException;

	/**
	 * Cancels a given job.
	 *
	 * @param authToken the authentication token
	 * @param jobID     the job id
	 * @return whether the job was cancelled successfully
	 * @throws RemoteException
	 */
	boolean cancelJob(UUID authToken, UUID jobID) throws RemoteException;
}
