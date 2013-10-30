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
package net.sf.mpaxs.test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 *
 * @author Nils Hoffmann
 */
public class TestCallable implements Callable<Double>, Serializable {

    @Override
    public Double call() throws Exception {
        long sum = 0;
		long start = System.currentTimeMillis();
		Random r = new Random(start);
		int sumUntil = r.nextInt(Integer.MAX_VALUE);
        for(int i = 0;i<sumUntil;i++) {
            sum+=i;
        }
		System.out.println("Executed sum in "+(System.currentTimeMillis()-start)+" ms");
        if(Math.random()>0.92) {
            throw new IOException("Failed on io due to simulated random error!");
        }
        return Long.valueOf(sum).doubleValue();
    }
}
