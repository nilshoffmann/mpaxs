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
package net.sf.maltcms.execution.api.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;
import net.sf.maltcms.execution.api.job.IJob;

/**
 *
 * @author Kai Bernd Stadermann
 */
public interface IRemoteServer extends Remote{

    UUID addHost(UUID authToken, String name, String ip, int cores) throws RemoteException;

    void addDoneJob(UUID authToken, IJob job) throws RemoteException;

    void addFailedJob(UUID authToken, IJob job) throws RemoteException;

    boolean delHost(UUID authToken, UUID id) throws RemoteException;

    boolean stillAlive(UUID authToken) throws RemoteException;
    
}
