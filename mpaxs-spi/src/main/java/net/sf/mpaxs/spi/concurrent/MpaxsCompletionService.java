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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ICompletionService;

/**
 * This class allows to create a completion service that allows access to
 * cancelled, failed and successful <code>Callables</code>.
 *
 * @author Nils Hoffmann
 * @param <T>
 */
public class MpaxsCompletionService<T extends Serializable> implements
	ICompletionService<T> {

	private final AtomicInteger callables = new AtomicInteger(0);
	private final AtomicInteger done = new AtomicInteger(0);
	private final AtomicInteger failed = new AtomicInteger(0);
	private final AtomicInteger cancelled = new AtomicInteger(0);
	private int maxThreads = 1;
	private long myTimeToWaitForTasks = 5;
	private TimeUnit myTimeUnitToWaitForTasks = TimeUnit.SECONDS;
	private boolean myBlockingWait = false;
	private ExecutorService e = null;
	private ExecutorCompletionService<T> es = null;
	private Map<Future<T>, Callable<T>> futureToTaskMap = null;
	private LinkedBlockingQueue<Callable<T>> failedTasks = null, cancelledTasks = null;

	/**
	 * Create a new completion service with default cached thread pool.
	 */
	public MpaxsCompletionService() {
		super();
		init();
		this.e = Executors.newCachedThreadPool();
		this.es = new ExecutorCompletionService<T>(e);
	}

	/**
	 * Create a new completion service.
	 *
	 * @param e                        the executor service to use
	 * @param myTimeToWaitForTasks     time to wait for tasks, if myBlockingWait=false
	 * @param myTimeUnitToWaitForTasks time unit to wait for tasks
	 * @param myBlockingWait           use blocking wait if true, use non-blocking wait otherwise
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
		callables.set(0);
		done.set(0);
		failed.set(0);
		cancelled.set(0);
		futureToTaskMap = new ConcurrentHashMap<Future<T>, Callable<T>>();
		failedTasks = new LinkedBlockingQueue<Callable<T>>();
		cancelledTasks = new LinkedBlockingQueue<Callable<T>>();
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

	/**
	 * Returns whether this completion service uses blocking wait.
	 *
	 * @return true if blocking wait, false otherwise
	 */
	public boolean isBlockingWait() {
		return myBlockingWait;
	}

	/**
	 * Returns the maximum number of threads to use for the backing executor service.
	 *
	 * @return the maximum number of threads
	 */
	public int getMaxThreads() {
		return this.maxThreads;
	}

	/**
	 * Returns the time to wait for tasks if this completion service is set to
	 * non-blocking wait.
	 *
	 * @return the time to wait for tasks
	 */
	public long getTimeToWaitForTasks() {
		return myTimeToWaitForTasks;
	}

	/**
	 * Returns the time unit to wait for tasks.
	 *
	 * @return the time unit to wait for tasks
	 */
	public TimeUnit getTimeUnitToWaitForTasks() {
		return myTimeUnitToWaitForTasks;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws java.lang.Exception
	 * @throws IllegalStateException if call is invoked more than once
	 */
	@Override
	public List<T> call() throws Exception {
		if (e == null) {
			throw new IllegalStateException("MpaxsCompletionService was already shut down and terminated!");
		}
		if (e.isShutdown() || e.isTerminated()) {
			throw new IllegalStateException("MpaxsCompletionService executor was already shut down!");
		}
		//take no more submissions
		e.shutdown();
		LinkedBlockingQueue<T> results = new LinkedBlockingQueue<T>();
		try {
			// count up to the number of submitted tasks minus the number
			// of failed tasks, irrespective of submission order
			while (!futureToTaskMap.keySet().isEmpty()) {
				retrieveResult(results);
			}
		} finally {
			// cancel all remaining tasks
			if (!futureToTaskMap.keySet().isEmpty()) {
				Logger.getLogger(MpaxsCompletionService.class.getName()).log(Level.FINEST,
					"Cancelling " + futureToTaskMap.size()
					+ " tasks!");
			}
			cancelled.getAndAdd(futureToTaskMap.keySet().size());
			cancelledTasks.addAll(futureToTaskMap.values());
			for (Future<T> f : futureToTaskMap.keySet()) {
				f.cancel(true);
			}
			futureToTaskMap.clear();
		}
		//if ((callables.get() - (failed.get() + cancelled.get())) != 0) {
		if (futureToTaskMap.isEmpty()) {
			Logger.getLogger(MpaxsCompletionService.class.getName()).log(Level.FINEST,
				"Retrieved all results. " + done + " jobs succeeded, "
				+ failed + " failed, " + cancelled
				+ " were cancelled.");
		}
		waitForShutdownCompletion();
		return new ArrayList<T>(results);
	}

	private synchronized void waitForShutdownCompletion() {
		try {
			// Wait a while for existing tasks to terminate
			if (!futureToTaskMap.keySet().isEmpty()) {
				if (!e.awaitTermination(myTimeToWaitForTasks,
					myTimeUnitToWaitForTasks)) {
					e.shutdownNow(); // Cancel currently executing tasks
					cancelled.getAndAdd(futureToTaskMap.keySet().size());
					cancelledTasks.addAll(futureToTaskMap.values());
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
				cancelled.getAndAdd(futureToTaskMap.keySet().size());
				cancelledTasks.addAll(futureToTaskMap.values());
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread is also interrupted
			e.shutdownNow();
			cancelled.getAndAdd(futureToTaskMap.keySet().size());
			cancelledTasks.addAll(futureToTaskMap.values());
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
		e = null;
		es = null;
	}

	@Override
	public List<Callable<T>> getFailedTasks() {
		ArrayList<Callable<T>> al = new ArrayList<Callable<T>>(failedTasks);
		return al;
	}

	@Override
	public List<Callable<T>> getFailedOrCancelledTasks() {
		ArrayList<Callable<T>> al = new ArrayList<Callable<T>>(failedTasks);
		al.addAll(cancelledTasks);
		return al;
	}

	@Override
	public List<Callable<T>> getCancelledTasks() {
		ArrayList<Callable<T>> al = new ArrayList<Callable<T>>(cancelledTasks);
		return al;
	}

	private boolean retrieveResult(Queue<T> results) {
		Future<T> f = getActiveFuture();
		boolean retrievedResult = false;
		if (f != null) {
			T t = null;
			try {
				if (myBlockingWait) {
					try {
						t = f.get();
					} catch (InterruptedException ie) {
						Logger.getLogger(
							MpaxsCompletionService.class.getName()).log(Level.WARNING, "Interrupted while waiting "
								+ myTimeToWaitForTasks + " "
								+ myTimeUnitToWaitForTasks
								+ " for computation to finish!");
						Thread.currentThread().interrupt();
						return false;
					}

				} else {
					try {
						t = f.get(myTimeToWaitForTasks,
							myTimeUnitToWaitForTasks);
					} catch (InterruptedException ie) {
						Logger.getLogger(
							MpaxsCompletionService.class.getName()).log(Level.WARNING, "Interrupted while waiting "
								+ myTimeToWaitForTasks + " "
								+ myTimeUnitToWaitForTasks
								+ " for computation to finish!");
						Thread.currentThread().interrupt();
						return false;
					} catch (TimeoutException te) {
						Logger.getLogger(
							MpaxsCompletionService.class.getName()).log(Level.FINE, "Timed out while waiting "
								+ myTimeToWaitForTasks + " "
								+ myTimeUnitToWaitForTasks
								+ " for computation to finish!");
						return false;
					}
				}

				// only add result if t != null
				if (t != null) {
					done.incrementAndGet();
					if (Logger.getLogger(
						MpaxsCompletionService.class.getName()).isLoggable(Level.FINE)) {
						Logger.getLogger(
							MpaxsCompletionService.class.getName()).log(
								Level.FINE,
								done + " of " + callables
								+ " submitted jobs finished. " + failed
								+ " jobs failed, " + cancelled
								+ " were cancelled!");
					}
					futureToTaskMap.remove(f);
					results.add(t);
					return true;
				} else {
					return false;
				}
			} catch (CancellationException ce) {
				Logger.getLogger(
					MpaxsCompletionService.class.getName()).log(Level.WARNING,
						"Job was cancelled: \n"
						+ ce.getLocalizedMessage());
				cancelled.incrementAndGet();
				failedTasks.add(futureToTaskMap.remove(f));
			} catch (Exception ee) {
				Logger.getLogger(
					MpaxsCompletionService.class.getName()).log(Level.SEVERE, null, ee);
				failed.incrementAndGet();
				failedTasks.add(futureToTaskMap.remove(f));
			}
		}
		return retrievedResult;
	}

	private Future<T> getActiveFuture() {
		try {
			return es.take();
		} catch (InterruptedException ex) {
			Thread.interrupted();
		}
		return null;
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
		callables.incrementAndGet();
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
		Callable<T> c = Executors.callable(r, t);
		Future<T> f = es.submit(c);
		futureToTaskMap.put(f, c);
		callables.incrementAndGet();
		return f;
	}
}
