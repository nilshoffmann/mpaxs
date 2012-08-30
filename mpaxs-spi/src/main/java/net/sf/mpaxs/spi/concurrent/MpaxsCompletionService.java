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

import net.sf.mpaxs.api.ICompletionService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nils Hoffmann
 */
public class MpaxsCompletionService<T extends Serializable> implements
        ICompletionService<T> {

    private int callables = 0;
    private int done = 0;
    private int failed = 0;
    private int cancelled = 0;
    private int maxThreads = 1;
    private long myTimeToWaitForTasks = 5;
    private TimeUnit myTimeUnitToWaitForTasks = TimeUnit.SECONDS;
    private boolean myBlockingWait = false;
    private ExecutorService e = null;
    private ExecutorCompletionService<T> es = null;
    private Map<Future<T>, Callable<T>> futureToTaskMap = null;
    private LinkedBlockingQueue<Future<T>> futures = null;

    public MpaxsCompletionService() {
        super();
        init();
        this.e = Executors.newSingleThreadExecutor();
        this.es = new ExecutorCompletionService<T>(e);
    }

    /**
     * @param es
     * @param myTimeToWaitForTasks
     * @param myTimeUnitToWaitForTasks
     * @param myBlockingWait
     */
    public MpaxsCompletionService(ExecutorService e,
            long myTimeToWaitForTasks, TimeUnit myTimeUnitToWaitForTasks,
            boolean myBlockingWait) {
        this();
        if (e != null) {
            this.e = e;
        } else {
            this.maxThreads = Math.max(1, Math.min(1, Runtime.getRuntime().availableProcessors() - 1));
            this.e = Executors.newFixedThreadPool(this.maxThreads);
        }
        this.es = new ExecutorCompletionService<T>(this.e);
        this.myTimeToWaitForTasks = myTimeToWaitForTasks;
        this.myTimeUnitToWaitForTasks = myTimeUnitToWaitForTasks;
        this.myBlockingWait = myBlockingWait;
    }

    private void init() {
        callables = 0;
        done = 0;
        failed = 0;
        cancelled = 0;
        futureToTaskMap = Collections.synchronizedMap(new LinkedHashMap<Future<T>, Callable<T>>());
        futures = new LinkedBlockingQueue<Future<T>>();
        // e = Executors.newFixedThreadPool(myMaxThreads);
    }

    /**
     * @param es
     * @param myTimeToWaitForTasks
     * @param myTimeUnitToWaitForTasks
     * @param myBlockingWait
     */
    public MpaxsCompletionService(ExecutorService es,
            long myTimeToWaitForTasks, String myTimeUnitToWaitForTasks,
            boolean myBlockingWait) {
        this(es, myTimeToWaitForTasks, TimeUnit.valueOf(myTimeUnitToWaitForTasks), myBlockingWait);
    }

    public boolean isBlockingWait() {
        return myBlockingWait;
    }

    public int getMaxThreads() {
        return this.maxThreads;
    }

    public long getTimeToWaitForTasks() {
        return myTimeToWaitForTasks;
    }

    public TimeUnit getTimeUnitToWaitForTasks() {
        return myTimeUnitToWaitForTasks;
    }

    @Override
    public List<T> call() throws Exception {
        if (e.isShutdown() || e.isTerminated()) {
            throw new IllegalStateException("Executor was already shut down!");
        }
        e.shutdown();
        List<T> results = new LinkedList<T>();
        try {
            int i = 0;
            //System.out.println("Received "+callables+" tasks, "+failed+" failed, "+cancelled+" were cancelled!");
            // count up to the number of submitted tasks minus the number
            // of failed tasks, irrespective of submission order
            while (i < (callables - (failed + cancelled))) {
                //System.out.println("i: "+i+" callables: "+callables+" failed+cancelled:"+(failed+cancelled));
                if (retrieveResult(results)) {
                    //System.out.println("Retrieved result "+i);
                    i++;
                }
            }
        } finally {
            // cancel all remaining tasks
            if (!futureToTaskMap.keySet().isEmpty()) {
                Logger.getLogger(MpaxsCompletionService.class.getName()).log(Level.FINEST,
                        "Cancelling " + futureToTaskMap.size()
                        + " tasks!");
            }
            for (Future<T> f : futureToTaskMap.keySet()) {
                f.cancel(true);
            }
        }
        if ((callables - (failed + cancelled)) != 0) {
            Logger.getLogger(MpaxsCompletionService.class.getName()).log(Level.FINEST,
                    "Retrieved all results. " + done + " jobs succeeded, "
                    + failed + " failed, " + cancelled
                    + " were cancelled.");
        }
        waitForShutdownCompletion();
        return results;
    }

    private void waitForShutdownCompletion() {
        try {
            // Wait a while for existing tasks to terminate
            if (callables - (failed + cancelled) > 0) {
                if (!e.awaitTermination(myTimeToWaitForTasks,
                        myTimeUnitToWaitForTasks)) {
                    e.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!e.awaitTermination(myTimeToWaitForTasks,
                            myTimeUnitToWaitForTasks)) {
                        Logger.getLogger(
                                MpaxsCompletionService.class.getName()).log(
                                Level.SEVERE,
                                "Thread pool did not terminate after waiting for "
                                + myTimeToWaitForTasks
                                + " "
                                + myTimeUnitToWaitForTasks);
                    }
                }
            } else {
                e.shutdownNow();
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread is also interrupted
            e.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        e = null;
        es = null;
    }

    @Override
    public List<Callable<T>> getFailedTasks() {
        return new ArrayList<Callable<T>>(futureToTaskMap.values());
    }

    private boolean retrieveResult(List<T> results) {
        Future<T> f = getActiveFuture();
        boolean retrievedResult = false;
        if (f != null) {
            T t = null;
            try {
                if (myBlockingWait) {
                    //System.out.println(
                    //        "Waiting forever for computation to complete");
                    try {
                        t = f.get();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        //System.out.println("Interrupted while waiting forever!");
                        return false;
                    }

                } else {
//                    System.out.println(
//                            "Waiting for " + myTimeToWaitForTasks + " "
//                            + myTimeUnitToWaitForTasks
//                            + " for computation to complete");
                    try {
                        t = f.get(myTimeToWaitForTasks,
                                myTimeUnitToWaitForTasks);
                    } catch (InterruptedException ie) {
//                        System.out.println("Interrupted while waiting for some time!");
                        Logger.getLogger(
                                MpaxsCompletionService.class.getName()).log(Level.WARNING, "Interrupted while waiting "
                                + myTimeToWaitForTasks + " "
                                + myTimeUnitToWaitForTasks
                                + " for computation to finish!");
                        //Thread.currentThread().interrupt();
                        return false;
                    } catch (TimeoutException te) {
//                        System.out.println("Timeout while waiting for some time!");
                        Logger.getLogger(
                                MpaxsCompletionService.class.getName()).log(Level.WARNING, "Timed out while waiting "
                                + myTimeToWaitForTasks + " "
                                + myTimeUnitToWaitForTasks
                                + " for computation to finish!");
                        return false;
                    }
                }

                // only add result if t != null
                if (t != null) {
//                    System.out.println("Retrieved a valid result");
                    done++;
                    // System.out.println(
                    // getClass().getSimpleName() + ":
                    Logger.getLogger(
                            MpaxsCompletionService.class.getName()).log(
                            Level.INFO,
                            done + " of " + callables
                            + " submitted jobs finished. " + failed
                            + " jobs failed, " + cancelled
                            + " were cancelled!");
                    futureToTaskMap.remove(f);
                    futures.remove(f);
                    results.add(t);
                    return true;
                }
            } catch (CancellationException ce) {
                Logger.getLogger(
                        MpaxsCompletionService.class.getName()).log(Level.WARNING,
                        "Job was cancelled: \n"
                        + ce.getLocalizedMessage());
                cancelled++;
            } catch (Exception ee) {
                Logger.getLogger(
                        MpaxsCompletionService.class.getName()).log(Level.SEVERE, null, ee);
                failed++;
            }
        }
        return retrievedResult;
    }

    private Future<T> getActiveFuture() {
        return futures.peek();
    }

    @Override
    public Future<T> submit(Callable<T> c) throws RejectedExecutionException,
            NullPointerException {
        if (e instanceof MpaxsExecutorService && !(c instanceof Serializable)) {
            throw new RejectedExecutionException(
                    "Callable must extend Serializable for remote execution!");
        }
        Future<T> f = es.submit(c);
        futureToTaskMap.put(f, c);
        futures.add(f);
        callables++;
        return f;
    }

    @Override
    public Future<T> submit(Runnable r, T t) throws RejectedExecutionException,
            NullPointerException {
        if (e instanceof MpaxsExecutorService && !(r instanceof Serializable)) {
            throw new RejectedExecutionException(
                    "Runnable must extend Serializablei for remote execution!");
        }
        if (e instanceof MpaxsExecutorService && !(t instanceof Serializable)) {
            throw new RejectedExecutionException(
                    "Return type t must extend Serializable for remote execution!");
        }
        Future<T> f = es.submit(r, t);
        futureToTaskMap.put(f, Executors.callable(r, t));
        futures.add(f);
        callables++;
        return f;
    }
}
