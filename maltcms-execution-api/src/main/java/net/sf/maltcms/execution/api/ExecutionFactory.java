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

package net.sf.maltcms.execution.api;

import net.sf.maltcms.execution.api.computeHost.IRemoteHost;
import net.sf.maltcms.execution.api.server.IComputeHostLauncher;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

/**
 *
 * @author nilshoffmann
 */
public class ExecutionFactory {

    public static Impaxs getDefaultComputeServer() {
        return getComputeServerProviders().get(0);
    }

    public static IRemoteHost getDefaultComputeHost() {
        return getComputeHostProviders().get(0);
    }
    
    public static IComputeHostLauncher getDefaultComputeHostLauncher() {
        return getComputeHostLaunchers(ExecutionType.LOCAL).get(0);
    }

    public static List<Impaxs> getComputeServerProviders() {
        return getServiceProviders(Impaxs.class);
    }
    
    public static List<IRemoteHost> getComputeHostProviders() {
        return getServiceProviders(IRemoteHost.class);
    }
    
    public static List<IComputeHostLauncher> getComputeHostLaunchers(ExecutionType et) {
        List<IComputeHostLauncher> l = getServiceProviders(IComputeHostLauncher.class);
        //System.out.println("Retrieving computeHost providers: "+l);
        LinkedList<IComputeHostLauncher> retl = new LinkedList<IComputeHostLauncher>();
        for(IComputeHostLauncher ichl:l) {
            if(ichl.getExecutionType().equals(et)) {
                retl.add(ichl);
            }
        }
        return retl;
    }
    
    public static <T> List<T> getServiceProviders(Class<T> c) {
        ServiceLoader<T> sl = ServiceLoader.load(c);
        Iterator<T> iter = sl.iterator();
        List<T> l = new LinkedList<T>();
        while(iter.hasNext()) {
            T t = iter.next();
            //System.out.println("Adding service provider "+t.getClass().getName()+" for interface "+c.getName());
            l.add(t);
        }
        return l;
    }
    
}
