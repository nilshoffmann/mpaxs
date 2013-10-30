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
package net.sf.mpaxs.spi.server.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.ExecutionType;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Speichert alle Konfigurationen des MasterServers ab.
 * Als singelton implementiert, sodass während der Ausführung immer nur eine
 * Instanz der Konfiguration vorhanden ist.
 *
 * @author Kai Bernd Stadermann
 */
public class Settings {

	//configuration keys
	//default values
	private static final int DEFAULT_LOCAL_PORT = 1099;
	private static final String DEFAULT_BASE_DIR = System.getProperty("user.dir") + File.separator + "mpaxs" + File.separator + "masterServer";
	private static String DEFAULT_CODEBASE = DEFAULT_BASE_DIR + File.separator + "codebase";
	private static final String DEFAULT_POLICY_NAME = DEFAULT_BASE_DIR + File.separator + "wideopen.policy";
	private static final String DEFAULT_NAME = "MasterServer";
	private static final String DEFAULT_LOCAL_IP = getOwnIP();
	private static final int DEFAULT_SCHEDULE_WAIT_TIME = 5;
	private static final String DEFAULT_PATH_TO_COMPUTEHOST_JAR = DEFAULT_BASE_DIR + File.separator + "computeHost" + File.separator + "ComputeHost.jar";
	private static final String DEFAULT_COMPUTE_HOST_MAIN_CLASS = "net.sf.mpaxs.spi.computeHost.StartUp";
	private static final String DEFAULT_PATH_TO_JAVA = "java";
	private static final String DEFAULT_COMPUTE_HOST_WORKING_DIR = DEFAULT_BASE_DIR + File.separator + "computeHost";
	private static final String DEFAULT_COMPUTE_HOST_ERROR_FILE = DEFAULT_COMPUTE_HOST_WORKING_DIR + File.separator + "error.txt";
	private static final String DEFAULT_COMPUTE_HOST_OUTPUT_FILE = DEFAULT_COMPUTE_HOST_WORKING_DIR + File.separator + "output.txt";
	private static final ExecutionType DEFAULT_EXECUTION_MODE = ExecutionType.LOCAL;
	private static final boolean DEFAULT_GUI_MODE = true;
	private static final boolean DEFAULT_ERROR_TO_CONSOLE = false;
	private static final int DEFAULT_MAX_ERROR_PER_JOB = 3;
	private static final int DEFAULT_MAX_JOBS_PER_HOST = 5;
	private static final int DEFAULT_MAX_NUMBER_OF_CHOSTS = 1;
	//path to configfiles
	private static String[] CONFIG_FILES = {};//"./config.txt"
	//instance of settings
	private static Settings instance = null;
	//storage of configuration entrys
//    private Map<String, String> config = new HashMap<String, String>();
	private PropertiesConfiguration config = new PropertiesConfiguration();

	private Settings() {
		load();
	}

	/**
	 *
	 * @param file
	 */
	public static void setConfigFile(String file) {
		if (file != null) {
			String[] tmp = {file};
			CONFIG_FILES = tmp;
		}
	}

