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
import javax.management.*;
import java.util.Arrays;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.api.job.Progress;
import net.sf.mpaxs.spi.server.Host;
import net.sf.mpaxs.spi.server.MasterServer;
import net.sf.mpaxs.spi.server.MyConcurrentLinkedJobQueue;

/**
 * Class Server
 *
 * @author nilshoffmann
 */
public class Server extends StandardMBean implements ServerMBean {
    private MasterServer theRef;

    public Server(MasterServer theRef) throws NotCompliantMBeanException {
        //WARNING Uncomment the following call to super() to make this class compile (see BUG ID 122377)
        super(ServerMBean.class);
        this.theRef = theRef;
    }
    
    @Override
    public MBeanInfo getMBeanInfo() {
        MBeanInfo mbinfo = super.getMBeanInfo();
        return new MBeanInfo(mbinfo.getClassName(),
                mbinfo.getDescription(),
                mbinfo.getAttributes(),
                mbinfo.getConstructors(),
                mbinfo.getOperations(),
                getNotificationInfo());
    }
    
    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[] {};
    }

    /**
     * Override customization hook:
     * You can supply a customized description for MBeanInfo.getDescription()
     */
    @Override
    protected String getDescription(MBeanInfo info) {
        return "Server Description";
    }

    /**
     * Override customization hook:
     * You can supply a customized description for MBeanAttributeInfo.getDescription()
     */
    @Override
    protected String getDescription(MBeanAttributeInfo info) {
        String description = null;
        if (info.getName().equals("CanceledJobs")) {
            description = "Attribute exposed for management";
        } else if (info.getName().equals("DoneJobs")) {
            description = "Attribute exposed for management";
        } else if (info.getName().equals("FailedJobs")) {
            description = "Attribute exposed for management";
        } else if (info.getName().equals("Hosts")) {
            description = "Attribute exposed for management";
        } else if (info.getName().equals("PendingJobs")) {
            description = "Attribute exposed for management";
        } else if (info.getName().equals("RunningJobs")) {
            description = "Attribute exposed for management";
        } else if (info.getName().equals("UndoneJob")) {
            description = "Attribute exposed for management";
        }
        return description;
    }

    /**
     * Override customization hook:
     * You can supply a customized description for MBeanParameterInfo.getDescription()
     */
    @Override
    protected String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        if (op.getName().equals("cancelJob")) {
            switch (sequence) {
                case 0:
                    return "";
                default:
                    return null;
            }
        } else if (op.getName().equals("findJob")) {
            switch (sequence) {
                case 0:
                    return "";
                default:
                    return null;
            }
        } else if (op.getName().equals("getHost")) {
            switch (sequence) {
                case 0:
                    return "";
                default:
                    return null;
            }
        } else if (op.getName().equals("getHostJobIsRunningOn")) {
            switch (sequence) {
                case 0:
                    return "";
                default:
                    return null;
            }
        } else if (op.getName().equals("getJobProgress")) {
            switch (sequence) {
                case 0:
                    return "";
                default:
                    return null;
            }
        } else if (op.getName().equals("removeHost")) {
            switch (sequence) {
                case 0:
                    return "";
                default:
                    return null;
            }
        } else if (op.getName().equals("shutdown")) {
            switch (sequence) {
                default:
                    return null;
            }
        } else if (op.getName().equals("shutdownHost")) {
            switch (sequence) {
                case 0:
                    return "";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * Override customization hook:
     * You can supply a customized description for MBeanParameterInfo.getName()
     */
    @Override
    protected String getParameterName(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        if (op.getName().equals("cancelJob")) {
            switch (sequence) {
                case 0:
                    return "param0";
                default:
                    return null;
            }
        } else if (op.getName().equals("findJob")) {
            switch (sequence) {
                case 0:
                    return "param0";
                default:
                    return null;
            }
        } else if (op.getName().equals("getHost")) {
            switch (sequence) {
                case 0:
                    return "param0";
                default:
                    return null;
            }
        } else if (op.getName().equals("getHostJobIsRunningOn")) {
            switch (sequence) {
                case 0:
                    return "param0";
                default:
                    return null;
            }
        } else if (op.getName().equals("getJobProgress")) {
            switch (sequence) {
                case 0:
                    return "param0";
                default:
                    return null;
            }
        } else if (op.getName().equals("removeHost")) {
            switch (sequence) {
                case 0:
                    return "param0";
                default:
                    return null;
            }
        } else if (op.getName().equals("shutdown")) {
            switch (sequence) {
                default:
                    return null;
            }
        } else if (op.getName().equals("shutdownHost")) {
            switch (sequence) {
                case 0:
                    return "param0";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * Override customization hook:
     * You can supply a customized description for MBeanOperationInfo.getDescription()
     */
    @Override
    protected String getDescription(MBeanOperationInfo info) {
        String description = null;
        MBeanParameterInfo[] params = info.getSignature();
        String[] signature = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            signature[i] = params[i].getType();
        }
        String[] methodSignature;
        methodSignature = new String[]{java.util.UUID.class.getName()};
        if (info.getName().equals("cancelJob") && Arrays.equals(signature, methodSignature)) {
            description = "Operation exposed for management";
        }
        methodSignature = new String[]{java.util.UUID.class.getName()};
        if (info.getName().equals("findJob") && Arrays.equals(signature, methodSignature)) {
            description = "Operation exposed for management";
        }
        methodSignature = new String[]{java.util.UUID.class.getName()};
        if (info.getName().equals("getHost") && Arrays.equals(signature, methodSignature)) {
            description = "Operation exposed for management";
        }
        methodSignature = new String[]{java.util.UUID.class.getName()};
        if (info.getName().equals("getHostJobIsRunningOn") && Arrays.equals(signature, methodSignature)) {
            description = "Operation exposed for management";
        }
        methodSignature = new String[]{java.util.UUID.class.getName()};
        if (info.getName().equals("getJobProgress") && Arrays.equals(signature, methodSignature)) {
            description = "Operation exposed for management";
        }
        methodSignature = new String[]{java.util.UUID.class.getName()};
        if (info.getName().equals("removeHost") && Arrays.equals(signature, methodSignature)) {
            description = "Operation exposed for management";
        }
        methodSignature = new String[]{};
        if (info.getName().equals("shutdown") && Arrays.equals(signature, methodSignature)) {
            description = "Operation exposed for management";
        }
        methodSignature = new String[]{java.util.UUID.class.getName()};
        if (info.getName().equals("shutdownHost") && Arrays.equals(signature, methodSignature)) {
            description = "Operation exposed for management";
        }
        return description;
    }

    /**
     * Get Attribute exposed for management
     */
    public HashMap getCanceledJobs() {
        return theRef.getCanceledJobs();
    }

    /**
     * Get Attribute exposed for management
     */
    public HashMap getDoneJobs() {
        return theRef.getDoneJobs();
    }

    /**
     * Get Attribute exposed for management
     */
    public List getFailedJobs() {
        return theRef.getFailedJobs();
    }

    /**
     * Get Attribute exposed for management
     */
    public HashMap getHosts() {
        return theRef.getHosts();
    }

    /**
     * Get Attribute exposed for management
     */
    public MyConcurrentLinkedJobQueue getPendingJobs() {
        return theRef.getPendingJobs();
    }

    /**
     * Get Attribute exposed for management
     */
    public HashMap getRunningJobs() {
        return theRef.getRunningJobs();
    }

    /**
     * Get Attribute exposed for management
     */
    public IJob getUndoneJob() {
        return theRef.getUndoneJob();
    }

    /**
     * Operation exposed for management
     * @param param0
     * @return boolean
     */
    public boolean cancelJob(UUID param0) {
        return theRef.cancelJob(param0);
    }

    /**
     * Operation exposed for management
     * @param param0
     * @return net.sf.maltcms.execution.api.IJob
     */
    public IJob findJob(UUID param0) {
        return theRef.findJob(param0);
    }

    /**
     * Operation exposed for management
     * @param param0
     * @return net.sf.maltcms.execution.masterServer.Host
     */
    public Host getHost(UUID param0) {
        return theRef.getHost(param0);
    }

    /**
     * Operation exposed for management
     * @param param0
     * @return net.sf.maltcms.execution.masterServer.Host
     */
    public Host getHostJobIsRunningOn(UUID param0) {
        return theRef.getHostJobIsRunningOn(param0);
    }

    /**
     * Operation exposed for management
     * @param param0
     * @return net.sf.maltcms.execution.api.Progress
     */
    public Progress getJobProgress(UUID param0) {
        return theRef.getJobProgress(param0);
    }

    /**
     * Operation exposed for management
     * @param param0
     * @return boolean
     */
    public boolean removeHost(UUID param0) {
        return theRef.removeHost(param0);
    }

    /**
     * Operation exposed for management
     */
    public void shutdown() {
        theRef.shutdown();
    }

    /**
     * Operation exposed for management
     * @param param0
     */
    public void shutdownHost(UUID param0) {
        theRef.shutdownHost(param0);
    }
}


