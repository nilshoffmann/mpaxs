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
package net.sf.mpaxs.api.job;

/**
 * Job status enumeration. The job lifecycle starts in <code>UNKNOWN</code>.
 * Once the job is scheduled, it changes to <code>WAITING</code>. It changes to <code>RUNNING</code>
 * once the job is being executed by a compute host. The job status is <code>DONE</code>
 * if the job finished successfully or <code>ERROR</code> if it encountered an exception, or <code>CANCELED</code>
 * if it was cancelled.
 *
 * @author Kai Bernd Stadermann
 */
public enum Status {

	/**
	 * Initial lifecycle state of a job.
	 */
	UNKNOWN,
	/**
	 * State after submission to an execution queue.
	 */
	WAITING,
	/**
	 * State after transmission and begin of execution on a compute host.
	 */
	RUNNING,
	/**
	 * State after succesful termination of a job.
	 */
	DONE,
	/**
	 * State after experiencing an exception.
	 */
	ERROR,
	/**
	 * State after cancellation of a job.
	 */
	CANCELED;
}
