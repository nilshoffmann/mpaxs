/*
 * Mpaxs, modular parallel execution system. 
 * Copyright (C) 2010-2012, The authors of Mpaxs. All rights reserved.
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
 * @author Kai Bernd Stadermann
 */
public class Settings {

    //configuration keys
    //default values
    private static final int DEFAULT_LOCAL_PORT = 1099;
    private static final String DEFAULT_BASE_DIR = System.getProperty("user.dir")+File.separator+"mpaxs" + File.separator + "masterServer";
    private static String DEFAULT_CODEBASE = DEFAULT_BASE_DIR + File.separator + "codebase";
    private static final String DEFAULT_POLICY_NAME = DEFAULT_BASE_DIR + File.separator + "wideopen.policy";
    private static final String DEFAULT_NAME = "MasterServer";
    private static final String DEFAULT_LOCAL_IP = getOwnIP();
    private static final int DEFAULT_SCHEDULE_WAIT_TIME = 5;
    private static final String DEFAULT_PATH_TO_COMPUTEHOST_JAR = DEFAULT_BASE_DIR + File.separator + "computeHost" + File.separator + "ComputeHost.jar";
    private static final String DEFAULT_COMPUTE_HOST_MAIN_CLASS = "net.sf.mpaxs.spi.computeHost.StartUp";
    private static final String DEFAULT_PATH_TO_JAVA = "/vol/java-1.6/bin/java";
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

    public static void setConfigFile(String file) {
        if (file != null) {
            String[] tmp = {file};
            CONFIG_FILES = tmp;
        }
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public void setOption(String key, String value) {
        this.config.setProperty(key, value);
    }

    public Object getOption(String key) {
//        if (this.config.containsKey(key)) {
        return this.config.getProperty(key);
//        }
//        throw new NullPointerException(
//                "Key " + key + " not contained in Settings!");
    }

    private void load() {
        System.out.println(
                "Settings holds " + CONFIG_FILES.length + " configuration files!");
        for (int i = 0; i < CONFIG_FILES.length; i++) {

            addConfigFile(CONFIG_FILES[i]);
        }
//        if (CONFIG_FILES.length == 0) {
//            System.out.println("Preparing defaults.");
        prepareDefaults();
//        }
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
        System.out.println("Writing default policy to " + policyFile.
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

        //create working directory
//        new File(getComputeHostWorkingDir()).mkdirs();
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

//            FileInputStream inputStream;
//            try {
//                inputStream = new FileInputStream(path);
//                prop.load(inputStream);
//                inputStream.close();
//
//
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE,
//                        null, ex);
//            } catch (IOException ex) {
//                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE,
//                        null, ex);
//            }


        }
    }

    public Integer getInt(String key) {
//        try {
        return Integer.valueOf(config.getInt(key, 0));
//        } catch (NullPointerException ex) {
//            return 0;
//        } catch (NumberFormatException ex) {
//            return 0;
//        }
    }

    public String getString(String key) {
        return config.getString(key);
    }

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

//        if (tmp != 0) {
//            return tmp;
//        } else {
//            return DEFAULT_MAX_NUMBER_OF_CHOSTS;
//        }
        return maxChosts;
    }

    public int getMaxJobsPerHost() {
        int tmp = getInt(ConfigurationKeys.KEY_MAX_JOBS_PER_HOST);
        if (tmp != 0) {
            return tmp;
        } else {
            return DEFAULT_MAX_JOBS_PER_HOST;
        }
    }

    public int getMaxErrorsPerJob() {
        int tmp = getInt(ConfigurationKeys.KEY_MAX_ERROR_PER_JOB);
        if (tmp != 0) {
            return tmp;
        } else {
            return DEFAULT_MAX_ERROR_PER_JOB;
        }
    }

    public boolean getErrorToConsole() {
        String tmp = getString(ConfigurationKeys.KEY_ERROR_TO_CONSOLE);
        if (tmp != null && tmp.equals("true")) {
            return true;
        } else {
            return DEFAULT_ERROR_TO_CONSOLE;
        }
    }

    public boolean getGuiMode() {
        String tmp = getString(ConfigurationKeys.KEY_GUI_MODE);
        if (tmp != null && tmp.equals("false")) {
            return false;
        } else {
            return DEFAULT_GUI_MODE;
        }
    }

