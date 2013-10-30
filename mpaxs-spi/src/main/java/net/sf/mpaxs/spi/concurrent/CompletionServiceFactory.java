/*
 * Mpaxs, modular parallel execution system.
 * Copyright (C) 2010-2013, The authors of Mpaxs. All rights reserved.
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
 * Factory for the creation of completion services.
 *
 * @author Nils Hoffmann
 * @param <T> the target type
 */
public class CompletionServiceFactory<T extends Serializable> {

	private long timeOut = 5;
	private TimeUnit timeUnit = TimeUnit.SECONDS;
	private boolean blockingWait = false;
	private int maxThreads = 1;

	/**
	 * Get the time out when non-blocking waiting is used.
	 *
	 * @return the time out
	 */
	public long getTimeOut() {
		return timeOut;
	}

	/**
	 * Set the time out when non-blocking waiting is used.
	 *
	 * @param timeOut the time to wait blockingly
	 */
	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * Get the time unit of the time out when non-blocking waiting is used.
	 *
	 * @return the time out time unit
	 */
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	/**
	 * Set the time unit of the time out when non-blocking waiting is used.
	 *
	 * @param timeUnit the time out time unit
	 */
	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	/**
	 * Get, whether the completion service will use blocking or non-blocking wait.
	 *
	 * @return true if blocking wait, false otherwise
	 */
	public boolean isBlockingWait() {
		return blockingWait;
	}

	/**
	 * Set, whether the completion service will use blocking or non-blocking wait.
	 *
	 * @param blockingWait true if blocking wait, false otherwise
	 */
	public void setBlockingWait(boolean blockingWait) {
		this.blockingWait = blockingWait;
	}

	/**
	 * Get the maximum number of threads for local execution.
	 *
	 * @return the maximum number of threads
	 */
	public int getMaxThreads() {
		return maxThreads;
	}

	/**
	 * Set the maximum number of threads for local execution.
	 *
	 * @param maxThreads the maximum number of threads
	 */
	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	/**
	 * Creates a new local completion service.
	 *
	 * @return a new mpaxs completion service with thread pool of size <code>maxThreads</code>.
	 */
	public MpaxsCompletionService<T> newLocalCompletionService() {
		MpaxsCompletionService<T> mcs = new MpaxsCompletionService<T>(Executors.newFixedThreadPool(maxThreads),
			timeOut, timeUnit, blockingWait);
		return mcs;
	}

	/**
	 * Creates a new distributed completion service.
	 *
	 * @return a new mpaxs completion service backed by a {@link MpaxsExecutorService}.
	 */
	public MpaxsCompletionService<T> newDistributedCompletionService() {
		MpaxsCompletionService<T> mcs = new MpaxsCompletionService<T>(new MpaxsExecutorService(),
			timeOut, timeUnit, blockingWait);
		return mcs;
	}

	/**
	 * Creates a resubmission completion service.
	 *
	 * @param ics              the completion service to wrap
	 * @param maxResubmissions the number of resubmissions before a job is assumed to have permanently failed
	 * @return a resubmission completion service
	 */
	public ICompletionService<T> asResubmissionService(MpaxsCompletionService<T> ics, int maxResubmissions) {
		MpaxsResubmissionCompletionService<T> mrcs = new MpaxsResubmissionCompletionService<T>(ics);
		mrcs.setMaxResubmissions(maxResubmissions);
		return mrcs;
	}
}
