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
package net.sf.mpaxs.spi.computeHost;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.server.IRemoteServer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author Kai Bernd Stadermann
 * @author Nils Hoffmann
 *
 */
public class Settings {

	//values stored during runtime
	private UUID hostID;
	private IRemoteServer remoteReference;
	//path to configfiles
	private static final String[] CONFIG_FILES = {};//"./chconfig.txt"
	private final PropertiesConfiguration config = new PropertiesConfiguration();

	//overwrites defaults

	/**
	 *
	 * @param config
	 */
		public Settings(Configuration config) {
		this();
		//overwrite defaults
		Iterator iter = config.getKeys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			this.config.setProperty(key, config.getProperty(key));
		}
	}

	/**
	 *
	 */
	public Settings() {
		load();
	}

	private void load() {
		//set defaults
		config.setProperty(ConfigurationKeys.KEY_LOCAL_PORT, 1099);
		config.setProperty(ConfigurationKeys.KEY_LOCAL_IP, getOwnIP());
		config.setProperty(ConfigurationKeys.KEY_CODEBASE,
			new File(System.getProperty("user.dir")).getAbsolutePath());
		config.setProperty(ConfigurationKeys.KEY_POLICY_NAME, "client.policy");
		config.setProperty(ConfigurationKeys.KEY_MASTERSERVER_NAME,
			"MasterServer");
		config.setProperty(ConfigurationKeys.KEY_MASTERSERVER_IP, "127.0.0.1");
		config.setProperty(ConfigurationKeys.KEY_MASTERSERVER_PORT, "1099");
		config.setProperty(ConfigurationKeys.KEY_MASTER_SERVER_TIMEOUT, 15);
		config.setProperty(ConfigurationKeys.KEY_TIMEOUT_BEFORE_SHUTDOWN, 2000);
		config.setProperty(ConfigurationKeys.KEY_NUMBER_OF_CORES, 1);
		config.setProperty(ConfigurationKeys.KEY_SILENT_MODE, "true");
		config.setProperty(ConfigurationKeys.KEY_BASE_DIR, System.getProperty("user.dir"));
		for (String CONFIG_FILES1 : CONFIG_FILES) {
			addConfigFile(CONFIG_FILES1);
		}
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
	public String getOption(String key) {
		return getOption(key, null);
	}

	/**
	 *
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getOption(String key, String defaultValue) {
		if (this.config.containsKey(key)) {
			return this.config.getString(key);
		}
		if (defaultValue == null) {
			throw new NullPointerException("Key " + key + " not bound in configuration!");
		}
		return defaultValue;
	}

	private void addConfigFile(String path) {
		Properties prop = new Properties();

		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(path);
			prop.load(inputStream);
			inputStream.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null,
				ex);
		} catch (IOException ex) {
			Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null,
				ex);
		}

		for (Object key : prop.keySet()) {
			String s = (String) key;
			config.setProperty(s, prop.getProperty(s));
		}
	}

	private Integer getInt(String key) {
		try {
			return Integer.valueOf(config.getInt(key));
		} catch (NullPointerException ex) {
			return 0;
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	private String getString(String key) {
		return config.getString(key);
	}

	/**
	 *
	 * @return
	 */
	public int getLocalPort() {
		int ret = getInt(ConfigurationKeys.KEY_LOCAL_PORT);
		return ret;
	}

	/**
	 *
	 * @return
	 */
	public int getMasterServerTimeout() {
		int ret = getInt(ConfigurationKeys.KEY_MASTER_SERVER_TIMEOUT);
		return ret;
	}

	/**
	 *
	 * @return
	 */
	public int getTimeoutBeforeShutdown() {
		int ret = getInt(ConfigurationKeys.KEY_TIMEOUT_BEFORE_SHUTDOWN);
		return ret;
	}

	/**
	 *
	 * @return
	 */
	public IRemoteServer getRemoteReference() {
		return remoteReference;
	}

	/**
	 *
	 * @param remoteReference
	 */
	public void setRemoteReference(IRemoteServer remoteReference) {
		this.remoteReference = remoteReference;
	}

	/**
	 *
	 * @param hostID
	 */
	public void setHostID(UUID hostID) {
		this.hostID = hostID;
	}

	/**
	 *
	 * @return
	 */
	public int getMasterServerPort() {
		int ret = getInt(ConfigurationKeys.KEY_MASTERSERVER_PORT);
		return ret;
	}

	/**
	 *
	 * @return
	 */
	public String getMasterServerIP() {
		String ret = getString(ConfigurationKeys.KEY_MASTERSERVER_IP);
		return ret;
	}

	/**
	 *
	 * @return
	 */
	public int getCores() {
		int ret = getInt(ConfigurationKeys.KEY_NUMBER_OF_CORES);
		return ret;
	}

	/**
	 *
	 * @return
	 */
	public String getLocalIp() {
		String ret = getString(ConfigurationKeys.KEY_LOCAL_IP);
		return ret;
	}

	/**
	 *
	 * @return
	 */
	public String getName() {
		String ret = getString(ConfigurationKeys.KEY_NAME);
		if (ret == null) {
			ret = UUID.randomUUID().toString();
			config.setProperty(ConfigurationKeys.KEY_NAME, ret);
		}
		return ret;
	}

	/**
	 *
	 * @return
	 * @throws MalformedURLException
	 */
	public URL getCodebase() throws MalformedURLException {
		String ret = getString(ConfigurationKeys.KEY_CODEBASE);
		return new File(ret).toURI().toURL();
	}

	/**
	 *
	 * @return
	 */
	public UUID getHostID() {
		return hostID;
	}

	/**
	 *
	 * @return
	 */
	public String getPolicyName() {
		String ret = getString(ConfigurationKeys.KEY_POLICY_NAME);
		return ret;
	}

	/**
	 *
	 * @return
	 */
	public String getMasterServerName() {
		String ret = getString(ConfigurationKeys.KEY_MASTERSERVER_NAME);
		return ret;
	}

	/**
	 *
	 * @return
	 */
	public boolean getSilentMode() {
		String ret = getString(ConfigurationKeys.KEY_SILENT_MODE);
		if (ret == null) {
			return false;
		}
		return ret.equals("true");
	}

	private String getOwnIP() {

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

	/**
	 *
	 * @param DEFAULT_MASTERSERVER_IP
	 */
	public void setDEFAULT_MASTERSERVER_IP(String DEFAULT_MASTERSERVER_IP) {
		config.setProperty(ConfigurationKeys.KEY_MASTERSERVER_IP,
			DEFAULT_MASTERSERVER_IP);
	}

	/**
	 *
	 * @param DEFAULT_PORT
	 */
	public void setDEFAULT_PORT(String DEFAULT_PORT) {
		config.setProperty(ConfigurationKeys.KEY_LOCAL_PORT, Integer.parseInt(
			DEFAULT_PORT));
	}
}
