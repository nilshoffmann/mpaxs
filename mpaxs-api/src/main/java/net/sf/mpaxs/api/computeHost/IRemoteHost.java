/*
 * Mpaxs, modular parallel execution system. 
 * Copyright (C) 2010-2012, The authors of Mpaxs. All rights reserved.
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
package net.sf.mpaxs.api.computeHost;

import java.rmi.Remote;
import java.util.UUID;
import net.sf.mpaxs.api.job.IJob;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author Nils Hoffmann
 */
public interface IRemoteHost extends Thread.UncaughtExceptionHandler{

    /**
     * Meldet diesen Host vom Server ab.
     * @return true = erfolgreich abgemeldet, false = Abmeldung fehlgeschlagen
     */
    boolean disconnectFromMasterServer();

    /**
     *
     * @param job
     */
    void sendDoneJob(IJob job);

    /**
     * Meldet das RemoteObject ab und schließt danach das Programm.
     * @param obj RemoteObject
     */
    void shutdown(Remote obj);

    /**
     *
     * @param t
     * @param e
     */
    @Override
    void uncaughtException(Thread t, Throwable e);

    /**
     * Sets the authentication token required to connect to the correct
     * master server.
     * 
     * @param authToken
     */
    void setAuthenticationToken(UUID authToken);

    /**
     *
     * @return
     */
    UUID getAuthenticationToken();

    /**
     *
     */
    void startComputeHost();
    
    /**
     *
     * @param cfg
     */
    void configure(Configuration cfg);

}
