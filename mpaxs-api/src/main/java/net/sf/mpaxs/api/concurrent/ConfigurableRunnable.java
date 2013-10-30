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
package net.sf.mpaxs.api.concurrent;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import net.sf.mpaxs.api.job.Progress;

/**
 * Implementation of remote runnable tasks that require external configuration via
 * a configuration file.
 *
 * Take care that the type V returned is Serializable.
 *
 * @author Kai Bernd Stadermann
 * @param <V>
 */
public interface ConfigurableRunnable<V> extends Runnable, Serializable {

	/**
	 * An InterruptedException is thrown, when the result is not yet ready.
	 * An ExecutionException is thrown, when the job has experienced an Exception
	 * or Error of any kind.
	 *
	 * @return the result
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public V get() throws InterruptedException, ExecutionException;

	/**
	 * Configure the configurable runnable.
	 *
	 * @param pathToConfig the configuration file location
	 */
	public void configure(File pathToConfig);

	/**
	 * Returns the progress object of this configurable runnable.
	 *
	 * @return the progress object
	 */
	public Progress getProgress();
}
