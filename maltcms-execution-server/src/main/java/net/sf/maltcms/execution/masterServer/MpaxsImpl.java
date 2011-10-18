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
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.maltcms.execution.api.Impaxs;
import java.util.UUID;
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.event.IJobEventListener;
import net.sf.maltcms.execution.api.job.Progress;
import net.sf.maltcms.execution.masterServer.settings.Settings;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Local endpoint implementation for direct use in APIs
 * @author Kai Bernd Stadermann
 */
public class MpaxsImpl implements Impaxs {

    MasterServer master;

    @Override
    public void startMasterServer() {
        if(master!=null) {
            throw new IllegalStateException("Master server was already started!");
        }
        master = StartUp.start();
    }

    @Override
    public void startMasterServer(String configFile) {
        if(master!=null) {
            throw new IllegalStateException("Master server was already started!");
        }
        master = StartUp.start(configFile, null);
    }
    
    @Override
    public void startMasterServer(String configFile, Container c) {
        if(master!=null) {
            throw new IllegalStateException("Master server was already started!");
        }
        master = StartUp.start(configFile, c);
    }

    @Override
    public void startMasterServer(Container c) {
        if(master!=null) {
            throw new IllegalStateException("Master server was already started!");
        }
        master = StartUp.start(null, c);
    }
    
    @Override
    public void startMasterServer(Configuration config, Container c) {
        if(master!=null) {
            throw new IllegalStateException("Master server was already started!");
        }
        if(config==null) {
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
        startMasterServer(config,null);
    }

    @Override
    public void submitJob(IJob job) {
        master.submitJob(job);
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
    public void stopMasterServer() {
        master.shutdown();
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

}
