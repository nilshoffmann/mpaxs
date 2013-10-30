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
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.Impaxs;
import net.sf.mpaxs.api.event.IJobEventListener;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Progress;
import net.sf.mpaxs.api.job.Status;
import net.sf.mpaxs.spi.server.settings.Settings;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Local endpoint implementation for direct use in APIs
 *
 * @author Kai Bernd Stadermann
 */
public class MpaxsImpl implements Impaxs {

	MasterServer master;

	@Override
	public void startMasterServer() {
		if (master != null) {
			throw new IllegalStateException("Master server was already started!");
		}
		master = StartUp.start();
	}

	@Override
	public void startMasterServer(String configFile) {
		if (master != null) {
			throw new IllegalStateException("Master server was already started!");
		}
		master = StartUp.start(configFile, null);
	}

	@Override
	public void startMasterServer(String configFile, Container c) {
		if (master != null) {
			throw new IllegalStateException("Master server was already started!");
		}
		master = StartUp.start(configFile, c);
	}

	@Override
	public void startMasterServer(Container c) {
		if (master != null) {
			throw new IllegalStateException("Master server was already started!");
		}
		master = StartUp.start(null, c);
	}

	@Override
	public void startMasterServer(Configuration config, Container c) {
		if (master != null) {
			throw new IllegalStateException("Master server was already started!");
		}
		if (config == null) {
			System.out.println("Configuration is null, starting master with default parameters!");
			startMasterServer();
			return;
		}
		try {
			File f = File.createTempFile(UUID.randomUUID().toString(),
				".properties");
			PropertiesConfiguration pc;
			try {
				pc = new PropertiesConfiguration(f);
				ConfigurationUtils.copy(config, pc);
				pc.save(f);
				System.out.println(ConfigurationUtils.toString(pc));
				master = StartUp.start(f.getAbsolutePath(), c);
			} catch (ConfigurationException ex) {
				Logger.getLogger(MpaxsImpl.class.getName()).
					log(Level.SEVERE, null, ex);
			}
		} catch (IOException ex) {
			Logger.getLogger(MpaxsImpl.class.getName()).log(Level.SEVERE, null,
				ex);
		}

	}

	@Override
	public void startMasterServer(Configuration config) {
		startMasterServer(config, null);
	}

	@Override
	public void submitJob(IJob job) {
		master.submitJob(job);
	}

	@Override
	public void submitScheduledJob(IJob job, long timeUntilStart, long scheduleAt, TimeUnit timeUnit) {
		master.submitJob(job, timeUntilStart, scheduleAt, timeUnit);
	}

	@Override
	public Progress getJobProgress(UUID jobId) {
		return master.getJobProgress(jobId);
	}

	@Override
	public void addJobEventListener(IJobEventListener listener) {
		master.addListener(listener);
	}

	@Override
	public void removeJobEventListener(IJobEventListener listener) {
		master.removeListener(listener);
	}

	@Override
	public void addJobEventListener(IJobEventListener listener, UUID jobId) {
		master.addListener(listener, jobId);
	}

	@Override
	public void removeJobEventListener(IJobEventListener listener, UUID jobId) {
		master.removeListener(listener, jobId);
	}

	@Override
	public void stopMasterServer() {
		master.shutdown();
		master = null;
	}

	@Override
	public UUID getAuthenticationToken() {
		return UUID.fromString(Settings.getInstance().getString(
			ConfigurationKeys.KEY_AUTH_TOKEN));
	}

	@Override
	public boolean cancelJob(UUID jobId) {
		return master.cancelJob(jobId);
	}

	@Override
	public void resubmitJob(IJob job) {
		master.cancelJob(job.getId());
		job.setStatus(Status.UNKNOWN);
		job.setThrowable(null);
		master.submitJob(job);
	}

}
