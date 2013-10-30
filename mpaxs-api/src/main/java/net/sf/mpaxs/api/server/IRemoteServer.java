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
package net.sf.mpaxs.api.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;
import net.sf.mpaxs.api.job.IJob;

/**
 * Interface for the remote server access, as exposed via RMI.
 *
 * @author Kai Bernd Stadermann
 */
public interface IRemoteServer extends Remote {

	/**
	 * Add a host with the given parameters.
	 *
	 * @param authToken the authentication token
	 * @param name      the name of the compute host
	 * @param ip        the ip of the compute host
	 * @param cores     the number of available physical cores on the host
	 * @return the unique id of the new host
	 * @throws RemoteException
	 */
	UUID addHost(UUID authToken, String name, String ip, int cores) throws RemoteException;

	/**
	 * Notify the server a successfully finished job.
	 *
	 * @param authToken the authentication token
	 * @param job       the job that finished successfully
	 * @throws RemoteException
	 */
	void addDoneJob(UUID authToken, IJob job) throws RemoteException;

	/**
	 * Notify the server a failed job.
	 *
	 * @param authToken the authentication token
	 * @param job       the job that failed
	 * @throws RemoteException
	 */
	void addFailedJob(UUID authToken, IJob job) throws RemoteException;

	/**
	 * Remove the host from the server's host register.
	 *
	 * @param authToken the authentication token
	 * @param id        the host id
	 * @return whether remove was successful
	 * @throws RemoteException
	 */
	boolean delHost(UUID authToken, UUID id) throws RemoteException;

	/**
	 * Query the server for its liveness status.
	 *
	 * @param authToken the authentication token
	 * @return true if the server is still alive and serves request or false if it is shutting down
	 * @throws RemoteException
	 */
	boolean stillAlive(UUID authToken) throws RemoteException;

}
