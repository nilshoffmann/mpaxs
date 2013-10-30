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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.server.IRemoteServer;
import net.sf.mpaxs.spi.server.settings.Settings;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class ServerImpl extends UnicastRemoteObject implements IRemoteServer {

    HostRegister register;
    MasterServer master;
    private UUID authToken;

	/**
	 *
	 * @throws RemoteException
	 */
	public ServerImpl() throws RemoteException {
    }

	/**
	 *
	 * @param register
	 * @param master
	 * @param authToken
	 * @throws RemoteException
	 */
	public ServerImpl(HostRegister register, MasterServer master, UUID authToken) throws RemoteException {
        this.register = register;
        this.master = master;
        this.authToken = authToken;
    }

    @Override
    public UUID addHost(UUID authToken, String name, String ip, int cores) throws RemoteException {
        authenticate(authToken);
        return register.newHost(name, ip, cores);
    }

    @Override
    public boolean delHost(UUID authToken, UUID id) throws RemoteException {
        authenticate(authToken);
        return register.removeHost(id);
    }

    @Override
    public boolean stillAlive(UUID authToken) throws RemoteException {
        authenticate(authToken);
        return true;
    }

    @Override
    public void addDoneJob(UUID authToken, IJob job) throws RemoteException {
        authenticate(authToken);
        master.addDoneJob(job);
    }

    @Override
    public void addFailedJob(UUID authToken, IJob job) throws RemoteException {
        authenticate(authToken);
        master.jobComputationFailed(job);
    }

	/**
	 *
	 * @param remoteAuthToken
	 * @throws RemoteException
	 */
	protected void authenticate(UUID remoteAuthToken) throws RemoteException {
        if (!remoteAuthToken.equals(authToken)) {
            throw new RemoteException("Authentication with token: " + remoteAuthToken + " failed!");
        }
    }
}
