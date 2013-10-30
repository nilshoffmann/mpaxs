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

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.ICompletionService;
import net.sf.mpaxs.api.Impaxs;
import net.sf.mpaxs.api.job.Job;
import net.sf.mpaxs.spi.concurrent.CompletionServiceFactory;
import net.sf.mpaxs.spi.concurrent.ComputeServerFactory;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author Nils Hoffmann
 */
public class DistributedRmiExecution implements Callable<Double>, Serializable {

	private final int maxJobs;
	private final PropertiesConfiguration cfg;

	/**
	 *
	 * @param cfg
	 * @param maxJobs
	 */
	public DistributedRmiExecution(PropertiesConfiguration cfg, int maxJobs) {
		this.cfg = cfg;
		this.maxJobs = maxJobs;
	}

	@Override
	public Double call() throws Exception {
		/*
		 * Compute Server is only required for VM external execution
		 */
		Impaxs impxs = ComputeServerFactory.getComputeServer();
		if (cfg.getBoolean(ConfigurationKeys.KEY_GUI_MODE, false)) {
			final JFrame jf = new JFrame();
			jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			JPanel controlPanel = new JPanel();
			jf.add(controlPanel);
			controlPanel.setLayout(new BorderLayout());
			impxs.startMasterServer(cfg, controlPanel);
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					jf.setVisible(true);
					jf.pack();
				}
			});
		} else {
			impxs.startMasterServer(cfg);
		}
		CompletionServiceFactory<Double> csf = new CompletionServiceFactory<Double>();
		csf.setBlockingWait(true);
		final ICompletionService<Double> mcs = csf.asResubmissionService(csf.newDistributedCompletionService(), 3);
		for (int i = 0; i < maxJobs; i++) {
			mcs.submit(new TestCallable());
		}
		Job<Boolean> job = new Job<Boolean>(new TestScheduledRunnable(), Boolean.TRUE);
		//increase the priority so that the job can bypass other, waiting jobs
		job.setPriority(job.getPriority() + 1);
		//api submission, this job will be wrapped as a ScheduledJob
		impxs.submitScheduledJob(job, 1, 5, TimeUnit.SECONDS);
		//alternative, direct creation of a ScheduledJob
//		impxs.submitJob(new ScheduledJob(job, 1, 5, TimeUnit.SECONDS));
		double result = 0.0d;
		try {
			List<Double> results = mcs.call();
			System.out.println("Distributed execution: " + results);
			for (Double double1 : results) {
				result += double1;
			}
		} catch (Exception ex) {
			Logger.getLogger(DistributedRmiExecution.class.getName()).
				log(Level.SEVERE, null, ex);
			throw ex;
		}

		CompletionServiceFactory<String> csf2 = new CompletionServiceFactory<String>();
//        csf2.setTimeOut(1);
//        csf2.setTimeUnit(TimeUnit.SECONDS);
		csf2.setBlockingWait(true);
		final ICompletionService<String> mcs2 = csf2.newDistributedCompletionService();
		for (int i = 0; i < maxJobs; i++) {
			mcs2.submit(new TestCallable2());
		}
		try {
			List<String> results = mcs2.call();
			System.out.println("Distributed execution: " + results);
			for (String str : results) {
				System.out.println("Result: " + str);
			}
		} catch (Exception ex) {
			Logger.getLogger(DistributedRmiExecution.class.getName()).
				log(Level.SEVERE, null, ex);
			throw ex;
		}

		final ICompletionService<Double> mcs3 = csf.newDistributedCompletionService();
		for (int i = 0; i < maxJobs; i++) {
			mcs3.submit(new TestCallable());
		}
		try {
			List<Double> results = mcs3.call();
			System.out.println("Distributed execution: " + results);
			for (Double double1 : results) {
				result += double1;
			}
		} catch (Exception ex) {
			Logger.getLogger(DistributedRmiExecution.class.getName()).
				log(Level.SEVERE, null, ex);
			throw ex;
		}

		impxs.stopMasterServer();
		return result;
	}
}
