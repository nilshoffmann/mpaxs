/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.test;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.api.ExecutionType;
import net.sf.maltcms.execution.api.Impaxs;
import net.sf.maltcms.execution.masterServer.MasterServer;
import net.sf.maltcms.execution.spi.ComputeServerFactory;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author nilshoffmann
 */
public class ImpaxsExecution {
    
    public final static String eightyHashes = "################################################################################";
    public final static String hash = "#";

    public static void main(String[] args) {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setProperty(ConfigurationKeys.KEY_EXECUTION_MODE, ExecutionType.DRMAA);
        pc.setProperty(ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR, System.getProperty("user.dir") + File.separator + "maltcms-execution-test-1.6-SNAPSHOT.jar");
        pc.setProperty(ConfigurationKeys.KEY_MASTER_SERVER_EXIT_ON_SHUTDOWN, false);
        pc.setProperty(ConfigurationKeys.KEY_MAX_NUMBER_OF_CHOSTS,"5");
        final int maxJobs = 20;
        Executors.newSingleThreadExecutor().submit(new Runnable() {

            @Override
            public void run() {
                printMessage("Running Within VM Execution");
                /*
                 * LOCAL within VM execution
                 */
                LocalHostExecution lhe = new LocalHostExecution(maxJobs);
                List<String> leResults;
                try{
                    leResults = lhe.call();
                } catch (Exception ex) {
                    Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
                }
                
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

    }
    
    public static void printMessage(String message) {
        System.out.println(eightyHashes);
        System.out.println(hash+" "+message);
        System.out.println(eightyHashes);
    }
}
