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
package net.sf.mpaxs.api.job;

import net.sf.mpaxs.api.concurrent.ConfigurableRunnable;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.UUID;

/**
 * T must implement Serializable, however, this is not enforceable 
 * within the api to keep compatibility with JAVA's executor framework.
 * Thus, a runtime exception is thrown whenever a job with a type of T 
 * is created/submitted, which is not Serializable.
 * 
 * @author nilshoffmann
 */
public interface IJob<T> extends Serializable {

    void errorOccurred();

    ConfigurableRunnable<T> getClassToExecute();

    String getConfigurationFile();

    int getErrorCounter();

    UUID getId();

    String getJobConfigFile();

    Status getStatus();

    void setJobConfigFile(String jobConfigFile);

    void setStatus(Status status);
    
    void setClassToExecute(String jobConfigFile) throws ClassNotFoundException,
            MalformedURLException, InstantiationException, IllegalAccessException, IOException;
    
    void setClassToExecute(ConfigurableRunnable<T> cr);
    
    void setThrowable(Throwable t);
    
    Throwable getThrowable();

}
