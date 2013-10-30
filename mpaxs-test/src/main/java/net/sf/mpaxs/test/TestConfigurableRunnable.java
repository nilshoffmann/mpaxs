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

import java.io.File;
import net.sf.mpaxs.api.concurrent.ConfigurableRunnable;
import net.sf.mpaxs.api.job.Progress;

/**
 *
 * @author Nils Hoffmann
 */
public class TestConfigurableRunnable implements ConfigurableRunnable<Long>{

    private Progress progress = new Progress();
    private long finished = -1l;

    @Override
    public Long get() {
        return finished;
    }

    @Override
    public void configure(File pathToConfig) {

    }

    @Override
    public Progress getProgress() {
        return progress;
    }

    @Override
    public void run() {
        progress.setMessage("Starting computation");
        for(int i = 0;i<10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                //Logger.getLogger(TestConfigurableRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
            progress.setProgress(i*10);
        }
        finished = System.nanoTime();
    }

}
