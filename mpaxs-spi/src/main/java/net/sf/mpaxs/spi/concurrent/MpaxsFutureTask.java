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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Phaser;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.Impaxs;
import net.sf.mpaxs.api.concurrent.DefaultCallable;
import net.sf.mpaxs.api.concurrent.DefaultRunnable;
import net.sf.mpaxs.api.event.IJobEventListener;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Job;
import net.sf.mpaxs.api.job.Status;

/**
 * Implementation of RunnableFuture for mpaxs jobs.
 *
 * @author Nils Hoffmann
 * @param <T> the type of computed results
 * @see java.util.concurrent.FutureTask
 */
public class MpaxsFutureTask<T> extends FutureTask<T> implements
	RunnableFuture<T>, IJobEventListener {

	private final IJob<T> job;
	private final Impaxs computeServer;
	private Phaser phaser;

	/**
	 * Create a new instance.
	 *
	 * @param computeServer the compute server to use for job submission
	 * @param callable      the callable to be executed
	 */
	public MpaxsFutureTask(Impaxs computeServer, Callable<T> callable) {
		super(callable);
		this.computeServer = computeServer;
		job = new Job<T>(new DefaultCallable<T>(callable));
	}

	/**
	 * Create a new instance.
	 *
	 * @param computeServer the compute server to use for job submission
	 * @param runnable      the runnable to be executed
	 * @param result        the result to be returned on successful completion
	 */
	public MpaxsFutureTask(Impaxs computeServer, Runnable runnable, T result) {
		super(runnable, result);
		this.computeServer = computeServer;
		job = new Job<T>(new DefaultRunnable<T>(runnable, result));
	}

	@Override
	public boolean cancel(boolean bln) {
		boolean superCancelled = super.cancel(bln);
		if (job.getStatus() != Status.CANCELED) {
			computeServer.cancelJob(job.getId());
			job.setStatus(Status.CANCELED);
		}
		return superCancelled;
	}

	@Override
	public void run() {
		phaser = new Phaser(1);
		computeServer.addJobEventListener(this, job.getId());
		job.setStatus(Status.UNKNOWN);
		job.setThrowable(null);
		computeServer.submitJob(job);
		phaser.awaitAdvance(0);
	}

	@Override
	public void jobChanged(final IJob job) {
		if (job.getId().equals(this.job.getId())) {
			if (job.getStatus() == Status.DONE) {
				try {
					T v = (T) job.getClassToExecute().get();
					if (v != null) {
						set(v);
					}
				} catch (InterruptedException ex) {
					Logger.getLogger(MpaxsFutureTask.class.getName()).log(Level.SEVERE, null, ex);
				} catch (ExecutionException ex) {
					Logger.getLogger(MpaxsFutureTask.class.getName()).log(Level.SEVERE, null, ex);
					setException(ex);
				}
				computeServer.removeJobEventListener(this, job.getId());
				int phase = phaser.arriveAndDeregister();
			} else if (job.getStatus() == Status.CANCELED) {
				cancel(true);
				computeServer.removeJobEventListener(this, job.getId());
				int phase = phaser.arriveAndDeregister();
			} else if (job.getStatus() == Status.ERROR) {
				setException(job.getThrowable());
				computeServer.removeJobEventListener(this, job.getId());
				int phase = phaser.arriveAndDeregister();
			}

		}
	}
}
