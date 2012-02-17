package net.sf.maltcms.execution.spi;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import net.sf.maltcms.execution.api.ICompletionService;
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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author nilshoffmann
 */
public class MaltcmsCompletionService<T extends Serializable> implements
		ICompletionService<T> {

	private int callables = 0;
	private int done = 0;
	private int failed = 0;
	private int cancelled = 0;
	private int maxThreads = 1;
	private long myTimeToWaitForTasks = 1;
	private TimeUnit myTimeUnitToWaitForTasks = TimeUnit.HOURS;
	private boolean myBlockingWait = false;
	private ExecutorService e = null;
	private ExecutorCompletionService<T> es = null;
	private Map<Future<T>, Callable<T>> futureToTaskMap = null;

	public MaltcmsCompletionService() {
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
	public MaltcmsCompletionService(ExecutorService e,
			long myTimeToWaitForTasks, TimeUnit myTimeUnitToWaitForTasks,
			boolean myBlockingWait) {
		this();
		if (e != null) {
			this.e = e;
		} else {
			this.maxThreads = Math.max(1,Math.min(1, Runtime.getRuntime()
					.availableProcessors() - 1));
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
		futureToTaskMap = Collections
				.synchronizedMap(new LinkedHashMap<Future<T>, Callable<T>>());
		// e = Executors.newFixedThreadPool(myMaxThreads);
	}

	/**
	 * @param es
	 * @param myTimeToWaitForTasks
	 * @param myTimeUnitToWaitForTasks
	 * @param myBlockingWait
	 */
	public MaltcmsCompletionService(ExecutorService es,
			long myTimeToWaitForTasks, String myTimeUnitToWaitForTasks,
			boolean myBlockingWait) {
		this(es, myTimeToWaitForTasks, TimeUnit
				.valueOf(myTimeUnitToWaitForTasks), myBlockingWait);
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
			// count up to the number of submitted tasks minus the number
			// of failed tasks, irrespective of submission order
			while (i < (callables - (failed + cancelled))) {
				if (retrieveResult(results)) {
					i++;
				}
			}
		} finally {
			// cancel all remaining tasks
			if(!futureToTaskMap.keySet().isEmpty()) {
				Logger.getLogger(MaltcmsCompletionService.class.getName())
					.log(Level.DEBUG,
						"Cancelling " + futureToTaskMap.size()
							+ " tasks!");
			}
			for (Future<T> f : futureToTaskMap.keySet()) {
				f.cancel(true);
			}
		}
		if(failed!=0 || cancelled!=0) {
			Logger.getLogger(MaltcmsCompletionService.class.getName())
					.log(Level.DEBUG,
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
								MaltcmsCompletionService.class.getName())
								.log(
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
					// System.out.println(
					// "Waiting forever for computation to complete");
					try {
						t = f.get();
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						return retrievedResult;
					}

				} else {
					// System.out.println(
					// "Waiting for " + myTimeToWaitForTasks + " " +
					// myTimeUnitToWaitForTasks +
					// " for computation to complete");
					try {
						t = f.get(myTimeToWaitForTasks,
								myTimeUnitToWaitForTasks);
					} catch (InterruptedException ie) {
						Logger.getLogger(
								MaltcmsCompletionService.class.getName())
								.log(Level.WARNING,"Interrupted while waiting "
								+ myTimeToWaitForTasks + " "
								+ myTimeUnitToWaitForTasks
								+ " for computation to finish!");
						Thread.currentThread().interrupt();
						return retrievedResult;
					} catch (TimeoutException te) {
						Logger.getLogger(
								MaltcmsCompletionService.class.getName())
								.log(Level.WARNING,"Timed out while waiting "
								+ myTimeToWaitForTasks + " "
								+ myTimeUnitToWaitForTasks
								+ " for computation to finish!");
						return retrievedResult;
					}
				}

				// only add result if t != null
				if (t != null) {
					done++;
					// System.out.println(
					// getClass().getSimpleName() + ":
					Logger.getLogger(
							MaltcmsCompletionService.class
									.getName()).log(
							Level.INFO,
							done + " of " + callables
									+ " submitted jobs finished. " + failed
									+ " jobs failed, " + cancelled
									+ " were cancelled!");
					futureToTaskMap.remove(f);
					results.add(t);
					retrievedResult = true;
				}
			} catch (CancellationException ce) {
				Logger.getLogger(
						MaltcmsCompletionService.class.getName())
						.log(Level.WARNING,
								"Job was cancelled: \n"
										+ ce.getLocalizedMessage());
				cancelled++;
			} catch (Exception ee) {
				Logger.getLogger(
						MaltcmsCompletionService.class.getName())
						.log(Level.SEVERE, null, ee);
				failed++;
			}
		}
		return retrievedResult;
	}

	private Future<T> getActiveFuture() {
		Future<T> f = null;
		if (myBlockingWait) {
			// System.out.println("Waiting forever for next available future");
			try {
				f = es.take();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		} else {
			// System.out.println("Waiting for "+myTimeToWaitForTasks+" "+myTimeUnitToWaitForTasks+" for next available future");
			try {
				f = es.poll(myTimeToWaitForTasks, myTimeUnitToWaitForTasks);

			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		return f;
	}

	@Override
	public Future<T> submit(Callable<T> c) throws RejectedExecutionException,
			NullPointerException {
		if (e instanceof MpaxsExecutorService && !(c instanceof Serializable)) {
			throw new RejectedExecutionException(
					"Callable must extend Serializable for remote execution!");
		}
		// if (c instanceof Serializable) {
		// Callable<List<? extends T>> c1 = (Callable<List<? extends T>>) c;
		Future<T> f = es.submit(c);
		futureToTaskMap.put(f, c);
		callables++;
		return f;
		// }
		// throw new
		// RejectedExecutionException("Callable "+c.getClass().getName()+" does not implement Serializable!");
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
		callables++;
		return f;
	}
}
