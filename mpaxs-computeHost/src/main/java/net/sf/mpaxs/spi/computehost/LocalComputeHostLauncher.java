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
package net.sf.mpaxs.spi.computehost;

import java.util.logging.Logger;
import net.sf.mpaxs.api.ExecutionType;
import net.sf.mpaxs.api.server.IComputeHostLauncher;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class LocalComputeHostLauncher implements IComputeHostLauncher {

    @Override
    public ExecutionType getExecutionType() {
        return ExecutionType.LOCAL;
    }

    @Override
    public void startComputeHost(Configuration cfg) {
        Logger.getLogger(getClass().getName()).info("Starting local compute host with configuration: "+ConfigurationUtils.toString(cfg));
        StartUp su = new StartUp(cfg);
    }
    
}
