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
package net.sf.mpaxs.api.server;

import net.sf.mpaxs.api.ExecutionType;
import org.apache.commons.configuration.Configuration;

/**
 * Interface for compute host launcher implementations.
 *
 * @author Nils Hoffmann
 * @see net.sf.mpaxs.api.ExecutionType
 */
public interface IComputeHostLauncher {

	/**
	 * Return the sole execution type that this launcher supports.
	 *
	 * @return the execution type
	 */
	ExecutionType getExecutionType();

	/**
	 * Launch the compute host. This method will be executed on a thread, so
	 * it can block.
	 *
	 * @param cfg the configuration for the compute host
	 */
	void startComputeHost(Configuration cfg);
}
