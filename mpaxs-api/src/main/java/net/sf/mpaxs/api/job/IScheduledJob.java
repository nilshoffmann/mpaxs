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

import java.util.concurrent.TimeUnit;

/**
 * A scheduled job is a job that should be run repeatedly after the initial
 * delay and with the given period.
 *
 * @author Nils Hoffmann
 * @param <T>
 * @see java.util.concurrent.ScheduledExecutorService
 */
public interface IScheduledJob<T> extends IJob<T> {

	/**
	 * Returns the initial delay before this job should be scheduled.
	 *
	 * @return the initial delay
	 */
	long getInitialDelay();

	/**
	 * Returns the scheduling period between two successive invocations.
	 *
	 * @return the period
	 */
	long getPeriod();

	/**
	 * Returns the time unit for initialDelay and period.
	 *
	 * @return the time unit
	 */
	TimeUnit getTimeUnit();

}
