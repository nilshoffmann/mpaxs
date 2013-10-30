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
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.computeHost.IComputeHost;
import net.sf.mpaxs.api.computeHost.IRemoteHost;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Progress;

/**
 *
 * @author Kai Bernd Stadermann
 */
public final class ComputeHostImpl extends UnicastRemoteObject implements IComputeHost {

	private final ConcurrentHashMap<UUID, JobExecutor> jobLocation = new ConcurrentHashMap<UUID, JobExecutor>();
	private IRemoteHost host;
	private Settings settings;

	/**
	 *
	 * @throws RemoteException
	 */
	public ComputeHostImpl() throws RemoteException {

	}

	/**
	 *
	 * @param host
	 * @param settings
	 * @throws RemoteException
	 */
	public ComputeHostImpl(IRemoteHost host, Settings settings) throws RemoteException {
		this.host = host;
		this.settings = settings;
	}

	/**
	 *
	 * @param remoteAuthToken
	 * @throws RemoteException
	 */
	protected void authenticate(UUID remoteAuthToken) throws RemoteException {
		String authToken = settings.getOption(ConfigurationKeys.KEY_AUTH_TOKEN);
		if (!remoteAuthToken.toString().equals(authToken)) {
			throw new RemoteException("Authentication with token: " + remoteAuthToken + " failed!");
		}
	}

	@Override
	public void setHost(UUID authToken, IRemoteHost host) {
		this.host = host;
	}

	@Override
	public void runJob(UUID authToken, IJob job) throws RemoteException {
		authenticate(authToken);
		JobExecutor executor = new JobExecutor(job, host, settings.getRemoteReference(), jobLocation);
		executor.start();
	}

	@Override
	public void terminateHost(UUID authToken) throws RemoteException {
		authenticate(authToken);
		System.exit(1);
	}

	@Override
	public void masterServerShuttingDown(UUID authToken) throws RemoteException {
		authenticate(authToken);
		if (!settings.getSilentMode()) {
			Logger.getLogger(ComputeHostImpl.class.getName()).log(Level.INFO, "MasterServer is shutting down: terminating compute host {0}", host.getHostId());
		}
		host.shutdown(this);
	}

	@Override
	public boolean stillAlive(UUID authToken) throws RemoteException {
		authenticate(authToken);
		return true;
	}

	@Override
	public Progress getJobProgress(UUID authToken, UUID jobID) throws RemoteException {
		authenticate(authToken);
		if (jobLocation.containsKey(jobID)) {
			return jobLocation.get(jobID).getJobProgress();
		} else {
			return null;
		}
	}

	@Override
	public boolean cancelJob(UUID authToken, UUID jobID) throws RemoteException {
		authenticate(authToken);
		if (jobLocation.containsKey(jobID)) {
			jobLocation.get(jobID).interrupt();
			jobLocation.remove(jobID);
			return true;
		} else {
			return false;
		}
	}

}
