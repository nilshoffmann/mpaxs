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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * First argument is ip of master server, second one is port number of RMI port.
 *
 * @author Kai Bernd Stadermann
 */
public class StartUp {

	/**
	 *
	 * @param cfg
	 */
	public StartUp(Configuration cfg) {
		Settings settings = new Settings(cfg);
		try {
			System.setProperty("java.rmi.server.codebase",
				settings.getCodebase().toString());
			Logger.getLogger(StartUp.class.getName()).
				log(Level.INFO, "RMI Codebase at {0}", settings.getCodebase().
					toString());
		} catch (MalformedURLException ex) {
			Logger.getLogger(StartUp.class.getName()).log(Level.SEVERE,
				null,
				ex);
		}
		File policyFile;
		policyFile = new File(new File(
			settings.getOption(ConfigurationKeys.KEY_COMPUTE_HOST_WORKING_DIR)),
			settings.getPolicyName());
		if (!policyFile.exists()) {
			System.out.println(
				"Did not find security policy, will create default one!");
			policyFile.getParentFile().mkdirs();
			BufferedReader br = new BufferedReader(
				new InputStreamReader(
					StartUp.class.getResourceAsStream(
						"/net/sf/mpaxs/spi/computeHost/wideopen.policy")));
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(
					policyFile));
				String s = null;
				while ((s = br.readLine()) != null) {
					bw.write(s + "\n");
				}
				bw.flush();
				bw.close();
				br.close();
				Logger.getLogger(StartUp.class.getName()).
					log(Level.INFO,
						"Using security policy at " + policyFile.getAbsolutePath());
			} catch (IOException ex) {
				Logger.getLogger(StartUp.class.getName()).log(
					Level.SEVERE, null, ex);
			}
		} else {
			Logger.getLogger(StartUp.class.getName()).
				log(Level.INFO,
					"Found existing policy file at " + policyFile.getAbsolutePath());
		}
		System.setProperty("java.security.policy", policyFile.getAbsolutePath());

		System.setProperty("java.net.preferIPv4Stack", "true");

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		Logger.getLogger(StartUp.class.getName()).
			log(Level.FINE, "Creating host");
		Host h = new Host();
		Logger.getLogger(StartUp.class.getName()).
			log(Level.FINE, "Configuring host");
		h.configure(cfg);
		Logger.getLogger(StartUp.class.getName()).
			log(Level.FINE, "Setting auth token " + settings.getOption(
					ConfigurationKeys.KEY_AUTH_TOKEN));
		String at = settings.getOption(
			ConfigurationKeys.KEY_AUTH_TOKEN);
		h.setAuthenticationToken(UUID.fromString(at));
		Logger.getLogger(StartUp.class.getName()).
			log(Level.INFO, "Starting host {0}", settings.getHostID());
		h.startComputeHost();
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String args[]) {
		FileHandler handler;
		try {
			handler = new FileHandler("computeHost.log");
			Logger logger = Logger.getLogger(StartUp.class.getName());
			logger.addHandler(handler);
		} catch (IOException ex) {
			Logger.getLogger(StartUp.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SecurityException ex) {
			Logger.getLogger(StartUp.class.getName()).log(Level.SEVERE, null, ex);
		}

		Options options = new Options();
		Option[] optionArray = new Option[]{OptionBuilder.withArgName(
			"configuration").
			hasArg().isRequired().withDescription(
			"URL to configuration file for compute host").create("c")};
		for (Option opt : optionArray) {
			options.addOption(opt);
		}
		if (args.length == 0) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp(
				StartUp.class.getCanonicalName(), options,
				true);
			System.exit(1);
		}
		GnuParser gp = new GnuParser();
		try {
			CommandLine cl = gp.parse(options, args);
			try {
				URL configURL = new URL(cl.getOptionValue("c"));
				PropertiesConfiguration cfg = new PropertiesConfiguration(
					configURL);
				StartUp su = new StartUp(cfg);
			} catch (ConfigurationException ex) {
				Logger.getLogger(StartUp.class.getName()).
					log(Level.SEVERE, null, ex);
			} catch (MalformedURLException ex) {
				Logger.getLogger(StartUp.class.getName()).
					log(Level.SEVERE, null, ex);
			}

		} catch (ParseException ex) {
			Logger.getLogger(StartUp.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
}
