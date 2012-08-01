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
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author nilshoffmann
 */
public class ImpaxsExecution {

    public final static String eightyHashes = "################################################################################";
    public final static String hash = "#";

    public static void main(String[] args) {
        String version;
        try {
            version = net.sf.mpaxs.api.Version.getVersion();
            System.out.println("Running mpaxs "+version);
//            File computeHostJarLocation = new File(System.getProperty("user.dir"), "lib/mpaxs-computeHost-" + version + ".jar");
            File computeHostJarLocation = new File(System.getProperty("user.dir"), "mpaxs.jar");
            if (!computeHostJarLocation.exists() || !computeHostJarLocation.isFile()) {
                throw new IOException("Could not locate mpaxs.jar in "+System.getProperty("user.dir"));
            }
            final PropertiesConfiguration pc = new PropertiesConfiguration();
            pc.setProperty(ConfigurationKeys.KEY_EXECUTION_MODE, ExecutionType.DRMAA);
            pc.setProperty(ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR, computeHostJarLocation);
            pc.setProperty(ConfigurationKeys.KEY_MASTER_SERVER_EXIT_ON_SHUTDOWN, false);
            pc.setProperty(ConfigurationKeys.KEY_MAX_NUMBER_OF_CHOSTS, "2");
            final int maxJobs = 10;
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    printMessage("Running Within VM Execution");
                    /*
                     * LOCAL within VM execution
                     */
                    LocalHostExecution lhe = new LocalHostExecution(maxJobs);
                    List<String> leResults;
                    try {
                        leResults = lhe.call();
                    } catch (Exception ex) {
                        Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
                    }

//                    try {
//                        System.out.println("Drmaa Implementation: "+SessionFactory.getFactory().getSession().getDrmaaImplementation());
//                    }catch(Error e) {
//                        System.err.println(e.toString());
//                    }
                    Impaxs impxs = ComputeServerFactory.getComputeServer();

                    printMessage("Running Distributed Host RMI Execution");
                    /*
                     * Grid Engine (DRMAA API) distributed RMI execution
                     */
                    impxs.startMasterServer(pc);
                    DrmaaExecution de = new DrmaaExecution(maxJobs);
                    List<String> deResults;
                    try {
                        deResults = de.call();
                    } catch (Exception ex) {
                        Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    //impxs.stopMasterServer();

//                printMessage("Running Local Host RMI Execution");
//                /*
//                 *  LOCAL RMI-based execution 
//                 */
//                pc.setProperty(ConfigurationKeys.KEY_EXECUTION_MODE, ExecutionType.LOCAL);
//                impxs.startMasterServer(pc);
//                DrmaaExecution de3 = new DrmaaExecution();
//                List<String> de3Results;
//                try {
//                    de3Results = de3.call();
//                } catch (Exception ex) {
//                    Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                impxs.stopMasterServer();
                    impxs.stopMasterServer();
                    System.exit(0);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void printMessage(String message) {
        System.out.println(eightyHashes);
        System.out.println(hash + " " + message);
        System.out.println(eightyHashes);
    }
}
