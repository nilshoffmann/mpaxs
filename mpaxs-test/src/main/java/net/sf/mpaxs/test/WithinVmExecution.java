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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ICompletionService;
import net.sf.mpaxs.spi.concurrent.CompletionServiceFactory;

/**
 *
 * @author Nils Hoffmann
 */
public class WithinVmExecution implements Callable<Double>, Serializable {

    private final int maxJobs;
    private final int maxThreads;

    public WithinVmExecution(int maxJobs, int maxThreads) {
        this.maxJobs = maxJobs;
        this.maxThreads = maxThreads;
    }

    @Override
    public Double call() throws Exception {
        CompletionServiceFactory<Double> csf = new CompletionServiceFactory<Double>();
        csf.setTimeOut(1);
        csf.setTimeUnit(TimeUnit.SECONDS);
        csf.setMaxThreads(maxThreads);
        csf.setBlockingWait(false);

        final ICompletionService<Double> mcs2 = csf.newLocalCompletionService();
        for (int i = 0; i < maxJobs; i++) {
//            mcs2.submit(new TestCallable());
            mcs2.submit(new Callable<Double>() {
                @Override
                public Double call() throws Exception {
                    long sum = 0;
                    for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
                        sum += i;
                    }
                    if (Math.random() > 0.9) {
                        throw new IOException("Failed on io due to simulated random error!");
                    }
                    return Long.valueOf(sum).doubleValue();
                }
            });
        }
        double result = 0.0d;
        try {
            List<Double> results = mcs2.call();
            System.out.println("Local Results (Local Host execution): " + results);
            for (Double double1 : results) {
                result += double1;
            }
        } catch (Exception ex) {
            Logger.getLogger(WithinVmExecution.class.getName()).
                    log(Level.SEVERE, null, ex);
            throw ex;
        }
        return result;
    }
}
