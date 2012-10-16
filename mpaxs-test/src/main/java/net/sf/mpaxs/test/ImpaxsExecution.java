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
package net.sf.mpaxs.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.ExecutionType;
import net.sf.mpaxs.api.Impaxs;
import net.sf.mpaxs.spi.concurrent.ComputeServerFactory;
import net.sf.mpaxs.test.DrmaaExecution;
import net.sf.mpaxs.test.DrmaaExecution;
import net.sf.mpaxs.test.LocalHostExecution;
import net.sf.mpaxs.test.LocalHostExecution;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author Nils Hoffmann
 */
public class ImpaxsExecution {

    /**
     *
     */
    public final static String eightyHashes = "################################################################################";
    /**
     *
     */
    public final static String hash = "#";

    public static void main(String[] args) {
        String version;
        try {
            version = net.sf.mpaxs.api.Version.getVersion();
            System.out.println("Running mpaxs "+version);
            File computeHostJarLocation = new File(System.getProperty("user.dir"), "mpaxs.jar");
            if (!computeHostJarLocation.exists() || !computeHostJarLocation.isFile()) {
                throw new IOException("Could not locate mpaxs.jar in "+System.getProperty("user.dir"));
            }
            final PropertiesConfiguration pc = new PropertiesConfiguration();
            //set default execution type
            pc.setProperty(ConfigurationKeys.KEY_EXECUTION_MODE, ExecutionType.DRMAA);
            //set location of compute host jar
            pc.setProperty(ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR, computeHostJarLocation);
            //do not exit to console when master server shuts down
            pc.setProperty(ConfigurationKeys.KEY_MASTER_SERVER_EXIT_ON_SHUTDOWN, false);
            //limit the number of used compute hosts
            pc.setProperty(ConfigurationKeys.KEY_MAX_NUMBER_OF_CHOSTS, Runtime.getRuntime().availableProcessors()-1);
            final int maxJobs = 10;
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    printMessage("Running Within VM Execution");
                    /*
                     * LOCAL within VM execution
                     */
                    WithinVmExecution lhe = new WithinVmExecution(maxJobs);
                    List<String> leResults;
                    try {
                        leResults = lhe.call();
                        Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.INFO, "Results: "+leResults);
                    } catch (Exception ex) {
                        Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
                    } 

                    Impaxs impxs = ComputeServerFactory.getComputeServer();

                    printMessage("Running Distributed Host RMI Execution");
                    /*
                     * Grid Engine (DRMAA API) or local host distributed RMI execution
                     */
                    impxs.startMasterServer(pc);
                    DistributedRmiExecution de = new DistributedRmiExecution(maxJobs);
                    List<String> deResults;
                    try {
                        deResults = de.call();
                        Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.INFO, "Results: "+deResults);
                    } catch (Exception ex) {
                        Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    impxs.stopMasterServer();
                    System.exit(0);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param message
     */
    public static void printMessage(String message) {
        System.out.println(eightyHashes);
        System.out.println(hash + " " + message);
        System.out.println(eightyHashes);
    }
}
