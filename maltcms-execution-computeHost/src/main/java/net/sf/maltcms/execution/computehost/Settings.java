/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 */
package net.sf.maltcms.execution.computehost;

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
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.api.server.IRemoteServer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author Kai Bernd Stadermann
 * @author Nils Hoffmann
 * 
 */
public class Settings {

    //configuration keys
    //default values
//    private static int DEFAULT_LOCAL_PORT = 1099;
//    private static final String DEFAULT_LOCAL_IP = getOwnIP();
//    private static final String DEFAULT_CODEBASE = new File(System.getProperty("user.dir")).getAbsolutePath();
//    private static final String DEFAULT_POLICY_NAME = "client.policy";
//    private static final String DEFAULT_MASTERSERVER_NAME ="MasterServer";
//    private static String DEFAULT_MASTERSERVER_IP ="127.0.0.1";
//    private static final int DEFAULT_MASTER_SERVER_TIMEOUT = 15;
//    private static final int DEFAULT_TIMEOUT_BEFORE_SHUTDOWN = 2000;
//    private static final int DEFAULT_NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
//    private static final String DEFAULT_SILENT_MODE = "true";
    //values stored during runtime
    private UUID hostID;
    private IRemoteServer remoteReference;
    //path to configfiles
    private static final String[] CONFIG_FILES = {};//"./chconfig.txt"
    //instance of settings
//    private static Settings instance = null;
    //storage of configuration entrys
//    private Map<String, String> config = new HashMap<String, String>();
    private PropertiesConfiguration config = new PropertiesConfiguration();

    //overwrites defaults
    public Settings(Configuration config) {
        this();
        //overwrite defaults
        Iterator iter = config.getKeys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            this.config.setProperty(key, config.getProperty(key));
        }
    }

    public Settings() {
        load();
    }

//    public static Settings getInstance(){
//        if(instance == null) {
//            instance = new Settings();
//        }
//        return instance;
//    }
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
        for (int i = 0; i < CONFIG_FILES.length; i++) {

            addConfigFile(CONFIG_FILES[i]);
        }
    }

    public void setOption(String key, String value) {
        this.config.setProperty(key, value);
    }

    public String getOption(String key) {
        if (this.config.containsKey(key)) {
            return this.config.getString(key);
        }
        throw new NullPointerException("Key "+key+" not bound in configuration!");
    }

//    public Configuration getOption(String key) {
//        return this.config.getProperty(key);
//    }
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

    public int getLocalPort() {
        int ret = getInt(ConfigurationKeys.KEY_LOCAL_PORT);
//        if(ret == 0){
//            return DEFAULT_LOCAL_PORT;
//        }else{
        return ret;
//        }
    }

    public int getMasterServerTimeout() {
        int ret = getInt(ConfigurationKeys.KEY_MASTER_SERVER_TIMEOUT);
//        if (ret == 0) {
//            return DEFAULT_MASTER_SERVER_TIMEOUT;
//        } else {
        return ret;
//        }
    }

    public int getTimeoutBeforeShutdown() {
        int ret = getInt(ConfigurationKeys.KEY_TIMEOUT_BEFORE_SHUTDOWN);
//        if (ret == 0) {
//            return DEFAULT_TIMEOUT_BEFORE_SHUTDOWN;
//        } else {
        return ret;
//        }
    }

    public IRemoteServer getRemoteReference() {
        return remoteReference;
    }

    public void setRemoteReference(IRemoteServer remoteReference) {
        this.remoteReference = remoteReference;
    }

    public void setHostID(UUID hostID) {
        this.hostID = hostID;
    }

    public int getMasterServerPort() {
        int ret = getInt(ConfigurationKeys.KEY_MASTERSERVER_PORT);
        return ret;
    }
    
    public String getMasterServerIP() {
        String ret = getString(ConfigurationKeys.KEY_MASTERSERVER_IP);
//        if (ret == null) {
//            return DEFAULT_MASTERSERVER_IP;
//        } else {
        return ret;
//        }
    }

    public int getCores() {
        int ret = getInt(ConfigurationKeys.KEY_NUMBER_OF_CORES);
//        if (ret == 0) {
//            return DEFAULT_NUMBER_OF_CORES;
//        } else {
        return ret;
//        }
    }

    public String getLocalIp() {
        String ret = getString(ConfigurationKeys.KEY_LOCAL_IP);
//        if (ret == null) {
//            return DEFAULT_LOCAL_IP;
//        } else {
        return ret;
//        }
    }

    public String getName() {
        String ret = getString(ConfigurationKeys.KEY_NAME);
        if (ret == null) {
            ret = UUID.randomUUID().toString();
            config.setProperty(ConfigurationKeys.KEY_NAME, ret);
        }
        return ret;
    }

    public URL getCodebase() throws MalformedURLException {
        String ret = getString(ConfigurationKeys.KEY_CODEBASE);
//        if (ret == null) {
//            return new File(DEFAULT_CODEBASE).toURI().toURL();
//        } else {
        return new File(ret).toURI().toURL();
//        }
    }

    public UUID getHostID() {
        return hostID;
    }

    public String getPolicyName() {
        String ret = getString(ConfigurationKeys.KEY_POLICY_NAME);
//        if (ret == null) {
//            return DEFAULT_POLICY_NAME;
//        } else {
        return ret;
//        }
    }

    public String getMasterServerName() {
        String ret = getString(ConfigurationKeys.KEY_MASTERSERVER_NAME);
//        if (ret == null) {
//            return DEFAULT_MASTERSERVER_NAME;
//        } else {
        return ret;
//        }
    }

    public boolean getSilentMode() {
        String ret = getString(ConfigurationKeys.KEY_SILENT_MODE);
        if (ret == null) {
            return false;
        }
        if (ret.equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    private String getOwnIP() {

        InetAddress inet2 = null;
        try {
            InetAddress inet1 = InetAddress.getLocalHost();
            inet2 = InetAddress.getByName(inet1.getHostName());
        } catch (UnknownHostException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
        return inet2.getHostAddress();
    }

    public void setDEFAULT_MASTERSERVER_IP(String DEFAULT_MASTERSERVER_IP) {
        config.setProperty(ConfigurationKeys.KEY_MASTERSERVER_IP,
                DEFAULT_MASTERSERVER_IP);
    }

    public void setDEFAULT_PORT(String DEFAULT_PORT) {
        config.setProperty(ConfigurationKeys.KEY_LOCAL_PORT, Integer.parseInt(
                DEFAULT_PORT));
    }
}
