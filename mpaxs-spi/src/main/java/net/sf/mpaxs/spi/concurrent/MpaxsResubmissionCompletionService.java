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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ICompletionService;

/**
 * This class allows to create a completion service that is resilient
 * to random failures of jobs. It can be configured to allow for a maximum
 * number of retries for every failed job.
 *
 * @author Nils Hoffmann
 * @param <T> the type of computed results
 */
public class MpaxsResubmissionCompletionService<T extends Serializable>
	implements ICompletionService<T> {

	private MpaxsCompletionService<T> mcs = new MpaxsCompletionService<T>();
	private HashMap<Callable<T>, Integer> submissionCounter = null;
	private Collection<Callable<T>> submission = null;
	private Collection<Callable<T>> failedJobs = null;
	private AtomicInteger submitted = new AtomicInteger(0);
	private AtomicInteger failed = new AtomicInteger(0);
	private AtomicInteger finished = new AtomicInteger(0);
	private int maxResubmissions = 3;

	/**
	 *
	 */
	public MpaxsResubmissionCompletionService() {
		super();
		init();
	}

	private void init() {
		submitted = new AtomicInteger(0);
		failed = new AtomicInteger(0);
		finished = new AtomicInteger(0);
		submissionCounter = new HashMap<Callable<T>, Integer>();
		submission = new LinkedHashSet<Callable<T>>();
		failedJobs = new LinkedHashSet<Callable<T>>();
	}

	/**
	 * Create a new resubmission service, delegating to the provided
	 * completion service.
	 *
	 * @param mcs the provided completion service
	 */
	public MpaxsResubmissionCompletionService(MpaxsCompletionService<T> mcs) {
		this();
		this.mcs = mcs;
	}

	/**
	 * Get the maximum number of retries for failed jobs.
	 *
	 * @return the maximum number of resubmissions to attempt
	 */
	public int getMaxResubmissions() {
		return maxResubmissions;
	}

	/**
	 * Set the maximum number of retries for failed jobs.
	 *
	 * @param maxResubmissions the maximum number of resubmissions to attempt
	 */
	public void setMaxResubmissions(int maxResubmissions) {
		this.maxResubmissions = maxResubmissions;
	}

	/**
	 * Get the number of jobs that experienced an exception.
	 *
	 * @return the number of failed jobs
	 */
	public int getFailed() {
		return failed.get();
	}

	/**
	 * Get the number of jobs that have finished and have neither been
	 * cancelled nor experienced an exception.
	 *
	 * @return the number of successfully finished jobs
	 */
	public int getFinished() {
		return finished.get();
	}

	/**
	 * Whether more jobs can be submitted.
	 *
	 * @return true if no more jobs can be submitted, false otherwise
	 */
	public boolean isSubmissionClosed() {
		return submissionClosed;
	}

	/**
	 * Get the number of submitted jobs.
	 *
	 * @return the number of submitted jobs
	 */
	public int getSubmitted() {
		return submitted.get();
	}
	private boolean submissionClosed = false;

	@Override
	public List<Callable<T>> getFailedTasks() {
		return new ArrayList<Callable<T>>(failedJobs);
	}

	@Override
	public List<Callable<T>> getCancelledTasks() {
		return mcs.getCancelledTasks();
	}

	@Override
	public List<Callable<T>> getFailedOrCancelledTasks() {
		ArrayList<Callable<T>> al = new ArrayList<Callable<T>>(failedJobs);
		al.addAll(mcs.getCancelledTasks());
		return al;
	}

	@Override
	public Future<T> submit(Callable<T> c) throws RejectedExecutionException,
		NullPointerException {
		if (submissionClosed) {
			throw new RejectedExecutionException(
				"CompletionService already started execution.");
		}
		submissionCounter.put(c, 1);
		submitted.incrementAndGet();
		return mcs.submit(c);
	}

	@Override
	public Future<T> submit(Runnable r, T t) throws RejectedExecutionException,
		NullPointerException {
		if (submissionClosed) {
			throw new RejectedExecutionException(
				"CompletionService already started execution.");
		}
		submissionCounter.put(Executors.callable(r, t), 1);
		submitted.incrementAndGet();
		return mcs.submit(r, t);
	}

	/**
	 * <p>
	 * Returns the list of computed results</p>
	 *
	 * <p>
	 * This method should only be called once to obtain the results.</p>
	 *
	 * @return the list of computed results
	 * @throws Exception
	 * @throws IllegalStateException if call() is invoked more than once
	 */
	@Override
	public List<T> call() throws Exception {
		if (submissionClosed) {
			throw new IllegalStateException("MpaxsCompletionService was already shut down and terminated!");
		}
		submissionClosed = true;
		Logger.getLogger(
			MpaxsResubmissionCompletionService.class
			.getName()).log(
				Level.FINER,
				"Submitted " + submitted + " jobs!");
		boolean allJobsDone = false;
		Set<T> results = new LinkedHashSet<T>();
		while (!allJobsDone) {
			try {
				Collection<T> mcsResult = mcs.call();
				results.addAll(mcsResult);
				finished.addAndGet(mcsResult.size());
				// System.out.println("Results: " + results);
				Collection<Callable<T>> failedTasks = mcs.getFailedTasks();
				// System.out.println("Failed tasks: " + failedTasks.size() +
				// " Results: " + mcsResult.size());
				for (Callable<T> c : failedTasks) {
					if (submissionCounter.get(c) < maxResubmissions) {
						Logger.getLogger(
							MpaxsResubmissionCompletionService.class
							.getName()).log(
								Level.INFO,
								"Resubmitting " + c + " for try "
								+ (submissionCounter.get(c) + 1) + "/"
								+ maxResubmissions);
						submissionCounter.put(c, submissionCounter.get(c) + 1);
						submission.add(c);
					} else {
						failedJobs.add(c);
					}
				}

				failed.getAndSet(failedJobs.size());

				if (submitted.get() == (finished.get() + failed.get())) {
					allJobsDone = true;
					Logger.getLogger(
						MpaxsResubmissionCompletionService.class
						.getName()).log(
							Level.INFO,
							"submitted jobs: " + submitted
							+ " | finished jobs: " + finished
							+ " | total failed jobs (after "
							+ maxResubmissions + " tries): " + failed);
					return new ArrayList<T>(results);
				} else {
					long timeOut = mcs.getTimeToWaitForTasks();
					TimeUnit timeUnit = mcs.getTimeUnitToWaitForTasks();
					boolean blockingWait = mcs.isBlockingWait();
					mcs = new MpaxsCompletionService<T>(null, timeOut,
						timeUnit, blockingWait);
				}
			} catch (Exception ex) {
				Logger.getLogger(
					MpaxsResubmissionCompletionService.class.getName())
					.log(Level.SEVERE, null, ex);
			}
			while (!submission.isEmpty()) {
				if (Thread.interrupted()) {
					return new ArrayList<T>(results);
				}
				Iterator<Callable<T>> iter = submission.iterator();
				Callable<T> tc = iter.next();
				iter.remove();

				mcs.submit(tc);
			}
			if (Thread.interrupted()) {
				return new ArrayList<T>(results);
			}
		}
		return new ArrayList<T>(results);
	}
}
