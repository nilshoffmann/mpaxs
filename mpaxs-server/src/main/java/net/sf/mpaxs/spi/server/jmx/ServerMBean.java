/*
 * Mpaxs, modular parallel execution system. 
 * Copyright (C) 2010-2012, The authors of Mpaxs. All rights reserved.
 *
 * Project Administrator: nilshoffmann A T users.sourceforge.net
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
 * under licenses/ for details.
 */
package net.sf.mpaxs.spi.server.jmx;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Progress;
import net.sf.mpaxs.spi.server.Host;
import net.sf.mpaxs.spi.server.MyConcurrentLinkedJobQueue;

/**
 * Interface ServerMBean
 *
 * @author nilshoffmann
 */
public interface ServerMBean
{

    /**
     * Get Attribute exposed for management
     */
    public HashMap getCanceledJobs();

    /**
     * Get Attribute exposed for management
     */
    public HashMap getDoneJobs();

    /**
     * Get Attribute exposed for management
     */
    public List getFailedJobs();

    /**
     * Get Attribute exposed for management
     */
    public HashMap getHosts();

    /**
     * Get Attribute exposed for management
     */
    public MyConcurrentLinkedJobQueue getPendingJobs();

    /**
     * Get Attribute exposed for management
     */
    public HashMap getRunningJobs();

    /**
     * Get Attribute exposed for management
     */
    public IJob getUndoneJob();

    /**
     * Operation exposed for management
     * @param param0
     * @return boolean
     */
    public boolean cancelJob(UUID param0);

    /**
     * Operation exposed for management
     * @param param0
     * @return net.sf.maltcms.execution.api.IJob
     */
    public IJob findJob(UUID param0);

    /**
     * Operation exposed for management
     * @param param0
     * @return net.sf.maltcms.execution.masterServer.Host
     */
    public Host getHost(UUID param0);

    /**
     * Operation exposed for management
     * @param param0
     * @return net.sf.maltcms.execution.masterServer.Host
     */
    public Host getHostJobIsRunningOn(UUID param0);

    /**
     * Operation exposed for management
     * @param param0
     * @return net.sf.maltcms.execution.api.Progress
     */
    public Progress getJobProgress(UUID param0);

    /**
     * Operation exposed for management
     * @param param0
     * @return boolean
     */
    public boolean removeHost(UUID param0);

    /**
     * Operation exposed for management
     */
    public void shutdown();

    /**
     * Operation exposed for management
     * @param param0
     */
    public void shutdownHost(UUID param0);
    
}


