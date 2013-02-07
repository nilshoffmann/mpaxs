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
import java.util.Random;
import java.util.concurrent.Callable;

/**
 *
 * @author Nils Hoffmann
 */
public class TestCallable implements Callable<Double>, Serializable {

    @Override
    public Double call() throws Exception {
        Random sr = new Random(System.nanoTime());
        //generate a random double between 0 and 1
        double randomNumber = sr.nextDouble();
        //sleep between 0 and 1000 milliseconds
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
//                Logger.getLogger(TestCallable.class.getName()).log(Level.SEVERE, null, ex);
        }
        return randomNumber;
    }
}
