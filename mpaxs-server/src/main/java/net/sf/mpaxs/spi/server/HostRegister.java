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
package net.sf.mpaxs.spi.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.ExecutionFactory;
import net.sf.mpaxs.api.ExecutionType;
import net.sf.mpaxs.api.server.IComputeHostLauncher;
import net.sf.mpaxs.spi.server.messages.IComputeHostEventListener;
import net.sf.mpaxs.spi.server.messages.Reporter;
import net.sf.mpaxs.spi.server.settings.Settings;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Hold the register of hosts currently registert at the MasterServer
 *
 * @author Kai Bernd Stadermann
 * @author Nils Hoffmann
 */
public class HostRegister {

//    private MasterServer master;
//    private int numberOfDRMAAHosts = 0;
    private Settings settings = Settings.getInstance();
    private Reporter reporter = Reporter.getInstance();
    private MyConcurrentLinkedHostQueue hosts = new MyConcurrentLinkedHostQueue();
    private HashMap<UUID, Host> usedHosts = new HashMap<UUID, Host>();
    private ArrayList<IComputeHostEventListener> listeners = new ArrayList<IComputeHostEventListener>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int hostsLaunched = 0;
    private int hostLaunchRetries = 0;
    private int maxHostLaunchRetries = 1;

//    public HostRegister(MasterServer master) {
//        this.master = master;
//    }
    /**
     * Adds a new Host to the register.
     *
     * @param name Name of the Host
     * @param ip IP of the server the Host is running on
     * @param cores Numer of availible cores
     * @return
     */
    public UUID newHost(final String name, final String ip, final int cores) {
        UUID hostID = UUID.randomUUID();
        Host tmp = new Host(name, ip, cores, hostID);
        hosts.offer(tmp);
        reporter.report("New Host added with IP " + ip);
        hostAdded(tmp);
//        if(settings.getExecutionMode()) {
//            numberOfDRMAAHosts++;
//        }
        return hostID;
    }

