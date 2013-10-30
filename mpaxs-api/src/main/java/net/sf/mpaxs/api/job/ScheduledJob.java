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

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.sf.mpaxs.api.concurrent.ConfigurableRunnable;

/**
 * Implementation of a scheduled job with defined initialDeplay, execution period
 * and timeUnit.
 *
 * @author Nils Hoffmann
 * @param <T> the result type of the job
 */
public class ScheduledJob<T> implements IScheduledJob<T> {

	private final long initialDelay;
	private final long period;
	private final TimeUnit timeUnit;
	private final IJob<T> delegate;

	/**
	 * Create a new instance.
	 *
	 * @param job          the job instance
	 * @param initialDelay the initial delay
	 * @param period       the scheduling period
	 * @param timeUnit     the time unit for period
	 */
	public ScheduledJob(IJob<T> job, long initialDelay, long period, TimeUnit timeUnit) {
		this.delegate = job;
		this.initialDelay = initialDelay;
		this.period = period;
		this.timeUnit = timeUnit;
	}

	/**
	 * Start scheduling of the provided job at the given date, with the provided period
	 * between successive invocations in the provided time unit.
	 *
	 * @param <T>       the result type
	 * @param job       the job instance
	 * @param startDate the start date
	 * @param period    the scheduling period
	 * @param timeUnit  the time unit for period
	 * @return
	 */
	public static <T> ScheduledJob<T> startAt(IJob<T> job, Date startDate, long period, TimeUnit timeUnit) {
		Date now = new Date();
		if (startDate.before(now)) {
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			throw new IllegalArgumentException("Desired start date lies in the past: " + sf.format(startDate) + " submitted at: " + sf.format(now));
		}
		long delta = startDate.getTime() - now.getTime();
		return new ScheduledJob<T>(job, delta, period, timeUnit);
	}

	@Override
	public long getInitialDelay() {
		return initialDelay;
	}

	@Override
	public long getPeriod() {
		return period;
	}

	@Override
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	@Override
	public void errorOccurred() {
		delegate.errorOccurred();
	}

	@Override
	public ConfigurableRunnable<T> getClassToExecute() {
		return delegate.getClassToExecute();
	}

	@Override
	public String getConfigurationFile() {
		return delegate.getConfigurationFile();
	}

	@Override
	public int getErrorCounter() {
		return delegate.getErrorCounter();
	}

	@Override
	public UUID getId() {
		return delegate.getId();
	}

	@Override
	public String getJobConfigFile() {
		return delegate.getJobConfigFile();
	}

	@Override
	public Status getStatus() {
		return delegate.getStatus();
	}

	@Override
	public void setJobConfigFile(String jobConfigFile) {
		delegate.setJobConfigFile(jobConfigFile);
	}

	@Override
	public void setStatus(Status status) {
		delegate.setStatus(status);
	}

	@Override
	public void setClassToExecute(String jobConfigFile) throws ClassNotFoundException, MalformedURLException, InstantiationException, IllegalAccessException, IOException {
		delegate.setClassToExecute(jobConfigFile);
	}

	@Override
	public void setClassToExecute(ConfigurableRunnable<T> cr) {
		delegate.setClassToExecute(cr);
	}

	@Override
	public void setThrowable(Throwable t) {
		delegate.setThrowable(t);
	}

	@Override
	public Throwable getThrowable() {
		return delegate.getThrowable();
	}

	@Override
	public int getPriority() {
		return delegate.getPriority();
	}

	@Override
	public void setPriority(int priority) {
		delegate.setPriority(priority);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 11 * hash + (int) (this.initialDelay ^ (this.initialDelay >>> 32));
		hash = 11 * hash + (int) (this.period ^ (this.period >>> 32));
		hash = 11 * hash + (this.timeUnit != null ? this.timeUnit.hashCode() : 0);
		hash = 11 * hash + (this.delegate != null ? this.delegate.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ScheduledJob<?> other = (ScheduledJob<?>) obj;
		if (this.initialDelay != other.initialDelay) {
			return false;
		}
		if (this.period != other.period) {
			return false;
		}
		if (this.timeUnit != other.timeUnit) {
			return false;
		}
		return this.delegate == other.delegate || (this.delegate != null && this.delegate.equals(other.delegate));
	}

	@Override
	public String toString() {
		return "ScheduledJob{" + "initialDelay=" + initialDelay + ", period=" + period + ", timeUnit=" + timeUnit + ", delegate=" + delegate + '}';
	}

}
