/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.spi;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.maltcms.execution.api.Impaxs;
import net.sf.maltcms.execution.api.concurrent.DefaultCallable;
import net.sf.maltcms.execution.api.concurrent.DefaultRunnable;
import net.sf.maltcms.execution.api.event.IJobEventListener;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.job.Job;
import net.sf.maltcms.execution.api.job.Status;

/**
 *
 * @author nilshoffmann
 */
public class MaltcmsFutureTask<V> implements
        RunnableFuture<V>, IJobEventListener {

    private final IJob<V> job;
    private final Impaxs computeServer = ComputeServerFactory.getComputeServer();
    private Lock lock = new ReentrantLock(true);
    private Condition completed = lock.newCondition();
    private V result = null;

    public MaltcmsFutureTask(Callable<V> callable) {
        job = new Job<V>(new DefaultCallable<V>(callable));
        computeServer.addJobEventListener(this);
    }

    public MaltcmsFutureTask(Runnable r, V v) {
        job = new Job<V>(new DefaultRunnable<V>(r, v));
        computeServer.addJobEventListener(this);
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
        return (job.getStatus() == Status.DONE) && (result!=null);
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        if (job.getStatus() != Status.DONE) {
            throw new InterruptedException();
        }
        if (job.getStatus() == Status.ERROR) {
            throw new ExecutionException("Job terminated with exception!", job.
                    getThrowable());
        }
        final Lock lock = this.lock;
        lock.lockInterruptibly();
        try {
            try {
                while (result == null) {
                    completed.await();
                }
            } catch (InterruptedException ie) {
                completed.signal(); // propagate to non-interrupted thread
                throw ie;
            }
            return result;
        } finally {
            lock.unlock();
        }

    }

    @Override
    public V get(long l, TimeUnit tu) throws InterruptedException, ExecutionException, TimeoutException {
        long nanos = tu.toNanos(l);
        final Lock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                if (result != null) {
                    return result;
                }
                if (nanos <= 0) {
                    return null;
                }
                try {
                    nanos = completed.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    completed.signal(); // propagate to non-interrupted thread
                    throw ie;
                }

            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        computeServer.submitJob(job);
    }

    @Override
    public void jobChanged(IJob job) {
        if (job.getId().equals(this.job.getId())) {
            if (job.getStatus() == Status.DONE) {
                try {
                    result = (V) job.getClassToExecute().get();
                } catch (InterruptedException ex) {
                    Logger.getLogger(MaltcmsFutureTask.class.getName()).
                            log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(MaltcmsFutureTask.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
                computeServer.removeJobEventListener(this);
            }else if(job.getStatus() == Status.CANCELED) {
                computeServer.removeJobEventListener(this);
            }else if(job.getStatus() == Status.ERROR) {
                computeServer.removeJobEventListener(this);
            }
        }
    }
}
