/*
 * Mpaxs, modular parallel execution system.
 * Copyright (C) 2010-2013, The authors of Mpaxs. All rights reserved.
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
package net.sf.mpaxs.api;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import net.sf.mpaxs.api.computeHost.IRemoteHost;
import net.sf.mpaxs.api.server.IComputeHostLauncher;

/**
 * Factory for the retrieval of implementations of compute host and compute
 * server providers.
 *
 * @author Nils Hoffmann
 */
public class ExecutionFactory {

	/**
	 * The first compute server provider available.
	 *
	 * @return the first compute server provider
	 */
	public static Impaxs getDefaultComputeServer() {
		return getComputeServerProviders().get(0);
	}

	/**
	 * The first compute host provider available.
	 *
	 * @return the first compute host provider
	 */
	public static IRemoteHost getDefaultComputeHost() {
		return getComputeHostProviders().get(0);
	}

	/**
	 * The first compute host launcher available.
	 *
	 * @return the first local compute host launcher available
	 */
	public static IComputeHostLauncher getDefaultComputeHostLauncher() {
		return getComputeHostLaunchers(ExecutionType.LOCAL).get(0);
	}

	/**
	 * The list of available compute server providers.
	 *
	 * @return the list of compute server providers
	 */
	public static List<Impaxs> getComputeServerProviders() {
		return getServiceProviders(Impaxs.class);
	}

	/**
	 * The list of available compute host providers.
	 *
	 * @return the list of compute host providers
	 */
	public static List<IRemoteHost> getComputeHostProviders() {
		return getServiceProviders(IRemoteHost.class);
	}

	/**
	 * Returns the list of available compute host launchers for the given execution type.
	 *
	 * @param et the requested execution type
	 * @return a list of compute host launchers for the given execution type
	 */
	public static List<IComputeHostLauncher> getComputeHostLaunchers(ExecutionType et) {
		List<IComputeHostLauncher> l = getServiceProviders(IComputeHostLauncher.class);
		//System.out.println("Retrieving computeHost providers: "+l);
		LinkedList<IComputeHostLauncher> retl = new LinkedList<IComputeHostLauncher>();
		for (IComputeHostLauncher ichl : l) {
			System.out.println("Checking compute host provider " + ichl.getClass().getSimpleName());
			if (ichl.getExecutionType().equals(et)) {
				System.out.println("Provided execution type: " + ichl.getExecutionType() + "; required execution type: " + et);
				retl.add(ichl);
			}
		}
		if (l.isEmpty()) {
			throw new RuntimeException("Could not retrieve IComputeHostLauncher for execution type " + et);
		}
		return retl;
	}

	/**
	 * Available service providers for the given class.
	 *
	 * @param <T> generic type class for the requested service provider
	 * @param c   the class for the requested service provider
	 * @return a list of available service providers
	 */
	public static <T> List<T> getServiceProviders(Class<T> c) {
		System.out.println("Loading service providers for " + c.getSimpleName());
		ServiceLoader<T> sl = ServiceLoader.load(c);
		Iterator<T> iter = sl.iterator();
		List<T> l = new LinkedList<T>();
		while (iter.hasNext()) {
			T t = iter.next();
			System.out.println("Adding service provider " + t.getClass().getName() + " for interface " + c.getName());
			l.add(t);
		}
		return l;
	}

}
