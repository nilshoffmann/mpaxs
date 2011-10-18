/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.api.concurrent.ConfigurableCallable;
import net.sf.maltcms.execution.api.concurrent.ConfigurableRunnable;
import net.sf.maltcms.execution.api.concurrent.DefaultCallable;
import net.sf.maltcms.execution.api.concurrent.DefaultRunnable;
import net.sf.maltcms.execution.api.ExecutionFactory;
import net.sf.maltcms.execution.api.ExecutionType;
import net.sf.maltcms.execution.api.ICompletionService;
import net.sf.maltcms.execution.api.computeHost.IRemoteHost;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.event.IJobEventListener;
import net.sf.maltcms.execution.api.Impaxs;
import net.sf.maltcms.execution.api.Version;
import net.sf.maltcms.execution.api.job.Job;
import net.sf.maltcms.execution.api.job.Status;
import net.sf.maltcms.execution.spi.CompletionServiceFactory;
import net.sf.maltcms.execution.spi.ComputeServerFactory;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author nilshoffmann
 */
public class ImpaxsExecution {//<V> implements IJobEventListener {

//    private Impaxs imp = null;
//    private Set<IJob> finishedJobs = new LinkedHashSet<IJob>();
//    private Set<IJob> submittedJobs = new LinkedHashSet<IJob>();
//    private boolean isShutdown = false;
//    private int numberOfSubmittedJobs = 0;
//    private int numberOfFinishedJobs = 0;

//    public ImpaxsExecution() {
//        final JFrame jf = null;//new JFrame("MasterServer");
//        imp = ExecutionFactory.getDefaultComputeServer();
//        imp.startMasterServer(jf);
//        imp.addJobEventListener(this);
//        System.out.println("AuthToken is: " + imp.getAuthenticationToken());
////        Runnable r = new Runnable() {
////
////            @Override
////            public void run() {
////                jf.setVisible(true);
////            }
////        };
////        SwingUtilities.invokeLater(r);
//    }

    public static void main(String[] args) {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setProperty(ConfigurationKeys.KEY_EXECUTION_MODE, ExecutionType.DRMAA);
        try {
            File libLocation = new File("/vol/maltcms/rmi/computeHost/"+Version.getVersion()+"/computeHost.jar");
            pc.setProperty(ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR, libLocation.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Impaxs impxs = ComputeServerFactory.getComputeServer();
        impxs.startMasterServer(pc);
        CompletionServiceFactory<String> csf = new CompletionServiceFactory<String>();
        csf.setTimeOut(30);
        csf.setTimeUnit(TimeUnit.SECONDS);
        final ICompletionService<String> mcs = csf.createMpaxsCompletionService();

        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());

        try {
            System.out.println("MCS1 Results (RMI execution): " + mcs.call());
        } catch (Exception ex) {
            Logger.getLogger(ImpaxsExecution.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        csf = new CompletionServiceFactory<String>();
        csf.setTimeOut(30);
        csf.setTimeUnit(TimeUnit.SECONDS);
        final ICompletionService<String> mcs2 = csf.createVMLocalCompletionService();
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());

        try {
            System.out.println("MCS2 Results (local execution): " + mcs2.call());
        } catch (Exception ex) {
            Logger.getLogger(ImpaxsExecution.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        impxs.stopMasterServer();
        System.exit(0);
    }

//    public Impaxs getImpaxs() {
//        return imp;
//    }
//
//    public void submitJob(IJob job) {
//        imp.submitJob(job);
//        submittedJobs.add(job);
//        numberOfSubmittedJobs++;
//    }
//
//    @Override
//    public void jobChanged(IJob job) {
//        if (job.getStatus().equals(Status.DONE)) {
//            numberOfFinishedJobs++;
//            try {
//                //            finishedJobs.add(job);
//                //            submittedJobs.remove(job);
//                System.out.println("Result of job " + job.getId() + ": " + job.getClassToExecute().get());
//            } catch (InterruptedException ex) {
//                Logger.getLogger(ImpaxsExecution.class.getName()).log(
//                        Level.SEVERE, null, ex);
//            } catch (ExecutionException ex) {
//                Logger.getLogger(ImpaxsExecution.class.getName()).log(
//                        Level.SEVERE, null, ex);
//            }
//        }
//        if (numberOfSubmittedJobs == numberOfFinishedJobs && numberOfSubmittedJobs > 0) {
//            System.out.println("Finished execution of jobs!");
//            imp.stopMasterServer();
//            System.exit(0);
//        }
//    }
}
