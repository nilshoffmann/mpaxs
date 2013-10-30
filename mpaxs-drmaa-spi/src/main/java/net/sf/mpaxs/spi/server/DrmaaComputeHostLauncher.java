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

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.ExecutionType;
import net.sf.mpaxs.api.server.IComputeHostLauncher;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
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
	 * Settings from the Settings class are used and converted to <code>Configuration</code>.
	 *
	 * @param cfg the configuration to use
	 * @see net.sf.mpaxs.spi.computeHost.Settings
	 */
	@Override
	public void startComputeHost(Configuration cfg) {
		String drmaaImplementation = SessionFactory.getFactory().getSession().getDrmaaImplementation();
		System.out.println("Drmaa Implementation: " + drmaaImplementation);
		File configLocation = new File(cfg.getString(
			ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR), "computeHost.properties");
		try {
			PropertiesConfiguration pc = new PropertiesConfiguration(configLocation);
			ConfigurationUtils.copy(cfg, pc);
			pc.save();
		} catch (ConfigurationException ex) {
			Logger.getLogger(DrmaaComputeHostLauncher.class.getName()).log(Level.SEVERE, null, ex);
		}

		List<String> arguments = new ArrayList<String>();
		arguments.add("-cp");
		arguments.add(cfg.getString(
			ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR));
		arguments.add(cfg.getString(
			ConfigurationKeys.KEY_COMPUTE_HOST_MAIN_CLASS));
		arguments.add("-c");
		try {
			arguments.add(configLocation.toURI().toURL().toString());
		} catch (MalformedURLException ex) {
			Logger.getLogger(DrmaaComputeHostLauncher.class.getName()).log(Level.SEVERE, null, ex);
		}
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "ComputeHost configuration: {0}", ConfigurationUtils.toString(cfg));
		try {
			SessionFactory factory = SessionFactory.getFactory();
			Session session = factory.getSession();
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DRM System: {0} Implementation: {1} Version: {2}", new Object[]{session.
				getDrmSystem(), session.getDrmaaImplementation(), session.getVersion()});
			session.init("");
			JobTemplate jt = session.createJobTemplate();
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Remote command: {0}", cfg.getString(
				ConfigurationKeys.KEY_PATH_TO_JAVA));
			jt.setRemoteCommand(cfg.getString(ConfigurationKeys.KEY_PATH_TO_JAVA));
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Working dir: {0}", cfg.getString(
				ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR));
			jt.setWorkingDirectory(cfg.getString(
				ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR));
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Arguments: {0}", arguments);
			jt.setArgs(arguments);
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error path: {0}", cfg.getString(
				ConfigurationKeys.KEY_ERROR_FILE));
			jt.setErrorPath(
				":" + cfg.getString(ConfigurationKeys.KEY_ERROR_FILE));
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Output path: {0}", cfg.getString(
				ConfigurationKeys.KEY_OUTPUT_FILE));
			jt.setOutputPath(":" + cfg.getString(
				ConfigurationKeys.KEY_OUTPUT_FILE));
			jt.setNativeSpecification(cfg.getString(
				ConfigurationKeys.KEY_NATIVE_SPEC, ""));
			jt.setJobName("mpaxs-chost");
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

	/**
	 * Returns the execution type supported by this launcher.
	 *
	 * @return the execution type
	 */
	@Override
	public ExecutionType getExecutionType() {
		return ExecutionType.DRMAA;
	}
}
