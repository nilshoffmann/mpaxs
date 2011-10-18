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
package net.sf.maltcms.execution.api;

import net.sf.maltcms.execution.api.event.IJobEventListener;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.job.Progress;
import java.awt.Container;
import java.util.UUID;
import org.apache.commons.configuration.Configuration;

/**
 * External API to execute Jobs.
 * 
 * @author Kai Bernd Stadermann
 */
public interface Impaxs {

    /**
     * Starts a new instance of the MasterServer.
     * This method does the same thing the main method does.
     */
    public void startMasterServer();

    /**
     * Starts a new instance of the MasterServer which will use the given
     * configuration file.
     * @param configFile configuration file the MasterServer should use.
     */
    public void startMasterServer(String configFile);
    
    /**
     * Starts a new instance of the MasterServer which will use the given
     * configuration object.
     * @param config configuration the MasterServer should use.
     */
    public void startMasterServer(Configuration config);
    
    /**
     * Starts a new instance of the MasterServer which will use the given
     * configuration object.
     * @param config configuration the MasterServer should use.
     * @param c Container to add the MasterServer ui to
     */
    public void startMasterServer(Configuration config, Container c);

    /**
     * Starts a new instance of the MasterServer which will use the given
     * configuration file.
     * @param configFile configuration file the MasterServer should use.
     * @param c Container to add the MasterServer ui to
     */
    public void startMasterServer(String configFile, Container c);

    /**
     * Starts a new instance of the MasterServer which will use the given
     * configuration file.
     * @param c Container to add the MasterServer ui to
     */
    public void startMasterServer(Container c);

    /**
     * Stops the current instance of master server immediately.
     */
    public void stopMasterServer();
    
    /**
     * Submit a new Job.
     * @param job of type shared.Job
     */
    public void submitJob(IJob job);

    /**
     * Return the progress of a job currently beeing computed.
     * @param jobId UUID of the job you want to get the progress from.
     * @return Progress Object of type shared.Progress.
     */
    public Progress getJobProgress(UUID jobId);
    
    /**
     * Cancel the job given by jobId
     * @param jobId
     * @return whether the job was cancelled
     */
    public boolean cancelJob(UUID jobId);

    /**
     * Adds the specified Listener. The Listener will be informed about
     * status changes off all Jobs.
     * @param listener IJobEventListener that should be added.
     */
    public void addJobEventListener(IJobEventListener listener);

    /**
     * Removes the specified Listener.
     * @param listener IJobEventListener that should be removed.
     */
    public void removeJobEventListener(IJobEventListener listener);

    public UUID getAuthenticationToken();

}
