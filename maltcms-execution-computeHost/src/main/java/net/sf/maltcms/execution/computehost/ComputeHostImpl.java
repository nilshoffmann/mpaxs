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
package net.sf.maltcms.execution.computehost;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.UUID;
import net.sf.maltcms.execution.api.computeHost.IComputeHost;
import net.sf.maltcms.execution.api.computeHost.IRemoteHost;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.job.Progress;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class ComputeHostImpl extends UnicastRemoteObject implements IComputeHost{

    private IRemoteHost host;
    private Settings settings;
    private HashMap<UUID, JobExecutor> jobLocation = new HashMap<UUID, JobExecutor>();

    public ComputeHostImpl() throws RemoteException{

    }

    public ComputeHostImpl(IRemoteHost host, Settings settings) throws RemoteException{
        this.host = host;
        this.settings = settings;
    }
        
    @Override
    public void setHost(UUID authToken, IRemoteHost host) {
        this.host = host;
//        this.settings = Settings.getInstance();
    }     

    @Override
    public void runJob(UUID authToken, IJob job) throws RemoteException{
        JobExecutor executor = new JobExecutor(job, host,settings.getRemoteReference());
        jobLocation.put(job.getId(), executor);
        executor.start();
    }

    @Override
    public void terminateHost(UUID authToken) throws RemoteException {
        System.exit(1);
    }

    @Override
    public void masterServerShuttingDown(UUID authToken) throws RemoteException {
        if(!settings.getSilentMode()){
            System.out.println("MasterServer is shutting down.");
            System.out.println("Terminating ComputeHost.");
        }
        host.shutdown(this);
    }

    @Override
    public boolean stillAlive(UUID authToken) throws RemoteException {
        return true;
    }

    @Override
    public Progress getJobProgress(UUID authToken, UUID jobID) throws RemoteException {
        if(jobLocation.containsKey(jobID)) {
            return jobLocation.get(jobID).getJobProgress();
        } else {
            return null;
        }
    }

    @Override
    public boolean cancelJob(UUID authToken, UUID jobID) throws RemoteException {
        if(jobLocation.containsKey(jobID)) {
            jobLocation.get(jobID).interrupt();
            jobLocation.remove(jobID);
            return true;
        } else {
            return false;
        }
    }

}
