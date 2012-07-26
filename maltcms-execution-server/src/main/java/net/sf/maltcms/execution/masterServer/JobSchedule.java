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
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.logging.Level;
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.masterServer.logging.EventLogger;
import net.sf.maltcms.execution.masterServer.messages.IComputeHostEventListener;
import net.sf.maltcms.execution.masterServer.messages.Reporter;
import net.sf.maltcms.execution.api.computeHost.IComputeHost;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.job.Status;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class JobSchedule implements Runnable, IComputeHostEventListener {

    private MasterServer master;
    private HostRegister register;
    private Settings settings = Settings.getInstance();
    private Reporter reporter = Reporter.getInstance();
//    private Drmaa drmaa = new Drmaa();
    private IJob current;
    private boolean currentSubmitted = true;

    public JobSchedule(MasterServer master, HostRegister register) {
        this.master = master;
        this.register = register;
    }

    @Override
    public void run() {
        while (true) {
            if (currentSubmitted) {
                current = master.getUndoneJob();
                currentSubmitted = false;
            }
            //System.out.println("Looking whether a new host needs to be started!");
//            System.out.println("Current job: " + current);
            if (current != null && !current.getStatus().equals(Status.CANCELED)) {
//                System.out.println("Status: " + current.getStatus());
                //blocks until new host is available
                Host host = register.getFreeHost();
//                System.out.println("Retrieved host: " + host);

                if (host != null) {
                    IComputeHost remRef = null;
                    String connectionString = "";
                    try {
//                        System.out.println("Trying to lookup host");
                        connectionString = "//" + host.getIP()
                                + ":" + settings.getLocalPort() + "/" + host.getName();
                        remRef = (IComputeHost) Naming.lookup(
                                connectionString);
                        remRef.stillAlive(UUID.fromString(settings.getString(
                                ConfigurationKeys.KEY_AUTH_TOKEN)));
                        SubmitThread submitter = new SubmitThread(current,
                                remRef, master, host);
                        reporter.report(
                                "Submitting job " + current + " for execution on host: " + connectionString);
                        master.jobOnHost(current, host);
                        submitter.start();
                        currentSubmitted = true;
                    } catch (NotBoundException ex) {
                        reporter.report(
                                "Error during job submission! ComputeHost may be down.");
                        master.removeHost(host.getId());
                        EventLogger.getInstance().getLogger().log(Level.SEVERE,
                                null, ex);
                    } catch (MalformedURLException ex) {
                        reporter.report(
                                "Error during job submission! Please check connection details: " + connectionString);
                        master.removeHost(host.getId());
                        EventLogger.getInstance().getLogger().log(Level.SEVERE,
                                null, ex);
                    } catch (RemoteException ex) {
                        reporter.report(
                                "Error during job submission! ComputeHost may be down.");
                        master.removeHost(host.getId());
                        EventLogger.getInstance().getLogger().log(Level.SEVERE,
                                null, ex);
                    }
                }
//                }
            } else {
                currentSubmitted = true;
                try {
                    Thread.sleep(settings.getScheduleWaitingTime() * 1000);
                } catch (InterruptedException ex) {
                    EventLogger.getInstance().getLogger().log(Level.SEVERE, null,
                            ex);
                }
            }
        }
    }

    public void hostAdded(Host host) {
    }

    public void hostRemoved(Host host) {
    }
}
