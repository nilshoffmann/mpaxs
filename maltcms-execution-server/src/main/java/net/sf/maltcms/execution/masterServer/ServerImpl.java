/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 */
package net.sf.maltcms.execution.masterServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.server.IRemoteServer;
import net.sf.maltcms.execution.masterServer.settings.Settings;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class ServerImpl extends UnicastRemoteObject implements IRemoteServer {

    HostRegister register;
    MasterServer master;
    private UUID authToken;

    public ServerImpl() throws RemoteException {
    }

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

    protected void authenticate(UUID remoteAuthToken) throws RemoteException {
        if (!remoteAuthToken.equals(authToken)) {
            throw new RemoteException("Authentication with token: " + remoteAuthToken + " failed!");
        }
    }
}
