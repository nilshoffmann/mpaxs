package net.sf.maltcms.execution.spi;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */


import net.sf.maltcms.execution.api.ICompletionService;
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
public class MaltcmsResubmissionCompletionService<T extends Serializable>
        implements ICompletionService<T> {

    private MaltcmsCompletionService<T> mcs = new MaltcmsCompletionService<T>();
    private HashMap<Callable<T>, Integer> submissionCounter = null;
    private Collection<Callable<T>> submission = null;
    private Collection<Callable<T>> failedJobs = null;
    private int submitted = 0;
    private int failed = 0;
    private int finished = 0;
    private int maxSubmissions = 3;

    public MaltcmsResubmissionCompletionService() {
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
//        mcs.reset();
    }

    /**
     * @param mcs
     */
    public MaltcmsResubmissionCompletionService(MaltcmsCompletionService<T> mcs) {
        this();
        this.mcs = mcs;
    }

    public int getMaxSubmissions() {
        return maxSubmissions;
    }

    public void setMaxSubmissions(int maxSubmissions) {
        this.maxSubmissions = maxSubmissions;
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
    public Future<T> submit(Callable<T> c)
            throws RejectedExecutionException, NullPointerException {
        if (submissionClosed) {
            throw new RejectedExecutionException(
                    "CompletionService already started execution.");
        }
        submissionCounter.put(c, 1);
        submitted++;
        return mcs.submit(c);
    }
    
    @Override
    public Future<T> submit(Runnable r, T t) throws RejectedExecutionException, NullPointerException {
        if (submissionClosed) {
            throw new RejectedExecutionException(
                    "CompletionService already started execution.");
        }
        submissionCounter.put(Executors.callable(r, t), 1);
        submitted++;
        return mcs.submit(r,t);
    }

    @Override
    public List<T> call() throws Exception {
        submissionClosed = true;
        System.out.println("Submitted " + submitted + " jobs!");
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
                    if (submissionCounter.get(c) < maxSubmissions) {
                        System.out.println("Resubmitting " + c + " for try "
                                + (submissionCounter.get(c) + 1) + "/"
                                + maxSubmissions);
                        submissionCounter.put(c, submissionCounter.get(c) + 1);
                        submission.add(c);
                    } else {
                        failedJobs.add(c);
                    }
                }

                failed = failedJobs.size();

                if (submitted == (finished + failed)) {
                    allJobsDone = true;
                    System.out.println("submitted jobs: " + submitted
                            + " | finished jobs: " + finished
                            + " | total failed jobs (after " + maxSubmissions + " tries): "
                            + failed);
                    return new ArrayList<T>(results);
                } else {
                    long timeOut = mcs.getTimeToWaitForTasks();
                    TimeUnit timeUnit = mcs.getTimeUnitToWaitForTasks();
                    boolean blockingWait = mcs.isBlockingWait();
//                    int maxThreads = mcs.getMaxThreads();

                    mcs = new MaltcmsCompletionService<T>(null,timeOut, timeUnit, blockingWait);
                }
            } catch (Exception ex) {
                Logger.getLogger(
                        MaltcmsResubmissionCompletionService.class.getName()).log(Level.SEVERE, null, ex);
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
