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
package net.sf.mpaxs.spi.server;

import java.awt.Container;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.spi.server.settings.Settings;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class StartUp {

    /**
     * Starts the MasterServer.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        startUP(null);
    }

	/**
	 *
	 * @return
	 */
	public static MasterServer start() {
        return startUP(null);
    }

	/**
	 *
	 * @param configFile
	 * @return
	 */
	public static MasterServer start(String configFile) {
        Settings.setConfigFile(configFile);
        return startUP(null);
    }

	/**
	 *
	 * @param name
	 * @param value
	 */
	public static void setOption(String name, String value) {
        Settings.getInstance().setOption(name, value);
    }

	/**
	 *
	 * @param configFile
	 * @param c
	 * @return
	 */
	public static MasterServer start(String configFile, Container c) {
        Settings.setConfigFile(configFile);
        return startUP(c);
    }

    private static MasterServer startUP(Container c) {

        Settings settings = Settings.getInstance();
        if (c != null) {
            settings.setOption(ConfigurationKeys.KEY_GUI_MODE, "true");
        } else {
            settings.setOption(ConfigurationKeys.KEY_GUI_MODE, "false");
        }

        // Codebase used for dynamic class loading during runtime.
        System.setProperty("java.rmi.server.codebase", settings.getCodebase());
        // Path to the security manager configuration file.
        System.setProperty("java.security.policy", settings.getPolicyName());
        System.out.println("Using " + System.getProperty("java.security.policy") + " as policy file location!");
        String tmp = System.getProperty("java.class.path");
        System.setProperty("java.class.path", tmp + System.getProperty("path.seperator") + settings.getCodebase());

        // If there is no security manager, create one.
        if (System.getSecurityManager() == null) {
            System.out.println("Installing new SecurityManager.");
            System.setSecurityManager(new SecurityManager());
        }

        // Start MasterServer
        MasterServer ms = new MasterServer(c);
        return ms;
    }
}
