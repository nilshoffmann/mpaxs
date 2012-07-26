/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.spi;

import java.util.concurrent.*;
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
    private BlockingQueue<V> intermediateQueue = new LinkedBlockingQueue<V>(1);
    private BlockingQueue<V> resultQueue = new LinkedBlockingQueue<V>(1);
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
        return (job.getStatus() == Status.DONE) && (resultQueue.isEmpty());
    }

    @Override
    public V get() throws InterruptedException, ExecutionException, CancellationException {
//        System.out.println("Retrieving result with blocking get()");
//        Status status = job.getStatus();
//        if (status == Status.DONE) {
//            return resultQueue.take();
//        }
//        if (job.getStatus() == Status.CANCELED) {
//            throw new CancellationException();
//        }
//        if (job.getStatus() == Status.ERROR) {
//            throw new ExecutionException("Job terminated with exception!", job.getThrowable());
//        }
//        throw new InterruptedException("Job is not done yet!");
        return resultQueue.take();
    }

    @Override
    public V get(long l, TimeUnit tu) throws InterruptedException, ExecutionException, CancellationException, TimeoutException {
//        System.out.println("Retrieving result with non-blocking get()");
        return resultQueue.poll(l, tu);
//        Status status = job.getStatus();
//        if (status == Status.DONE) {
//            return resultQueue.poll(l, tu);
//        }
//        if (status == Status.CANCELED) {
//            throw new CancellationException();
//        }
//        if (status == Status.ERROR) {
//            throw new ExecutionException("Job terminated with exception!", job.getThrowable());
//        }
//        throw new InterruptedException("Job is not done yet!");
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
            Logger.getLogger(MaltcmsFutureTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void jobChanged(final IJob job) {
        if (job.getStatus() == Status.DONE) {
            try {
//                System.out.println("Adding result to queue!");
                intermediateQueue.put((V) job.getClassToExecute().get());
            } catch (InterruptedException ex) {
                Logger.getLogger(MaltcmsFutureTask.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(MaltcmsFutureTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
}
