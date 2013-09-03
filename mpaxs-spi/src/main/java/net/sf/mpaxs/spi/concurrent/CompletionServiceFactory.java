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
package net.sf.mpaxs.spi.concurrent;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.sf.mpaxs.api.ICompletionService;

/**
 *
 * @author Nils Hoffmann
 */
public class CompletionServiceFactory<T extends Serializable> {
    
    private long timeOut = 5;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private boolean blockingWait = false;
    private int maxThreads = 1;

	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public boolean isBlockingWait() {
		return blockingWait;
	}

	public void setBlockingWait(boolean blockingWait) {
		this.blockingWait = blockingWait;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}
	
    /**
     *
     * @return
     */
    public MpaxsCompletionService<T> newLocalCompletionService() {
        MpaxsCompletionService<T> mcs = new MpaxsCompletionService<T>(Executors.newFixedThreadPool(maxThreads),
                timeOut, timeUnit, blockingWait);
        return mcs;
    }
    
    /**
     *
     * @return
     */
    public MpaxsCompletionService<T> newDistributedCompletionService() {
        MpaxsCompletionService<T> mcs = new MpaxsCompletionService<T>(new MpaxsExecutorService(),
                timeOut, timeUnit, blockingWait);
        return mcs;
    }
    
    /**
     *
     * @param ics
     * @param maxResubmissions
     * @return
     */
    public ICompletionService<T> asResubmissionService(MpaxsCompletionService<T> ics, int maxResubmissions) {
    	MpaxsResubmissionCompletionService<T> mrcs = new MpaxsResubmissionCompletionService<T>(ics);
        mrcs.setMaxResubmissions(maxResubmissions);
        return mrcs;
    }
}
