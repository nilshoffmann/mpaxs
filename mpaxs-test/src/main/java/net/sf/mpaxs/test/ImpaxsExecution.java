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
package net.sf.mpaxs.test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.ExecutionType;
import net.sf.mpaxs.spi.computeHost.StartUp;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author Nils Hoffmann
 */
public class ImpaxsExecution {

	/**
	 *
	 */
	public final static String eightyHashes = "################################################################################";
	/**
	 *
	 */
	public final static String hash = "#";

	/**
	 *
	 */
	public enum Mode {

		/**
		 *
		 */
		ALL,

		/**
		 *
		 */
		LOCAL,

		/**
		 *
		 */
		DISTRIBUTED
	};

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();
		Option[] optionArray = new Option[]{
			OptionBuilder.withArgName("nhosts").
			hasArg().withDescription("Number of hosts for parallel processing").create("n"),
			OptionBuilder.withArgName("mjobs").
			hasArg().withDescription("Number of jobs to run in parallel").create("m"),
			OptionBuilder.withArgName("runmode").
			hasArg().withDescription("The mode in which to operate: one of <ALL,LOCAL,DISTRIBUTED>").create("r"), //            OptionBuilder.withArgName("gui").
		//            withDescription("Create gui for distributed execution").create("g")
		};
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
		int nhosts = 1;
		int mjobs = 10;
		boolean gui = false;
		Mode mode = Mode.ALL;
		try {
			CommandLine cl = gp.parse(options, args);
			if (cl.hasOption("n")) {
				nhosts = Integer.parseInt(cl.getOptionValue("n"));
			}
			if (cl.hasOption("m")) {
				mjobs = Integer.parseInt(cl.getOptionValue("m"));
			}
			if (cl.hasOption("r")) {
				mode = Mode.valueOf(cl.getOptionValue("r"));
			}
//            if (cl.hasOption("g")) {
//                gui = true;
//            }
		} catch (Exception ex) {
			Logger.getLogger(StartUp.class.getName()).log(Level.SEVERE, null, ex);
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp(
					StartUp.class.getCanonicalName(), options,
					true);
			System.exit(1);
		}

		String version;
		try {
			version = net.sf.mpaxs.api.Version.getVersion();
			System.out.println("Running mpaxs " + version);
			File computeHostJarLocation = new File(System.getProperty("user.dir"), "mpaxs.jar");
			if (!computeHostJarLocation.exists() || !computeHostJarLocation.isFile()) {
				throw new IOException("Could not locate mpaxs.jar in " + System.getProperty("user.dir"));
			}
			final PropertiesConfiguration cfg = new PropertiesConfiguration();
			//set default execution type
			cfg.setProperty(ConfigurationKeys.KEY_EXECUTION_MODE, ExecutionType.DRMAA);
			//set location of compute host jar
			cfg.setProperty(ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR, computeHostJarLocation);
			//do not exit to console when master server shuts down
			cfg.setProperty(ConfigurationKeys.KEY_MASTER_SERVER_EXIT_ON_SHUTDOWN, false);
			//limit the number of used compute hosts
			cfg.setProperty(ConfigurationKeys.KEY_MAX_NUMBER_OF_CHOSTS, nhosts);
			cfg.setProperty(ConfigurationKeys.KEY_NATIVE_SPEC, "");
			cfg.setProperty(ConfigurationKeys.KEY_GUI_MODE, gui);
			cfg.setProperty(ConfigurationKeys.KEY_SILENT_MODE, true);
			cfg.setProperty(ConfigurationKeys.KEY_SCHEDULE_WAIT_TIME, "500");
			final int maxJobs = mjobs;
			final int maxThreads = nhosts;
			final Mode runMode = mode;
			printMessage("Run mode: " + runMode);
			Executors.newSingleThreadExecutor().submit(new Runnable() {
				@Override
				public void run() {
					if (runMode == Mode.ALL || runMode == Mode.LOCAL) {
						printMessage("Running Within VM Execution");
						/*
						 * LOCAL within VM execution
						 */
						WithinVmExecution lhe = new WithinVmExecution(maxJobs, maxThreads);
						try {
							Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.INFO, "Sum is: " + lhe.call());
						} catch (Exception ex) {
							Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
						}
					}

					if (runMode == Mode.ALL || runMode == Mode.DISTRIBUTED) {
						printMessage("Running Distributed Host RMI Execution");
						/*
						 * Grid Engine (DRMAA API) or local host distributed RMI execution
						 */
						DistributedRmiExecution de = new DistributedRmiExecution(cfg, maxJobs);
						try {
							Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.INFO, "Sum is: " + de.call());
						} catch (Exception ex) {
							Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
					System.exit(0);
				}
			});
		} catch (IOException ex) {
			Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 *
	 * @param message
	 */
	public static void printMessage(String message) {
		System.out.println(eightyHashes);
		System.out.println(hash + " " + message);
		System.out.println(eightyHashes);
	}
}
