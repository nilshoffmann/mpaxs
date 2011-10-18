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
package net.sf.maltcms.execution.computehost;

import java.util.logging.Logger;
import net.sf.maltcms.execution.api.ExecutionType;
import net.sf.maltcms.execution.api.server.IComputeHostLauncher;
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
