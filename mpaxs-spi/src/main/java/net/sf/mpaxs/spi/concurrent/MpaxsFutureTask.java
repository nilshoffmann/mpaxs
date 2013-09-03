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

import java.util.concurrent.*;
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
 */
public class MpaxsFutureTask<V> extends FutureTask<V> implements
		RunnableFuture<V>, IJobEventListener {

	private final IJob<V> job;
	private final Impaxs computeServer;
	private Phaser phaser;

	public MpaxsFutureTask(Impaxs computeServer, Callable<V> callable) {
		super(callable);
		this.computeServer = computeServer;
		job = new Job<V>(new DefaultCallable<V>(callable));
	}

	public MpaxsFutureTask(Impaxs computeServer, Runnable runnable, V result) {
		super(runnable, result);
		this.computeServer = computeServer;
		job = new Job<V>(new DefaultRunnable<V>(runnable, result));
	}

	@Override
	public boolean cancel(boolean bln) {
		boolean superCancelled = super.cancel(bln);
		//job status will be UNKNOWN for unsubmitted jobs
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
		//wait for job changed
		System.out.println("Waiting in thread " + Thread.currentThread().getName());
		System.out.println("Phaser has " + phaser.getRegisteredParties() + " registered parties of which " + phaser.getUnarrivedParties() + " have not arrived yet!");
		phaser.awaitAdvance(0);
		System.out.println("Job is Done!");
	}

	@Override
	public void jobChanged(final IJob job) {
		if (job.getId().equals(this.job.getId())) {
//			System.out.println("Job status: " + job.getStatus());
			if (job.getStatus() == Status.DONE) {
				try {
					V v = (V)job.getClassToExecute().get();
					if(v!=null) {
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
			}else if(job.getStatus() == Status.CANCELED) {
				cancel(true);
				computeServer.removeJobEventListener(this, job.getId());
				int phase = phaser.arriveAndDeregister();
			}else if(job.getStatus() == Status.ERROR) {
				setException(job.getThrowable());
				computeServer.removeJobEventListener(this, job.getId());
				int phase = phaser.arriveAndDeregister();
			}
			
		}
	}
}
