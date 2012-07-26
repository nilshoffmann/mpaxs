/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.spi;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.Data;

/**
 *
 * @author nilshoffmann
 */
@Data
public class CompletionServiceFactory<T extends Serializable> {
    
    private long timeOut = 5;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private boolean blockingWait = false;
    private int maxThreads = 1;
    
    public MaltcmsCompletionService<T> createVMLocalCompletionService() {
        MaltcmsCompletionService<T> mcs = new MaltcmsCompletionService<T>(Executors.newFixedThreadPool(maxThreads),
                timeOut, timeUnit, blockingWait);
        return mcs;
    }
    
    public MaltcmsCompletionService<T> createMpaxsCompletionService() {
        MaltcmsCompletionService<T> mcs = new MaltcmsCompletionService<T>(new MpaxsExecutorService(),
                timeOut, timeUnit, blockingWait);
        return mcs;
    }
    
    public MaltcmsResubmissionCompletionService<T> asResubmissionService(MaltcmsCompletionService<T> ics, int maxResubmissions) {
    	return new MaltcmsResubmissionCompletionService<T>(ics);
    }
}
