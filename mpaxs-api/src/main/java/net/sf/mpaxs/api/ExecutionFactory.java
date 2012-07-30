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
package net.sf.mpaxs.api;

import net.sf.mpaxs.api.computeHost.IRemoteHost;
import net.sf.mpaxs.api.server.IComputeHostLauncher;
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
            System.out.println("Checking compute host provider "+ichl.getClass().getSimpleName());
            if(ichl.getExecutionType().equals(et)) {
                System.out.println("Provided execution type: "+ichl.getExecutionType()+"; required execution type: "+et);
                retl.add(ichl);
            }
        }
        if(l.isEmpty()) {
            throw new RuntimeException("Could not retrieve IComputeHostLauncher for execution type "+et);
        }
        return retl;
    }
    
    public static <T> List<T> getServiceProviders(Class<T> c) {
        System.out.println("Loading service providers for "+c.getSimpleName());
        ServiceLoader<T> sl = ServiceLoader.load(c);
        Iterator<T> iter = sl.iterator();
        List<T> l = new LinkedList<T>();
        while(iter.hasNext()) {
            T t = iter.next();
            System.out.println("Adding service provider "+t.getClass().getName()+" for interface "+c.getName());
            l.add(t);
        }
        return l;
    }
    
}