    public File getBaseDir() {
        String ret = getString(ConfigurationKeys.KEY_BASE_DIR);
        if (ret == null) {
            return new File(DEFAULT_BASE_DIR);
        } else {
            return new File(ret);
        }
    }

    public File getInputDir() {
        return new File(
                getBaseDir().getAbsolutePath() + File.separator + "input");
    }

    public File getRunningDir() {
        return new File(
                getBaseDir().getAbsolutePath() + File.separator + "running");
    }

    public File getDoneDir() {
        return new File(getBaseDir().getAbsolutePath() + File.separator + "done");
    }

    public File getErrorDir() {
        return new File(
                getBaseDir().getAbsolutePath() + File.separator + "error");
    }

    public ExecutionType getExecutionMode() {
        String val = getString(ConfigurationKeys.KEY_EXECUTION_MODE);
        if (val == null) {
            System.out.println(
                    "Using default execution mode: " + DEFAULT_EXECUTION_MODE);
            return DEFAULT_EXECUTION_MODE;
        }
        ExecutionType ret = ExecutionType.valueOf(val);
//        if (ret!=null) {
        return ret;
//        } else {
//            return DEFAULT_EXECUTION_MODE;
//        }
    }

    public String getComputeHostOutputFile() {
        String ret = getString(ConfigurationKeys.KEY_COMPUTE_HOST_OUTPUT_FILE);
        if (ret == null) {
            return DEFAULT_COMPUTE_HOST_OUTPUT_FILE;
        } else {
            return ret;
        }
    }

    public String getComputeHostErrorFile() {
        String ret = getString(ConfigurationKeys.KEY_COMPUTE_HOST_ERROR_FILE);
        if (ret == null) {
            return DEFAULT_COMPUTE_HOST_ERROR_FILE;
        } else {
            return ret;
        }
    }

    public String getComputeHostWorkingDir() {
        String ret = getString(ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR);
        if (ret == null) {
            return DEFAULT_COMPUTE_HOST_WORKING_DIR;
        } else {
            return ret;
        }
    }
    
    public String getComputeHostMainClass() {
        String ret = getString(ConfigurationKeys.KEY_COMPUTE_HOST_MAIN_CLASS);
        if (ret == null) {
            return DEFAULT_COMPUTE_HOST_MAIN_CLASS;
        } else {
            return ret;
        }
    }

    public String getPathToJava() {
        String ret = getString(ConfigurationKeys.KEY_PATH_TO_JAVA);
        if (ret == null) {
            return DEFAULT_PATH_TO_JAVA;
        } else {
            return ret;
        }
    }

    public String getPathToComputeHostJar() {
        String ret = getString(ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR);
        if (ret == null) {
            return DEFAULT_PATH_TO_COMPUTEHOST_JAR;
        } else {
            return ret;
        }
    }

    public int getScheduleWaitingTime() {
        int ret = getInt(ConfigurationKeys.KEY_SCHEDULE_WAIT_TIME);
        if (ret == 0) {
            return DEFAULT_SCHEDULE_WAIT_TIME;
        } else {
            return ret;
        }
    }

    public int getLocalPort() {
        int ret = getInt(ConfigurationKeys.KEY_LOCAL_PORT);
        if (ret == 0) {
            return DEFAULT_LOCAL_PORT;
        } else {
            return ret;
        }
    }

    public void setLocalPort(int newLocalPort) {
        config.setProperty(ConfigurationKeys.KEY_LOCAL_PORT, newLocalPort);
    }

    public String getLocalIP() {
        String ret = getString(ConfigurationKeys.KEY_LOCAL_IP);
        if (ret == null) {
            return DEFAULT_LOCAL_IP;
        } else {
            return ret;
        }
    }

    public String getCodebase() {
        String ret = getString(ConfigurationKeys.KEY_CODEBASE);
        if (ret == null) {
            return DEFAULT_CODEBASE;
        } else {
            return ret;
        }
    }

    public String getPolicyName() {
        String ret = getString(ConfigurationKeys.KEY_POLICY_NAME);
        if (ret == null) {
            return DEFAULT_POLICY_NAME;
        } else {
            return ret;
        }
    }

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


        } catch (UnknownHostException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null,
                    ex);
        }

        return inet2.getHostAddress();
    }
}
