/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.spi;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import net.sf.maltcms.execution.api.ICompletionService;

/**
 *
 * @author nilshoffmann
 */
@Data
public class CompletionServiceFactory<T extends Serializable> {
    
    private long timeOut = 5;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private boolean blockingWait = false;
    
    public ICompletionService<T> createVMLocalCompletionService() {
        MaltcmsCompletionService<T> mcs = new MaltcmsCompletionService<T>(null,
                timeOut, timeUnit, blockingWait);
        return mcs;
    }
    
    public ICompletionService<T> createMpaxsCompletionService() {
        MaltcmsCompletionService<T> mcs = new MaltcmsCompletionService<T>(new MpaxsExecutorService(),
                timeOut, timeUnit, false);
        return mcs;
    }
}
