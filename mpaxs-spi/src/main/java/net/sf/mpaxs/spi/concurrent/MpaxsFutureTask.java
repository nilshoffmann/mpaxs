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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
 *
 * @author Nils Hoffmann
 */
public class MpaxsFutureTask<V> implements
        RunnableFuture<V>, IJobEventListener {

    private final IJob<V> job;
    private final Impaxs computeServer;
    private BlockingQueue<V> intermediateQueue = new LinkedBlockingQueue<V>(1);
    private BlockingQueue<V> resultQueue = new LinkedBlockingQueue<V>(1);
    private V result = null;

    public MpaxsFutureTask(Impaxs computeServer, Callable<V> callable) {
        this.computeServer = computeServer;
        computeServer.addJobEventListener(this);
        job = new Job<V>(new DefaultCallable<V>(callable));
    }

    public MpaxsFutureTask(Impaxs computeServer, Runnable r, V v) {
        this.computeServer = computeServer;
        computeServer.addJobEventListener(this);
        job = new Job<V>(new DefaultRunnable<V>(r, v));
    }

    @Override
    public boolean cancel(boolean bln) {
        return computeServer.cancelJob(job.getId());
    }

    @Override
    public boolean isCancelled() {
        return job.getStatus() == Status.CANCELED;
    }

    @Override
    public boolean isDone() {
        return (job.getStatus() == Status.DONE) && (resultQueue.isEmpty());
    }

    @Override
    public V get() throws InterruptedException, ExecutionException, CancellationException {
        return resultQueue.take();
    }

    @Override
    public V get(long l, TimeUnit tu) throws InterruptedException, ExecutionException, CancellationException, TimeoutException {
        return resultQueue.poll(l, tu);
    }

    @Override
    public void run() {
        computeServer.submitJob(job);
        try {
//            System.out.println("Submitted job, waiting for result!");
            result = intermediateQueue.take();
//            System.out.println("Receieved result, passing to resultQueue!");
            resultQueue.put(result);
        } catch (InterruptedException ex) {
            Logger.getLogger(MpaxsFutureTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void jobChanged(final IJob job) {
        if (job.getStatus() == Status.DONE) {
            try {
//                System.out.println("Adding result to queue!");
                intermediateQueue.put((V) job.getClassToExecute().get());
            } catch (InterruptedException ex) {
                Logger.getLogger(MpaxsFutureTask.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(MpaxsFutureTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
}
