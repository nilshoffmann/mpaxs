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
package net.sf.maltcms.execution.spi.server;

import net.sf.maltcms.execution.api.ExecutionType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.api.server.IComputeHostLauncher;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class DrmaaComputeHostLauncher implements IComputeHostLauncher {

    /**
     * Submits a new ComputeHost to the GridEngine.
     * Settings from the Settings class are used.
     */
    @Override
    public void startComputeHost(Configuration cfg) {
        List<String> arguments = new ArrayList<String>();
        arguments.add("-cp");
        arguments.add(System.getProperty("java.class.path"));
        arguments.add("-jar");
        arguments.add(cfg.getString(
                ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR));
        arguments.add(cfg.getString(ConfigurationKeys.KEY_LOCAL_IP));
        arguments.add(cfg.getInt(ConfigurationKeys.KEY_LOCAL_PORT) + "");
//        settings.setOption("authToken", authToken.toString());
        //arguments.add(cfg.getString(ConfigurationKeys.KEY_AUTH_TOKEN));
        Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                "ComputeHost configuration: {}", ConfigurationUtils.toString(cfg));
        try {
            SessionFactory factory = SessionFactory.getFactory();
            Session session = factory.getSession();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, session.
                    getDrmSystem());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, session.
                    getDrmaaImplementation());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, session.
                    getVersion().toString());
            session.init("");
            JobTemplate jt = session.createJobTemplate();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "Remote command: " + cfg.getString(
                    ConfigurationKeys.KEY_PATH_TO_JAVA));
            jt.setRemoteCommand(
                    cfg.getString(ConfigurationKeys.KEY_PATH_TO_JAVA));
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "Working dir: " + cfg.getString(
                    ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR));
            jt.setWorkingDirectory(cfg.getString(
                    ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR));
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "Arguments: " + arguments);
            jt.setArgs(arguments);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "Error path: " + cfg.getString(
                    ConfigurationKeys.KEY_ERROR_FILE));
            jt.setErrorPath(
                    ":" + cfg.getString(ConfigurationKeys.KEY_ERROR_FILE));
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "Output path: " + cfg.getString(
                    ConfigurationKeys.KEY_OUTPUT_FILE));
            jt.setOutputPath(":" + cfg.getString(
                    ConfigurationKeys.KEY_OUTPUT_FILE));
            jt.setNativeSpecification(cfg.getString(
                    ConfigurationKeys.KEY_NATIVE_SPEC));
            session.runJob(jt);
            session.deleteJobTemplate(jt);
            session.exit();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "Session started!");
        } catch (DrmaaException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null,
                    ex);
        }

    }

    @Override
    public ExecutionType getExecutionType() {
        return ExecutionType.DRMAA;
    }
}
