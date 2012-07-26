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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.maltcms.execution.api.computeHost.IRemoteHost;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.server.IRemoteServer;
import net.sf.maltcms.execution.api.job.Progress;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class JobExecutor extends Thread implements Thread.UncaughtExceptionHandler {

    private IRemoteServer server;
    private IJob job;
    private IRemoteHost host;
    private ExecutorService executor = null;
    private boolean jobFailed = false;

    public JobExecutor(IJob job, IRemoteHost host, IRemoteServer server) {
        this.job = job;
        this.host = host;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            executor = Executors.newSingleThreadExecutor(new ExceptionSafeThreadFactory(this));
            executor.execute(job.getClassToExecute());
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
//            IRemoteServer remRef = settings.getRemoteRefference();
            if (!this.isInterrupted() && !jobFailed) {
                server.addDoneJob(host.getAuthenticationToken(),job);
            }
        } catch (InterruptedException ex) {
            job.setThrowable(ex);
            //Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
            job.setThrowable(ex);
        }
    }

    private void returnFailedJob() {
//        IRemoteServer remRef = settings.getRemoteRefference();
        if (!this.isInterrupted()) {
            try {
                server.addFailedJob(host.getAuthenticationToken(),job);
            } catch (RemoteException ex) {
                Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if(executor != null) {
            executor.shutdownNow();
        }
    }

     public Progress getJobProgress(){
         return job.getClassToExecute().getProgress();
     }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        jobFailed = true;
        job.setThrowable(e);
        returnFailedJob();
        interrupt();
    }
}
