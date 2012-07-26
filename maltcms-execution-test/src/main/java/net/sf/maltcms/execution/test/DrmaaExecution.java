/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.maltcms.execution.api.ICompletionService;
import net.sf.maltcms.execution.spi.CompletionServiceFactory;

/**
 *
 * @author hoffmann
 */
public class DrmaaExecution implements Callable<List<String>>{
    
    private final int maxJobs;
    
    public DrmaaExecution(int maxJobs) {
        this.maxJobs = maxJobs;
    }
    
    @Override
    public List<String> call() throws Exception {
        List<String> results = Collections.emptyList();
        CompletionServiceFactory<String> csf = new CompletionServiceFactory<String>();
        csf.setTimeOut(1);
        csf.setTimeUnit(TimeUnit.SECONDS);
        csf.setBlockingWait(false);
        final ICompletionService<String> mcs = csf.createMpaxsCompletionService();
        for(int i = 0; i< maxJobs; i++) {
            mcs.submit(new TestCallable());
        }

        try {
            results = mcs.call();
            System.out.println("MCS1 Results (RMI execution): " + results);
        } catch (Exception ex) {
            Logger.getLogger(DrmaaExecution.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        
        return results;
    }
    
}
