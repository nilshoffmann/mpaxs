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

import net.sf.maltcms.execution.masterServer.settings.Settings;
import java.io.File;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.logging.Level;
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.masterServer.logging.EventLogger;
import net.sf.maltcms.execution.masterServer.messages.Reporter;
import net.sf.maltcms.execution.api.computeHost.IComputeHost;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.job.Status;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class SubmitThread extends Thread {

    private IJob job;
    private IComputeHost remRef;
    private Host host;
    private MasterServer master;
    private Settings settings = Settings.getInstance();
    private Reporter reporter = Reporter.getInstance();

    public SubmitThread(IJob job, IComputeHost remRef, MasterServer master, Host host) {
        this.job = job;
        this.remRef = remRef;
        this.master = master;
        this.host = host;
    }

    @Override
    public void run() {
        master.getRunningJobs().put(job.getId(),job);
        job.setStatus(Status.RUNNING);
        master.jobChanged(job);
        try {
            remRef.runJob(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)),job);
        } catch (RemoteException ex) {
            reporter.report("Error during computation of job! Maybe the ComputeHost is down.");
            reporter.report(ex.getLocalizedMessage());
            master.removeHost(host.getId());
            job.errorOccurred();
            master.submitJob(job);
            EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
       /* All errors must be caught! If not, a poorly programmed run method in a job
        * could crash the whole server!
        */
        } catch (Exception ex) {
            if (job.getErrorCounter() <= settings.getMaxErrorsPerJob()) {
                reporter.report("Error during computation of job! Maybe run Method is not OK? Trying again.");
                reporter.report(ex.getLocalizedMessage());
                job.errorOccurred();
                master.submitJob(job);
            } else {
                reporter.report("A job has caused more than " + settings.getMaxErrorsPerJob() + " Errors. Job status changed to ERROR");
                File location = new File(job.getConfigurationFile());
                location.renameTo(new File(settings.getErrorDir() + File.separator + location.getName()));
                master.addFailedJob(location.getName());
            }
            EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
        }
    }
}
