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

import java.io.File;
import java.io.IOException;
import net.sf.maltcms.execution.api.computeHost.IRemoteHost;
import net.sf.maltcms.execution.computehost.consoleInput.Input;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.maltcms.execution.api.server.IRemoteServer;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.api.computeHost.IComputeHost;
import net.sf.maltcms.execution.api.job.IJob;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;

public class Host implements IRemoteHost {

    private Settings settings = null;
    private ScheduledExecutorService scheduler = Executors.
            newScheduledThreadPool(1);
    private UUID authToken = null;

    @Override
    public void configure(Configuration cfg) {
        settings = new Settings(cfg);
        System.out.println("Running ComputeHost at IP "+settings.getLocalIp());
//        if(authToken==null) {
//            authToken = UUID.fromString(settings.getOption(ConfigurationKeys.KEY_AUTH_TOKEN));
//        }
    }

    @Override
    public void sendDoneJob(IJob job) {
        IRemoteServer remRef = settings.getRemoteReference();
        try {
            remRef.addDoneJob(authToken, job);
        } catch (RemoteException ex) {
            Logger.getLogger(Host.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Meldet diesen Host beim Masterserver an. Nach erfolgreicher
     * Anmeldung kann der Masterserver Jobs an diesen Host vergeben.
     */
    private void connectToMasterServer() {
        final ScheduledExecutorService ses = Executors.
                newSingleThreadScheduledExecutor();
        final long startUpAt = System.currentTimeMillis();
        final long failAfter = 5000;
        ses.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                System.out.println("Trying to connect to MasterServer from IP "+settings.getLocalIp());
                long currentTime = System.currentTimeMillis();
                if (currentTime - startUpAt >= failAfter) {
                    System.out.println(
                            "Waited " + (failAfter / 1000) + " seconds for MasterServer, shutting down ComputeHost");
                    System.exit(1);
                }
                try {
                    System.out.println("Trying to bind to MasterServer at " + settings.
                            getMasterServerIP() + ":" + settings.getMasterServerPort()+" with name: "+settings.
                            getMasterServerName());
                    IRemoteServer remRef = (IRemoteServer) Naming.lookup("//" + settings.
                            getMasterServerIP()
                            + ":" + settings.getMasterServerPort() + "/" + settings.
                            getMasterServerName());
                    settings.setRemoteReference(remRef);
                    UUID hostID = remRef.addHost(authToken, settings.getName(),
                            settings.getLocalIp(), settings.getCores());
                    settings.setHostID(hostID);
                    System.out.println("Connection to server established!");
                    ses.shutdown();
                    try {
                        ses.awaitTermination(5, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Host.class.getName()).log(Level.SEVERE,
                                null, ex);
                    }
                } catch (NotBoundException ex) {
                    System.out.println(
                            "Master server not found, waiting for connection!");
                    //Logger.getLogger(Host.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(Host.class.getName()).log(Level.SEVERE,
                            null, ex);
                    System.exit(1);
                } catch (RemoteException ex) {
                    Logger.getLogger(Host.class.getName()).log(Level.SEVERE,
                            null, ex);
                    System.exit(1);
                }
            }
        }, 1, settings.getMasterServerTimeout(), TimeUnit.SECONDS);

    }

    private void MasterServerStillAlive() {
        scheduler.scheduleAtFixedRate(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            settings.getRemoteReference().stillAlive(authToken);

                        } catch (RemoteException ex) {
                            if (!settings.getSilentMode()) {
                                Input.printErr("MasterServer is not responding!");
                                Input.printErr(
                                        "This ComputeHost will shutdown now!");
                            }
                            shutdown(settings.getRemoteReference());
                        }
                    }
                }, settings.getMasterServerTimeout(), settings.
                getMasterServerTimeout(), TimeUnit.SECONDS);
    }

    /**
     * Meldet diesen Host vom Server ab.
     * @return true = erfolgreich abgemeldet, false = Abmeldung fehlgeschlagen
     */
    @Override
    public boolean disconnectFromMasterServer() {
        boolean ret = false;
        try {
            ret = settings.getRemoteReference().delHost(authToken, settings.
                    getHostID());
        } catch (RemoteException ex) {
            Logger.getLogger(Host.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    /**
     * Erstellt ein RemoteObject vom Typ IRemoteHost und trägt es in die lokale
     * RMI Registry ein. Über dieses RemoteObject können Jobs an diesen
     * ComputeHoste gesendet werden.
     */
    private void getReadyForClients() {
        System.out.println("Trying to create registry at "+settings.getLocalIp()+":"+settings.getLocalPort());
        try {
            LocateRegistry.createRegistry(settings.getLocalPort());
            System.out.println("Started own RMI-Registry on port " + settings.
                    getLocalPort());
        } catch (RemoteException ex) {
            if (!settings.getSilentMode()) {
                System.out.println("RMI-Registry already running on port " + settings.
                        getLocalPort() + ".");
                System.out.println(
                        "ComputeHost will use the already running Registry.");
            }
        }
        try {

            // RemoteObject erstellen
            IComputeHost remObj = new ComputeHostImpl(this, settings);
            String bindString = "//" + settings.getLocalIp() + ":" + settings.
                    getLocalPort() + "/" + settings.getName();
            System.out.println("Trying to bind " + bindString);
            Naming.bind(bindString, remObj);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Meldet das RemoteObject ab und schließt danach das Programm.
     * @param obj RemoteObject
     */
    @Override
    public void shutdown(Remote obj) {
        try {
            Naming.unbind(
                    "//" + settings.getLocalIp() + "/" + settings.getName());
            UnicastRemoteObject.unexportObject(obj, true);
        } catch (RemoteException ex) {
            Logger.getLogger(Host.class.getName()).log(Level.FINE, null, ex);
            //System.out.println("one");
        } catch (NotBoundException ex) {
            Logger.getLogger(Host.class.getName()).log(Level.FINE, null, ex);
            //System.out.println("two");
        } catch (MalformedURLException ex) {
            Logger.getLogger(Host.class.getName()).log(Level.FINE, null, ex);
            //System.out.println("three");
        }
        new Thread() {

            @Override
            public void run() {
                try {
                    sleep(settings.getTimeoutBeforeShutdown());
                } catch (InterruptedException ex) {
                    Logger.getLogger(Host.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
                File baseDir = new File(settings.getOption(ConfigurationKeys.KEY_BASE_DIR));
                try {
                    FileUtils.forceDeleteOnExit(baseDir);
                } catch (IOException ex) {
                    Logger.getLogger(Host.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(0);
            }
        }.start();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println("Unknown error: "+e.toString());
        System.exit(1);
    }

    @Override
    public void setAuthenticationToken(UUID authToken) {
        this.authToken = authToken;
        Logger.getLogger(Host.class.getName()).log(Level.INFO,
                "AuthToken for Host is: " + authToken);
    }

    @Override
    public void startComputeHost() {
        getReadyForClients();
        connectToMasterServer();
        MasterServerStillAlive();
        if (!settings.getSilentMode()) {
            new Input(this);
        }
    }

    @Override
    public UUID getAuthenticationToken() {
        return this.authToken;
    }
}
