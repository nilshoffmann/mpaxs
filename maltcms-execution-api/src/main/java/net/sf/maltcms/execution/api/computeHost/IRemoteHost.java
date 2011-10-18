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

package net.sf.maltcms.execution.api.computeHost;

import net.sf.maltcms.execution.api.job.IJob;
import java.rmi.Remote;
import java.util.UUID;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author nilshoffmann
 */
public interface IRemoteHost extends Thread.UncaughtExceptionHandler{

    /**
     * Meldet diesen Host vom Server ab.
     * @return true = erfolgreich abgemeldet, false = Abmeldung fehlgeschlagen
     */
    boolean disconnectFromMasterServer();

    void sendDoneJob(IJob job);

    /**
     * Meldet das RemoteObject ab und schließt danach das Programm.
     * @param obj RemoteObject
     */
    void shutdown(Remote obj);

    @Override
    void uncaughtException(Thread t, Throwable e);

    /**
     * Sets the authentication token required to connect to the correct
     * master server.
     * 
     * @param authToken
     */
    void setAuthenticationToken(UUID authToken);

    UUID getAuthenticationToken();

    void startComputeHost();
    
    void configure(Configuration cfg);

}
