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
package net.sf.mpaxs.spi.concurrent;

import net.sf.mpaxs.api.ExecutionFactory;
import net.sf.mpaxs.api.Impaxs;

/**
 * The Factory attempts to create a new instance of Impaxs,
 * if such an instance does not exist.
 *
 * @author Nils Hoffmann
 */
public class ComputeServerFactory {

	private static Impaxs imp = null;

	private ComputeServerFactory() {
	}

	/**
	 * Retrieve the current <code>Impaxs</code> implementation.
	 * If <code>false</code> is given, may return <code>null</code>.
	 * Otherwise, a new instance of the default <code>Impaxs</code> implementation
	 * is created and returned.
	 *
	 * @param autoCreate
	 * @return the default impxs implementation or null
	 */
	public static Impaxs getComputeServer(boolean autoCreate) {
		if (autoCreate) {
			return getComputeServer();
		}
		return imp;
	}

	/**
	 * Retrieve or create the singleton <code>Impaxs</code> implementation.
	 *
	 * @return the default impxs implementation
	 */
	public static Impaxs getComputeServer() {
		if (imp == null) {
			imp = ExecutionFactory.getDefaultComputeServer();
		}
		return imp;
	}

}
