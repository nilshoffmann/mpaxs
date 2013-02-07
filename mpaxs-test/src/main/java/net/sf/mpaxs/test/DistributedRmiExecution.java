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
package net.sf.mpaxs.test;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ICompletionService;
import net.sf.mpaxs.api.Impaxs;
import net.sf.mpaxs.spi.concurrent.CompletionServiceFactory;
import net.sf.mpaxs.spi.concurrent.ComputeServerFactory;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author Nils Hoffmann
 */
public class DistributedRmiExecution implements Callable<Double>, Serializable {

    private final int maxJobs;
    private final PropertiesConfiguration cfg;

    public DistributedRmiExecution(PropertiesConfiguration cfg, int maxJobs) {
        this.cfg = cfg;
        this.maxJobs = maxJobs;
    }

    @Override
    public Double call() throws Exception {
        /*
         * Compute Server is only required for VM external execution
         */
        Impaxs impxs = ComputeServerFactory.getComputeServer();
        impxs.startMasterServer(cfg);
        CompletionServiceFactory<Double> csf = new CompletionServiceFactory<Double>();
        csf.setTimeOut(1);
        csf.setTimeUnit(TimeUnit.SECONDS);
        csf.setBlockingWait(false);
        final ICompletionService<Double> mcs = csf.newDistributedCompletionService();
        for (int i = 0; i < maxJobs; i++) {
            mcs.submit(new TestCallable());
        }
        double result = 0.0d;
        try {
            List<Double> results = mcs.call();
            for (Double double1 : results) {
                result+=double1;
            }
        } catch (Exception ex) {
            Logger.getLogger(DistributedRmiExecution.class.getName()).
                    log(Level.SEVERE, null, ex);
            throw ex;
        }
        impxs.stopMasterServer();
        return result;
    }
}
