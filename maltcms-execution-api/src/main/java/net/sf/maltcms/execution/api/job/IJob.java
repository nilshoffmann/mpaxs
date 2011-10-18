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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.maltcms.execution.api.job;

import net.sf.maltcms.execution.api.concurrent.ConfigurableRunnable;
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
