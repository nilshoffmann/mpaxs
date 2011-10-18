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
package net.sf.maltcms.execution.api.computeHost;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;
import net.sf.maltcms.execution.api.computeHost.IRemoteHost;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.job.Progress;

/**
 *
 * @author Kai Bernd Stadermann
 */
public interface IComputeHost extends Remote{

    void setHost(UUID authToken, IRemoteHost host) throws RemoteException;
    
    void  runJob(UUID authToken, IJob job) throws RemoteException;

    void terminateHost(UUID authToken) throws RemoteException;

    void masterServerShuttingDown(UUID authToken) throws RemoteException;

    boolean stillAlive(UUID authToken) throws RemoteException;

    Progress getJobProgress(UUID authToken, UUID jobID) throws RemoteException;

    boolean cancelJob (UUID authToken, UUID jobID) throws RemoteException;
}
