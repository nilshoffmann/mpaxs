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
package net.sf.maltcms.execution.masterServer;

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
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.masterServer.jmx.Server;
import net.sf.maltcms.execution.masterServer.settings.Settings;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class StartUp {

    /** Starts the MasterServer.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        startUP(null);
    }

    public static MasterServer start() {
        return startUP(null);
    }

    public static MasterServer start(String configFile) {
        Settings.setConfigFile(configFile);
        return startUP(null);
    }
    
    public static void setOption(String name, String value) {
        Settings.getInstance().setOption(name, value);
    }

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
//        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
//
//        // Construct the ObjectName for the MBean we will register
//        ObjectName name;
//        try {
//            name = new ObjectName("net.sf.maltcms.execution.masterServer.jmx:type=Server");
//            // Create the Hello World MBean
//            Server mbean = new Server(ms);
//
//            // Register the Hello World MBean
//            mbs.registerMBean(mbean, name);
//        } catch (InstanceAlreadyExistsException ex) {
//            Logger.getLogger(StartUp.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (MBeanRegistrationException ex) {
//            Logger.getLogger(StartUp.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NotCompliantMBeanException ex) {
//            Logger.getLogger(StartUp.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (MalformedObjectNameException ex) {
//            Logger.getLogger(StartUp.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NullPointerException ex) {
//            Logger.getLogger(StartUp.class.getName()).log(Level.SEVERE, null, ex);
//        }

        return ms;
    }
}