    /**
     * Removes the host with the given id from the register.
     *
     * @param id Host ID of the Host that should be remove
     * @return true if remove action was succesfull, false if not
     */
    public synchronized boolean removeHost(final UUID id) {
        if (hosts.containsKey(id)) {
            hostRemoved(hosts.get(id));
            hosts.remove(id);
            hostsLaunched--;
//            if (settings.getExecutionMode()) {
//                numberOfDRMAAHosts--;
//            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gives back the instance of an host still having a free core.
     *
     * @return instance of the free host
     */
    public Host getFreeHost() {
        if (hosts.isEmpty() && hostsLaunched == 0) {
            launchNewHost();
        }
        Host host = null;
        host = hosts.poll();
        if (host == null) {
            return null;
        }
        host.oneCoreMoreUsed();
        if (host.getFreeCores() == 0 || host.getNumberOfJobs() >= settings.getMaxJobsPerHost()) {
            usedHosts.put(host.getId(), host);
        } else {
            hosts.offer(host);
        }
        return host;
    }

    protected void launchNewHost() {
        Runnable r = new Runnable() {

            @Override
            public void run() {

//                System.out.println("Starting new host " + ichl.getClass().
//                        getName());

//                System.out.println("Maximum allowed number of chosts: " + settings.
//                        getMaxNumberOfChosts());
//                System.out.println(
//                        "Current number of chosts: " + getNumberOfHosts());
                if (settings.getMaxNumberOfChosts() > getNumberOfHosts()) {
                    ExecutionType et = settings.getExecutionMode();
                    System.out.println("Execution mode: " + et);
                    IComputeHostLauncher ichl = ExecutionFactory.getComputeHostLaunchers(et).
                            get(0);
//                        try {
                    System.out.println("Preparing to launch host " + (getNumberOfHosts() + 1) + "/" + settings.getMaxNumberOfChosts());
                    String nativeSpec = "-q all.q@@qics";//-l \"idle=1\" -q all.q@@qics";
                    if (settings.getOption("nativeSpec") != null) {
                        nativeSpec = settings.getString("nativeSpec");
                    }
                    System.out.println("Setting up host configuration");
                    PropertiesConfiguration hostConfiguration = new PropertiesConfiguration();
                    UUID authToken = UUID.fromString(settings.getString(
                            ConfigurationKeys.KEY_AUTH_TOKEN));
                    hostConfiguration.setProperty(
                            ConfigurationKeys.KEY_AUTH_TOKEN,
                            authToken.toString());
                    hostConfiguration.setProperty(
                            ConfigurationKeys.KEY_NATIVE_SPEC,
                            nativeSpec);
                    hostConfiguration.setProperty(
                            ConfigurationKeys.KEY_MASTERSERVER_IP,
                            settings.getLocalIP());
                    hostConfiguration.setProperty(
                            ConfigurationKeys.KEY_MASTERSERVER_PORT,
                            settings.getLocalPort());
                    hostConfiguration.setProperty(
                            ConfigurationKeys.KEY_MASTERSERVER_NAME,
                            settings.getName());
                    hostConfiguration.setProperty(
                            ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR,
                            settings.getPathToComputeHostJar());
                    hostConfiguration.setProperty(ConfigurationKeys.KEY_COMPUTE_HOST_MAIN_CLASS,
                            settings.getComputeHostMainClass());
                    hostConfiguration.setProperty(
                            ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR,
                            new File(settings.getComputeHostWorkingDir(), "" + hostsLaunched).getAbsolutePath());
                    hostConfiguration.setProperty(
                            ConfigurationKeys.KEY_ERROR_FILE,
                            hostConfiguration.getString(ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR) + "/error.txt");
                    hostConfiguration.setProperty(
                            ConfigurationKeys.KEY_OUTPUT_FILE,
                            hostConfiguration.getString(ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR) + "/output.txt");
                    hostConfiguration.setProperty(
                            ConfigurationKeys.KEY_PATH_TO_JAVA,
                            settings.getPathToJava());
                    hostConfiguration.setProperty(
                            ConfigurationKeys.KEY_CODEBASE,
                            settings.getCodebase());
                    System.out.println(
                            "Starting compute host: " + ichl.getClass());
                    ichl.startComputeHost(hostConfiguration);
                    hostsLaunched++;


//                    } catch (Exception e) {
//                        Logger.getLogger(HostRegister.class.getName()).
//                                log(Level.SEVERE, null, e);
//                    }
                } else {
//                    System.out.println("Not launching new compute host: maximum number of active hosts reached (max: "+settings.getMaxNumberOfChosts()+" current: "+ getNumberOfHosts()+")");
                }
            }
        };
        Future<?> future = executorService.submit(r);
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(HostRegister.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(HostRegister.class.getName()).
                    log(Level.SEVERE, null, ex);
            System.err.println(
                    "Error occurred while waiting to create compute host. Setting fallback mode to local execution!");
            settings.setOption(ConfigurationKeys.KEY_EXECUTION_MODE,
                    ExecutionType.LOCAL.toString());

            if (hostLaunchRetries < maxHostLaunchRetries) {
                hostLaunchRetries++;
                return;
            } else {
                throw new RuntimeException(
                        "Failed to launch compute host after " + hostLaunchRetries + " tries!",
                        ex);
            }
        } catch (TimeoutException ex) {
            Logger.getLogger(HostRegister.class.getName()).
                    log(Level.SEVERE, null, ex);
            System.err.println(
                    "Timed out while waiting to create compute host. Setting fallback mode to local execution!");
            settings.setOption(ConfigurationKeys.KEY_EXECUTION_MODE,
                    ExecutionType.LOCAL.toString());

            if (hostLaunchRetries < maxHostLaunchRetries) {
                hostLaunchRetries++;
                return;
            } else {
                throw new RuntimeException(
                        "Failed to launch compute host after " + hostLaunchRetries + " tries!",
                        ex);
            }
        } catch (RuntimeException ex) {
            Logger.getLogger(HostRegister.class.getName()).
                    log(Level.SEVERE, null, ex);
            System.err.println(
                    "Caught runtime exception while waiting to create compute host. Setting fallback mode to local execution!");
            settings.setOption(ConfigurationKeys.KEY_EXECUTION_MODE,
                    ExecutionType.LOCAL.toString());

            if (hostLaunchRetries < maxHostLaunchRetries) {
                hostLaunchRetries++;
                return;
            } else {
                throw new RuntimeException(
                        "Failed to launch compute host after " + hostLaunchRetries + " tries!",
                        ex);
            }
        }
    }

    /**
     * When the host has completed its calculation this method is called to
     * indicate that its free again.
     *
     * @param host Host that should be released
     */
    public synchronized void releaseHost(Host host) {
        if (usedHosts.containsKey(host.getId())) {
            host.oneCoreUnused();
            usedHosts.remove(host.getId());
            hosts.offer(host);
        } else {
            host.oneCoreUnused();
        }
    }

    /**
     * Return the host with the given ID or null if there is no host with such
     * an ID.
     *
     * @param hostID UUID of the host
     * @return host with the given ID
     */
    public Host getHost(UUID hostID) {
        return hosts.get(hostID);
    }

    /**
     * Returns a HashMap containing all hosts currently associated.
     *
     * @return HashMap<Integer, Host>
     */
    public HashMap<UUID, Host> getHosts() {
        return hosts.getAll();
    }

    private void hostAdded(Host host) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).hostAdded(host);
        }
    }

    private void hostRemoved(Host host) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).hostRemoved(host);
        }
    }

    public boolean addListener(IComputeHostEventListener listener) {
        return listeners.add(listener);
    }

    public boolean removeListener(IComputeHostEventListener listener) {
        return listeners.remove(listener);
    }

    public int getNumberOfHosts() {
        return Math.max(hosts.size(), hostsLaunched);
    }

    public Object[] getHostIps() {
        HashMap<UUID, Host> allHosts = hosts.getAll();
        Object[] ret = new Object[allHosts.size()];
        int j = 0;
        for (Iterator<UUID> i = allHosts.keySet().iterator(); i.hasNext();) {
            UUID key = i.next();
            ret[j] = allHosts.get(key).getIP();
            j++;
        }
        return ret;
    }
}