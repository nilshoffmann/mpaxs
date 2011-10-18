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

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import net.sf.maltcms.execution.masterServer.settings.Settings;
import net.sf.maltcms.execution.masterServer.consoleInput.Input;
import net.sf.maltcms.execution.masterServer.dirWatcher.DirWatcher;
import net.sf.maltcms.execution.masterServer.gui.MainFrame;
import java.awt.Container;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.masterServer.logging.EventLogger;
import net.sf.maltcms.execution.api.event.IJobEventListener;
import net.sf.maltcms.execution.masterServer.messages.Reporter;
//import net.sf.maltcms.execution.api.Job;
import net.sf.maltcms.execution.api.server.IRemoteServer;
import net.sf.maltcms.execution.api.computeHost.IComputeHost;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.job.Progress;
import net.sf.maltcms.execution.api.job.Status;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class MasterServer implements Thread.UncaughtExceptionHandler {

    private final HostRegister register;
    private final Settings settings;
    private final Reporter reporter;
    private final JobSchedule jobScheduler;
    private final DirWatcher watcher;
    private HashMap<UUID, IJob> runningJobs = new HashMap<UUID, IJob>();
    private HashMap<UUID, IJob> doneJobs = new HashMap<UUID, IJob>();
    private HashMap<UUID, IJob> canceledJobs = new HashMap<UUID, IJob>();
    private HashMap<UUID, Host> jobRunningOnHost = new HashMap<UUID, Host>();
    private ArrayList<String> failedJobs = new ArrayList<String>();
    private MyConcurrentLinkedJobQueue undoneJobs = new MyConcurrentLinkedJobQueue();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private ArrayList<IJobEventListener> listeners = new ArrayList<IJobEventListener>();
    private MainFrame main;
    private Input input;
    private Thread inputThread = null;
    private boolean isShutdown = false;
    private boolean exitOnShutdown = false;
    //generate a random id for this master server
    private UUID authToken = UUID.randomUUID();

    public MasterServer(Container c) {
        settings = Settings.getInstance();
        settings.setOption(ConfigurationKeys.KEY_AUTH_TOKEN, authToken.toString());
        register = new HostRegister();
        reporter = Reporter.getInstance();
        bindHostRegister(authToken);
        jobScheduler = new JobSchedule(this, register);
        register.addListener(jobScheduler);
        watcher = new DirWatcher(this);
        scheduler.scheduleAtFixedRate(watcher, 1, settings.getScheduleWaitingTime(), TimeUnit.SECONDS);
        scheduler.schedule(jobScheduler, 1, TimeUnit.SECONDS);
        if (c == null) {
            this.exitOnShutdown = true;
        }
        if (settings.getGuiMode()) {
            main = new MainFrame(this, c);
            reporter.addListener(main);
            this.addListener(main);
            register.addListener(main);
        } else {
            input = new Input(this);
            reporter.addListener(input);
            this.addListener(input);
            register.addListener(input);
            inputThread = new Thread(input);
            inputThread.start();
        }
    }

    /**
     * Startet eine RMI-Registry und bindet das HostRegister an diese.
     * In diesem können sich dann ComputeHosts eintragen.
     */
    private void bindHostRegister(UUID authToken) {
        try {
            //Creates a RMI-Registry on the given Port.
            createRegistry(settings.getLocalPort());
            // Create Remote object.
            IRemoteServer remObj = new ServerImpl(register, this, authToken);
            // Naming remote object.
            Naming.bind("//" + settings.getLocalIP() + ":" + settings.getLocalPort() + "/" + settings.getName(), remObj);
        } catch (AlreadyBoundException ex) {
            EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private void createRegistry(int LocalPort) throws RemoteException {
        try {
            LocateRegistry.createRegistry(LocalPort);
            settings.setLocalPort(LocalPort);
        } catch (java.rmi.server.ExportException ex) {
            createRegistry(LocalPort + 1);
        }
    }

    public synchronized void submitJob(IJob job) {
        if (!isShutdown) {
            job.setStatus(Status.WAITING);
            undoneJobs.offer(job);
            jobChanged(job);
        } else {
            throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
        }
    }
    
    public HashMap<UUID, Host> getHosts() {
        return register.getHosts();
    }
    
    public HashMap<UUID, IJob> getRunningJobs() {
        return runningJobs;
    }
    
    public HashMap<UUID, IJob> getDoneJobs() {
        return doneJobs;
    }
    
    public MyConcurrentLinkedJobQueue getPendingJobs() {
        return undoneJobs;
    }
    
    public HashMap<UUID, IJob> getCanceledJobs() {
        return canceledJobs;
    }

    public void shutdown() {
        isShutdown = true;
        scheduler.shutdown();
        HashMap<UUID, Host> hosts = register.getHosts();

        for (Iterator<UUID> i = hosts.keySet().iterator(); i.hasNext();) {
            Host host = hosts.get(i.next());
            IComputeHost remRef;
            try {

                remRef = (IComputeHost) Naming.lookup("//" + host.getIP()
                        + ":" + settings.getLocalPort() + "/" + host.getName());
                remRef.masterServerShuttingDown(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)));

            } catch (NotBoundException ex) {
                EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
            }
        }

//        new Thread() {
//
//            @Override
//            public void run() {
                try {
                    EventLogger.getInstance().getLogger().log(Level.INFO, "Waiting for MasterServer to shut down!");
                    scheduler.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
                }
                EventLogger.getInstance().getLogger().log(Level.INFO, "exitOnShutdown: " + exitOnShutdown);
                if (exitOnShutdown) {
                    System.exit(0);
                }
//            }
//        }.start();
    }

    public Progress getJobProgress(UUID jobID) {
        if (!isShutdown) {
            Host host = getHostJobIsRunningOn(jobID);
            Progress ret;
            IComputeHost remRef;
            try {

                remRef = (IComputeHost) Naming.lookup("//" + host.getIP()
                        + ":" + settings.getLocalPort() + "/" + host.getName());
                ret = remRef.getJobProgress(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)),jobID);
                /* All errors must be caught! If not, a poor programmed run method in a job
                 * could crash the hole server!
                 */
            } catch (Exception ex) {
                ret = null;
            }
            if (ret == null) {
                ret = new Progress();
                ret.setProgress(0);
            }
            return ret;
        } else {
            throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
        }
    }

    public List<String> getFailedJobs() {
        return failedJobs;
    }

    public synchronized void addDoneJob(IJob job) {
        if (!this.isShutdown) {
            Host host = getHostJobIsRunningOn(job.getId());
            File tmp = new File(job.getJobConfigFile());
            tmp.renameTo(new File(settings.getDoneDir() + File.separator + tmp.getName()));
            job.setJobConfigFile(settings.getDoneDir() + File.separator + tmp.getName());
            runningJobs.remove(job.getId());
            doneJobs.put(job.getId(), job);
            job.setStatus(Status.DONE);
            jobRunningOnHost.remove(job.getId());
            jobChanged(job);
            register.releaseHost(host);
        } else {
            throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
        }
    }

    public synchronized IJob getUndoneJob() {
        if (!this.isShutdown) {
            IJob ret = undoneJobs.poll();
            return ret;
        } else {
            throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
        }
    }

    public void printDoneJobs() {
        for (Iterator<UUID> i = doneJobs.keySet().iterator(); i.hasNext();) {
            try {
                System.out.println(doneJobs.get(i.next()).getClassToExecute().get());
            } catch (InterruptedException ex) {
                Logger.getLogger(MasterServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(MasterServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Return the host with the given ID or null if there is no host with such
     * an ID.
     * @param hostID UUID of the host
     * @return host with the given ID
     */
    public Host getHost(UUID hostID) {
        return register.getHost(hostID);
    }

    /**
     * Shuts down the host with the given ID.
     * @param hostID UUID of the host that should be shutted down.
     */
    public void shutdownHost(UUID hostID) {
        try {
            Host host = register.getHost(hostID);
            register.removeHost(hostID);
            IComputeHost remRef = null;
            remRef = (IComputeHost) Naming.lookup("//" + host.getIP()
                    + ":" + settings.getLocalPort() + "/" + host.getName());
            remRef.stillAlive(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)));
            remRef.masterServerShuttingDown(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)));
        } catch (NotBoundException ex) {
            reporter.report("An error occurred while shutting down Compute Host " + hostID.toString()
                    + "Seems like the Compute Host is already down");
            EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            reporter.report("An error occurred while shutting down Compute Host " + hostID.toString()
                    + "Seems like the Compute Host is already down");
            EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            reporter.report("An error occurred while shutting down Compute Host " + hostID.toString()
                    + "Seems like the Compute Host is already down");
            EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            reporter.report("An error occurred while shutting down Compute Host " + hostID.toString()
                    + "Seems like the Compute Host is already down");
            EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public boolean cancelJob(UUID jobId) {
        if (!this.isShutdown) {
            Host host = getHostJobIsRunningOn(jobId);
            IJob job = findJob(jobId);
            job.setStatus(Status.CANCELED);
            if (host != null) {
                IComputeHost remRef = null;
                try {
                    remRef = (IComputeHost) Naming.lookup("//" + host.getIP()
                            + ":" + settings.getLocalPort() + "/" + host.getName());
                    remRef.cancelJob(UUID.fromString(settings.getString(ConfigurationKeys.KEY_AUTH_TOKEN)),jobId);
                    register.releaseHost(host);
                    afterCancel(job);
                    return true;
                } catch (NotBoundException ex) {
                    EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
                    return false;
                } catch (MalformedURLException ex) {
                    EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
                    return false;
                } catch (RemoteException ex) {
                    EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
                    return false;
                }
            }
            if (undoneJobs.containsJobWithID(jobId)) {
                undoneJobs.remove(jobId);
                afterCancel(job);
                return true;
            }
            if (runningJobs.containsKey(jobId)) {
                runningJobs.remove(jobId);
                afterCancel(job);
                return true;
            }
            return false;
        } else {
            throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
        }
    }

    public void jobComputationFailed(IJob job) {
        if (!this.isShutdown) {
            UUID jobId = job.getId();
            runningJobs.remove(job.getId());
            job.setStatus(Status.ERROR);
            Host host = getHostJobIsRunningOn(jobId);
            if (host != null) {
                register.releaseHost(host);
            }
            afterCancel(job);
        } else {
            throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
        }
    }

    private void afterCancel(IJob job) {
        File location = new File(job.getJobConfigFile());
        File to = new File(settings.getErrorDir() + File.separator + location.getName());
        location.renameTo(to);
        job.setJobConfigFile(to.getAbsolutePath());
        canceledJobs.put(job.getId(), job);
        jobChanged(job);
    }

    /**
     * Removes the host with the given id from the register.
     * @param id Host ID of the Host that should be remove
     * @return true if remove action was succesfull, false if not
     */
    public boolean removeHost(final UUID id) {
        if (!this.isShutdown) {
            boolean ret = register.removeHost(id);
            return ret;
        } else {
            throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
        }
    }

    public IJob findJob(UUID jobId) {
        if (undoneJobs.containsJobWithID(jobId)) {
            return undoneJobs.getJob(jobId);
        }
        if (runningJobs.containsKey(jobId)) {
            return runningJobs.get(jobId);
        }
        if (doneJobs.containsKey(jobId)) {
            return doneJobs.get(jobId);
        }
        if (canceledJobs.containsKey(jobId)) {
            return canceledJobs.get(jobId);
        }
        return null;
    }

    public synchronized void jobOnHost(IJob job, Host host) {
        if (!isShutdown) {
            jobRunningOnHost.put(job.getId(), host);
        } else {
            throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
        }
    }

    public Host getHostJobIsRunningOn(UUID jobID) {
        if (!isShutdown) {
            return jobRunningOnHost.get(jobID);
        } else {
            throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
        }
    }

    public void addFailedJob(String name) {
        if (!isShutdown) {
            failedJobs.add(name);
            main.updateFailedJobs(name);
        } else {
            throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
        }
    }

    public void jobChanged(IJob job) {
        if (!isShutdown) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).jobChanged(job);
            }
        } else {
            throw new IllegalStateException("MasterServer instance was already shutdown, can not accept new jobs!");
        }
    }

    public boolean addListener(IJobEventListener listener) {
        return listeners.add(listener);
    }

    public boolean removeListener(IJobEventListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        reporter.report("Error: "+e.getLocalizedMessage());
        EventLogger.getInstance().getLogger().log(Level.SEVERE, null, e);
    }
    
}
