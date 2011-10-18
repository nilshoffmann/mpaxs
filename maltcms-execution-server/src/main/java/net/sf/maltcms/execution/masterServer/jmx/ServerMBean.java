/*
 * ServerMBean.java
 *
 * Created on 9. Mai 2011, 20:07
 */

package net.sf.maltcms.execution.masterServer.jmx;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.job.Progress;
import net.sf.maltcms.execution.masterServer.Host;
import net.sf.maltcms.execution.masterServer.MyConcurrentLinkedJobQueue;

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


