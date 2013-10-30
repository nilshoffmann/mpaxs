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
package net.sf.mpaxs.api;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/**
 * Interface for completion services that support the retrieval of
 * more detailed task information, such as failed and cancelled tasks.
 *
 * Implementations of this interface can used to chain separate phases
 * of a parallel algorithm with intermediate completion requirements.
 *
 * This interface extends <code>Callable<List<T>></code> so that the completion
 * service itself can be submitted to other completion services or executor
 * services.
 *
 * @author Nils Hoffmann
 * @param <T> the result type
 */
public interface ICompletionService<T> extends Callable<List<T>> {

	/**
	 * Returns only failed tasks.
	 *
	 * @return the failed tasks
	 */
	List<Callable<T>> getFailedTasks();

	/**
	 * Returns all failed and cancelled tasks.
	 *
	 * @return the failed and cancelled tasks
	 */
	List<Callable<T>> getFailedOrCancelledTasks();

	/**
	 * Returns only the cancelled tasks.
	 *
	 * @return the cancelled tasks
	 */
	List<Callable<T>> getCancelledTasks();

	/**
	 * Creates a new Future for the submitted Callable.
	 *
	 * @param c the callable
	 * @return a new <code>Future</code> for the submitted <code>Callable</code>
	 * @throws RejectedExecutionException
	 * @throws NullPointerException
	 */
	Future<T> submit(Callable<T> c) throws RejectedExecutionException, NullPointerException;

	/**
	 * Creates a new Future for the submitted Runnable.
	 *
	 * @param r the runnable
	 * @param t the result type on successful completion of the task
	 * @return a new <code>Future</code> for the submitted <code>Runnable</code>
	 * @throws RejectedExecutionException
	 * @throws NullPointerException
	 */
	Future<T> submit(Runnable r, T t) throws RejectedExecutionException, NullPointerException;

}
