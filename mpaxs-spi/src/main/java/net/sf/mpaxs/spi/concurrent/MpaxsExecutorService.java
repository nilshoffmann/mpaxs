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
import net.sf.mpaxs.api.Impaxs;

/**
 * <code>ExecutorService</code> implementation for remote execution via RMI.
 * Wraps <code>Runnable</code> and <code>Callable</code> jobs into <code>MpaxsFutureTask</code>.
 *
 * @author Nils Hoffmann
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.AbstractExecutorService
 * @see MpaxsFutureTask
 */
public class MpaxsExecutorService extends AbstractExecutorService {

	private ExecutorService es = Executors.newSingleThreadExecutor();
	private final Impaxs computeServer;

	/**
	 * Create a new instance using the default compute server.
	 *
	 * @see ComputeServerFactory#getComputeServer()
	 */
	public MpaxsExecutorService() {
		this(ComputeServerFactory.getComputeServer());
	}

	/**
	 * Creates a new instance using the supplied compute server.
	 *
	 * @param executionServer
	 */
	public MpaxsExecutorService(Impaxs executionServer) {
		this.computeServer = executionServer;
	}

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
		return new MpaxsFutureTask<T>(computeServer, runnable, value);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		Logger.getLogger(MpaxsExecutorService.class.getName()).log(Level.FINER,
			"Creating new FutureTask for {}", callable.getClass());
		return new MpaxsFutureTask<T>(computeServer, callable);
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
		Logger.getLogger(MpaxsExecutorService.class.getName()).log(Level.FINER,
			"Running {}", r);
		es.execute(r);
	}
}
