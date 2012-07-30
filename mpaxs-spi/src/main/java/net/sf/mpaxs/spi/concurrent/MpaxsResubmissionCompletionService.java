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
package net.sf.mpaxs.spi.concurrent;

import net.sf.mpaxs.api.ICompletionService;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author nilshoffmann
 */
public class MpaxsResubmissionCompletionService<T extends Serializable>
		implements ICompletionService<T> {

	private MpaxsCompletionService<T> mcs = new MpaxsCompletionService<T>();
	private HashMap<Callable<T>, Integer> submissionCounter = null;
	private Collection<Callable<T>> submission = null;
	private Collection<Callable<T>> failedJobs = null;
	private int submitted = 0;
	private int failed = 0;
	private int finished = 0;
	private int maxResubmissions = 3;

	public MpaxsResubmissionCompletionService() {
		super();
		init();
	}

	private void init() {
		submitted = 0;
		failed = 0;
		finished = 0;
		submissionCounter = new HashMap<Callable<T>, Integer>();
		submission = new LinkedHashSet<Callable<T>>();
		failedJobs = new LinkedHashSet<Callable<T>>();
		// mcs.reset();
	}

	/**
	 * @param mcs
	 */
	public MpaxsResubmissionCompletionService(MpaxsCompletionService<T> mcs) {
		this();
		this.mcs = mcs;
	}

	public int getMaxResubmissions() {
		return maxResubmissions;
	}

	public void setMaxResubmissions(int maxResubmissions) {
		this.maxResubmissions = maxResubmissions;
	}

	public int getFailed() {
		return failed;
	}

	public int getFinished() {
		return finished;
	}

	public boolean isSubmissionClosed() {
		return submissionClosed;
	}

	public int getSubmitted() {
		return submitted;
	}

	private boolean submissionClosed = false;

	@Override
	public List<Callable<T>> getFailedTasks() {
		return new ArrayList<Callable<T>>(failedJobs);
	}

	@Override
	public Future<T> submit(Callable<T> c) throws RejectedExecutionException,
			NullPointerException {
		if (submissionClosed) {
			throw new RejectedExecutionException(
					"CompletionService already started execution.");
		}
		submissionCounter.put(c, 1);
		submitted++;
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
		submitted++;
		return mcs.submit(r, t);
	}

	@Override
	public List<T> call() throws Exception {
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
				finished += mcsResult.size();
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

				failed = failedJobs.size();

				if (submitted == (finished + failed)) {
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
					// int maxThreads = mcs.getMaxThreads();

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