	/**
	 *
	 * @return
	 */
	public static Settings getInstance() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}

	/**
	 *
	 * @param key
	 * @param value
	 */
	public void setOption(String key, String value) {
		this.config.setProperty(key, value);
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public Object getOption(String key) {
		return this.config.getProperty(key);
	}

	private void load() {
		Logger.getLogger(Settings.class.getName()).
			log(Level.INFO, "Settings holds {0} configuration files!", CONFIG_FILES.length);
		for (String CONFIG_FILES1 : CONFIG_FILES) {
			addConfigFile(CONFIG_FILES1);
		}
		prepareDefaults();
	}

	private void prepareDefaults() {
		//create basedir
		new File(DEFAULT_BASE_DIR).mkdirs();
		//create codebase dir
		File codebase = new File(DEFAULT_CODEBASE);
		codebase.mkdirs();
		try {
			DEFAULT_CODEBASE = new File(DEFAULT_CODEBASE).toURI().toURL().toString();
		} catch (MalformedURLException ex) {
			Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
		}

		//create default policyfile
		File policyFile = new File(DEFAULT_POLICY_NAME);
		Logger.getLogger(Settings.class.getName()).
			log(Level.INFO, "Writing default policy to {0}", policyFile.
				getAbsolutePath());
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().
			getResourceAsStream(
				"/net/sf/mpaxs/spi/server/wideopen.policy")));
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(policyFile));
			String s = null;
			while ((s = br.readLine()) != null) {
				bw.write(s + "\n");
			}
			bw.flush();
			bw.close();
			br.close();
		} catch (IOException ex) {
			Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null,
				ex);
		}

	}

	private void addConfigFile(String path) {
		if (path != null) {
			PropertiesConfiguration prop;
			try {
				prop = new PropertiesConfiguration(path);
				Iterator keyIter = prop.getKeys();
				while (keyIter.hasNext()) {
					Object obj = keyIter.next();
					String s = (String) obj;
					config.setProperty(s, prop.getProperty(s));
				}
			} catch (ConfigurationException ex) {
				Logger.getLogger(Settings.class.getName()).
					log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public Integer getInt(String key) {
		return Integer.valueOf(config.getInt(key, 0));
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		return config.getString(key);
	}

	/**
	 *
	 * @return
	 */
	public int getMaxNumberOfChosts() {
		int maxChosts = 1;
		int tmp = getInt(ConfigurationKeys.KEY_MAX_NUMBER_OF_CHOSTS);
		if (tmp == 0) {
			tmp = DEFAULT_MAX_NUMBER_OF_CHOSTS;
		}
		switch (getExecutionMode()) {
			case LOCAL:
				maxChosts = Math.min(Runtime.getRuntime().availableProcessors(),
					Math.max(tmp, maxChosts));
				break;
			default:
				maxChosts = Math.max(maxChosts, tmp);
				break;

		}
		return maxChosts;
	}

	/**
	 *
	 * @return
	 */
	public int getMaxJobsPerHost() {
		int tmp = getInt(ConfigurationKeys.KEY_MAX_JOBS_PER_HOST);
		if (tmp != 0) {
			return tmp;
		} else {
			return DEFAULT_MAX_JOBS_PER_HOST;
		}
	}

	/**
	 *
	 * @return
	 */
	public int getMaxErrorsPerJob() {
		int tmp = getInt(ConfigurationKeys.KEY_MAX_ERROR_PER_JOB);
		if (tmp != 0) {
			return tmp;
		} else {
			return DEFAULT_MAX_ERROR_PER_JOB;
		}
	}

	/**
	 *
	 * @return
	 */
	public boolean getErrorToConsole() {
		String tmp = getString(ConfigurationKeys.KEY_ERROR_TO_CONSOLE);
		if (tmp != null && tmp.equals("true")) {
			return true;
		} else {
			return DEFAULT_ERROR_TO_CONSOLE;
		}
	}

	/**
	 *
	 * @return
	 */
	public boolean getGuiMode() {
		String tmp = getString(ConfigurationKeys.KEY_GUI_MODE);
		if (tmp != null && tmp.equals("false")) {
			return false;
		} else {
			return DEFAULT_GUI_MODE;
		}
	}

	/**
	 *
	 * @return
	 */
	public File getBaseDir() {
		String ret = getString(ConfigurationKeys.KEY_BASE_DIR);
		if (ret == null) {
			return new File(DEFAULT_BASE_DIR);
		} else {
			return new File(ret);
		}
	}

	/**
	 *
	 * @return
	 */
	public File getInputDir() {
		return new File(
			getBaseDir().getAbsolutePath() + File.separator + "input");
	}

	/**
	 *
	 * @return
	 */
	public File getRunningDir() {
		return new File(
			getBaseDir().getAbsolutePath() + File.separator + "running");
	}

	/**
	 *
	 * @return
	 */
	public File getDoneDir() {
		return new File(getBaseDir().getAbsolutePath() + File.separator + "done");
	}

	/**
	 *
	 * @return
	 */
	public File getErrorDir() {
		return new File(
			getBaseDir().getAbsolutePath() + File.separator + "error");
	}

	/**
	 *
	 * @return
	 */
	public ExecutionType getExecutionMode() {
		String val = getString(ConfigurationKeys.KEY_EXECUTION_MODE);
		if (val == null) {
			System.out.println(
				"Using default execution mode: " + DEFAULT_EXECUTION_MODE);
			return DEFAULT_EXECUTION_MODE;
		}
		ExecutionType ret = ExecutionType.valueOf(val);
		return ret;
	}

	/**
	 *
	 * @return
	 */
	public String getComputeHostOutputFile() {
		String ret = getString(ConfigurationKeys.KEY_COMPUTE_HOST_OUTPUT_FILE);
		if (ret == null) {
			return DEFAULT_COMPUTE_HOST_OUTPUT_FILE;
		} else {
			return ret;
		}
	}

	/**
	 *
	 * @return
	 */
	public String getComputeHostErrorFile() {
		String ret = getString(ConfigurationKeys.KEY_COMPUTE_HOST_ERROR_FILE);
		if (ret == null) {
			return DEFAULT_COMPUTE_HOST_ERROR_FILE;
		} else {
			return ret;
		}
	}

	/**
	 *
	 * @return
	 */
	public String getComputeHostWorkingDir() {
		String ret = getString(ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR);
		if (ret == null) {
			return DEFAULT_COMPUTE_HOST_WORKING_DIR;
		} else {
			return ret;
		}
	}

	/**
	 *
	 * @return
	 */
	public String getComputeHostMainClass() {
		String ret = getString(ConfigurationKeys.KEY_COMPUTE_HOST_MAIN_CLASS);
		if (ret == null) {
			return DEFAULT_COMPUTE_HOST_MAIN_CLASS;
		} else {
			return ret;
		}
	}

	/**
	 *
	 * @return
	 */
	public String getPathToJava() {
		String ret = getString(ConfigurationKeys.KEY_PATH_TO_JAVA);
		if (ret == null) {
			return DEFAULT_PATH_TO_JAVA;
		} else {
			return ret;
		}
	}

	/**
	 *
	 * @return
	 */
	public String getPathToComputeHostJar() {
		String ret = getString(ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR);
		if (ret == null) {
			return DEFAULT_PATH_TO_COMPUTEHOST_JAR;
		} else {
			return ret;
		}
	}

	/**
	 *
	 * @return
	 */
	public int getScheduleWaitingTime() {
		int ret = getInt(ConfigurationKeys.KEY_SCHEDULE_WAIT_TIME);
		if (ret == 0) {
			return DEFAULT_SCHEDULE_WAIT_TIME;
		} else {
			return ret;
		}
	}

	/**
	 *
	 * @return
	 */
	public int getLocalPort() {
		int ret = getInt(ConfigurationKeys.KEY_LOCAL_PORT);
		if (ret == 0) {
			return DEFAULT_LOCAL_PORT;
		} else {
			return ret;
		}
	}

	/**
	 *
	 * @param newLocalPort
	 */
	public void setLocalPort(int newLocalPort) {
		config.setProperty(ConfigurationKeys.KEY_LOCAL_PORT, newLocalPort);
	}

	/**
	 *
	 * @return
	 */
	public String getLocalIP() {
		String ret = getString(ConfigurationKeys.KEY_LOCAL_IP);
		if (ret == null) {
			return DEFAULT_LOCAL_IP;
		} else {
			return ret;
		}
	}

	/**
	 *
	 * @return
	 */
	public String getCodebase() {
		String ret = getString(ConfigurationKeys.KEY_CODEBASE);
		if (ret == null) {
			return DEFAULT_CODEBASE;
		} else {
			return ret;
		}
	}

	/**
	 *
	 * @return
	 */
	public String getPolicyName() {
		String ret = getString(ConfigurationKeys.KEY_POLICY_NAME);
		if (ret == null) {
			return DEFAULT_POLICY_NAME;
		} else {
			return ret;
		}
	}

	/**
	 *
	 * @return
	 */
	public String getName() {
		String ret = getString(ConfigurationKeys.KEY_NAME);
		if (ret == null) {
			return DEFAULT_NAME;
		} else {
			return ret;
		}
	}

	private static String getOwnIP() {

		InetAddress inet2 = null;
		try {
			InetAddress inet1 = InetAddress.getLocalHost();
			inet2 = InetAddress.getByName(inet1.getHostName());
			return inet2.getHostAddress();
		} catch (UnknownHostException ex) {
			Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null,
				ex);
		}
		return null;
	}
}
