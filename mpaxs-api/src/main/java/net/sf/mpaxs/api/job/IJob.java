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
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.UUID;
import net.sf.mpaxs.api.concurrent.ConfigurableRunnable;

/**
 * T must implement Serializable, however, this is not enforceable
 * within the api to keep compatibility with JAVA's executor framework.
 * Thus, a runtime exception is thrown whenever a job with a type of T
 * is created/submitted, which is not Serializable.
 *
 * @author Nils Hoffmann
 * @param <T> the result type that this job provides
 */
public interface IJob<T> extends Serializable {

	/**
	 *
	 */
	void errorOccurred();

	/**
	 * Returns the <code>ConfigurableRunnable</code> instance to execute.
	 *
	 * @return the instance to execute
	 */
	ConfigurableRunnable<T> getClassToExecute();

	/**
	 * Returns the configuration file location used by this job instance.
	 *
	 * @return the configuration file location
	 */
	String getConfigurationFile();

	/**
	 * Returns the error number.
	 *
	 * @return the number of errors that have ocurred
	 */
	int getErrorCounter();

	/**
	 * Returns the unique id for this job.
	 *
	 * @return the unique job id
	 */
	UUID getId();

	/**
	 * Returns the file path to the job configuration file.
	 *
	 * @return the file path to the job configuration file
	 */
	String getJobConfigFile();

	/**
	 * Returns the status of this job.
	 *
	 * @return the status of this job
	 */
	Status getStatus();

	/**
	 * Set the job configuration file location.
	 *
	 * @param jobConfigFile
	 */
	void setJobConfigFile(String jobConfigFile);

	/**
	 * Set the status of this job.
	 *
	 * @param status
	 */
	void setStatus(Status status);

	/**
	 * Sets a job configuration file location which should contain the
	 * fully qualified name of the class to be executed by this job. Must
	 * implement <code>ConfigurableRunnable</code>.
	 *
	 * @param jobConfigFile
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	void setClassToExecute(String jobConfigFile) throws ClassNotFoundException,
		MalformedURLException, InstantiationException, IllegalAccessException, IOException;

	/**
	 * Set the <code>ConfigurableRunnable</code> to execute.
	 *
	 * @param cr the instance to execute
	 */
	void setClassToExecute(ConfigurableRunnable<T> cr);

	/**
	 * Set the throwable, if an exception has occurred.
	 *
	 * @param t the throwable
	 */
	void setThrowable(Throwable t);

	/**
	 * Get the throwable.
	 *
	 * @return the throwable or null if no exception has occurred
	 */
	Throwable getThrowable();

	/**
	 * The priority of this job instance.
	 * Jobs with a lower priority will have to wait until they are scheduled for execution,
	 * if any jobs with higher priorities are still in the job submission queue.
	 *
	 * @return the job priority
	 */
	int getPriority();

	/**
	 * Sets the priority of this job instance.
	 * This does not change scheduling affinity, once the job has been
	 * submitted for execution.
	 *
	 * @param priority the job priority
	 */
	void setPriority(int priority);

}
