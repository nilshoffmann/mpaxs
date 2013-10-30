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

/**
 *
 * @author Nils Hoffmann
 */
public class ConfigurationKeys {

    //compute host keys
	/**
	 *
	 */
	public static final String KEY_NAME = "NAME";

	/**
	 *
	 */
	public static final String KEY_CODEBASE = "CODEBASE";

	/**
	 *
	 */
	public static final String KEY_POLICY_NAME = "POLICY_NAME";

	/**
	 *
	 */
	public static final String KEY_MASTERSERVER_NAME = "MASTERSERVER_NAME";

	/**
	 *
	 */
	public static final String KEY_MASTERSERVER_IP = "MASTERSERVER_IP";

	/**
	 *
	 */
	public static final String KEY_MASTERSERVER_PORT = "MASTERSERVER_PORT";

	/**
	 *
	 */
	public static final String KEY_MASTER_SERVER_TIMEOUT = "MASTER_SERVER_TIMEOUT";

	/**
	 *
	 */
	public static final String KEY_TIMEOUT_BEFORE_SHUTDOWN = "TIMEOUT_BEFORE_SHUTDOWN";

	/**
	 *
	 */
	public static final String KEY_NUMBER_OF_CORES = "NUMBER_OF_CORES";

	/**
	 *
	 */
	public static final String KEY_SILENT_MODE = "SILENT_MODE";

	/**
	 *
	 */
	public static final String KEY_ERROR_FILE = "ERROR_FILE";

	/**
	 *
	 */
	public static final String KEY_OUTPUT_FILE = "OUTPUT_FILE";

    //drmaa specific
	/**
	 *
	 */
	public static final String KEY_NATIVE_SPEC = "NATIVE_SPEC";

    //master server keys
	/**
	 *
	 */
	public static final String KEY_SCHEDULE_WAIT_TIME = "SCHEDULE_WAIT_TIME";

	/**
	 *
	 */
	public static final String KEY_PATH_TO_COMPUTEHOST_JAR = "PATH_TO_COMPUTEHOST_JAR";

	/**
	 *
	 */
	public static final String KEY_PATH_TO_JAVA = "PATH_TO_JAVA";

	/**
	 *
	 */
	public static final String KEY_COMPUTE_HOST_WORKING_DIR = "COMPUTE_HOST_WORKING_DIR";

	/**
	 *
	 */
	public static final String KEY_COMPUTE_HOST_ERROR_FILE = "COMPUTE_HOST_ERROR_FILE";

	/**
	 *
	 */
	public static final String KEY_COMPUTE_HOST_OUTPUT_FILE = "COMPUTE_HOST_OUTPUT_FILE";

	/**
	 *
	 */
	public static final String KEY_EXECUTION_MODE = "EXECUTION_MODE";

	/**
	 *
	 */
	public static final String KEY_BASE_DIR = "BASE_DIR";

	/**
	 *
	 */
	public static final String KEY_GUI_MODE = "GUI_MODE";

	/**
	 *
	 */
	public static final String KEY_ERROR_TO_CONSOLE = "ERROR_TO_CONSOLE";

	/**
	 *
	 */
	public static final String KEY_MAX_ERROR_PER_JOB = "MAX_ERROR_PER_JOB";

	/**
	 *
	 */
	public static final String KEY_MAX_JOBS_PER_HOST = "MAX_JOBS_PER_HOST";

	/**
	 *
	 */
	public static final String KEY_MAX_NUMBER_OF_CHOSTS = "MAX_NUMBER_OF_CHOSTS";

	/**
	 *
	 */
	public static final String KEY_EXPORT_JMX = "EXPORT_JMX";

    //common keys
	/**
	 *
	 */
	public static final String KEY_AUTH_TOKEN = "AUTH_TOKEN";

	/**
	 *
	 */
	public static final String KEY_LOCAL_PORT = "LOCAL_PORT";

	/**
	 *
	 */
	public static final String KEY_LOCAL_IP = "LOCAL_IP";

	/**
	 *
	 */
	public static final String KEY_COMPUTE_HOST_MAIN_CLASS = "COMPUTE_HOST_MAIN_CLASS";

	/**
	 *
	 */
	public static final String KEY_MASTER_SERVER_EXIT_ON_SHUTDOWN = "MASTER_SERVER_EXIT_ON_SHUTDOWN";
}
