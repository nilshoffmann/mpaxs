/*
 * Mpaxs, modular parallel execution system. 
 * Copyright (C) 2010-2012, The authors of Mpaxs. All rights reserved.
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

import net.sf.mpaxs.spi.server.settings.Settings;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.logging.Level;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.spi.server.logging.EventLogger;
import net.sf.mpaxs.spi.server.messages.IComputeHostEventListener;
import net.sf.mpaxs.spi.server.messages.Reporter;
import net.sf.mpaxs.api.computeHost.IComputeHost;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Status;

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

    @Override
    public void hostAdded(Host host) {
    }

    @Override
    public void hostRemoved(Host host) {
    }
}
