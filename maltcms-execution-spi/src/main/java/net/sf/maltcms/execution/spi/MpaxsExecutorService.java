/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.spi;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author nilshoffmann
 */
public class MpaxsExecutorService extends AbstractExecutorService {

	private ExecutorService es = Executors.newSingleThreadExecutor();

	@Override
	public void shutdown() {
		es.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return es.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return es.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return es.isTerminated();
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return new MaltcmsFutureTask<T>(runnable, value);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		System.out.println("Creating new FutureTask");
		Logger.getLogger(MpaxsExecutorService.class.getName()).log(Level.INFO,
				"Creating new FutureTask for {}", callable.getClass());
		return new MaltcmsFutureTask<T>(callable);
	}

	@Override
	public boolean awaitTermination(long l, TimeUnit tu)
			throws InterruptedException {
		return es.awaitTermination(l, tu);
	}

	@Override
	public Future<?> submit(Runnable r) {
		if (!(r instanceof Serializable)) {
			throw new IllegalArgumentException(
					"Runnable must implement Serializable!");
		}
		return super.submit(r);
	}

	@Override
	public <T> Future<T> submit(Runnable r, T t) {
		if (!(r instanceof Serializable)) {
			throw new IllegalArgumentException(
					"Runnable must implement Serializable!");
		}
		return super.submit(r, t);
	}

	@Override
	public <T> Future<T> submit(Callable<T> clbl) {
		if (!(clbl instanceof Serializable)) {
			throw new IllegalArgumentException(
					"Runnable must implement Serializable!");
		}
		return super.submit(clbl);
	}

	@Override
	public void execute(Runnable r) {
		System.out.println("Running");
		Logger.getLogger(MpaxsExecutorService.class.getName()).log(Level.INFO,
				"Running {}", r);
		es.execute(r);
	}
}
