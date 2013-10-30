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
import java.util.UUID;
import net.sf.mpaxs.api.job.IJob;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author Nils Hoffmann
 */
public interface IRemoteHost extends Thread.UncaughtExceptionHandler {

	/**
	 * Disconnects this host from the master server.
	 *
	 * @return true if successul, false otherwise
	 */
	boolean disconnectFromMasterServer();

	/**
	 * Notify the master server of a finished job
	 *
	 * @param job the finished job
	 */
	void sendDoneJob(IJob job);

	/**
	 * Disconnect the compute host before initiating a shutdown.
	 *
	 * @param obj the remote reference
	 */
	void shutdown(Remote obj);

	/**
	 * Catch uncaught exceptions.
	 *
	 * @param t the thread which raised the exception
	 * @param e the throwable (exception)
	 */
	@Override
	void uncaughtException(Thread t, Throwable e);

	/**
	 * Sets the authentication token required to connect to the correct
	 * master server.
	 *
	 * @param authToken the authentication token
	 */
	void setAuthenticationToken(UUID authToken);

	/**
	 * Returns the unique id of this host.
	 *
	 * @return the unique id of this host
	 */
	UUID getHostId();

	/**
	 * Returns the authentication token used for the current session.
	 *
	 * @return the authentication token
	 */
	UUID getAuthenticationToken();

	/**
	 * Starts the compute host.
	 */
	void startComputeHost();

	/**
	 * Sets the configuration of this compute host.
	 *
	 * @param cfg the configuration
	 */
	void configure(Configuration cfg);

}
