/*
 * Mpaxs, modular parallel execution system. 
 * Copyright (C) 2010-2012, The authors of Mpaxs. All rights reserved.
 *
 * Project Administrator: nilshoffmann A T users.sourceforge.net
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
 * under licenses/ for details.
 */
package net.sf.mpaxs.spi.computehost;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.UUID;
import net.sf.mpaxs.api.computeHost.IComputeHost;
import net.sf.mpaxs.api.computeHost.IRemoteHost;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Progress;

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
