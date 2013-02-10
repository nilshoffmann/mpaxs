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
package net.sf.mpaxs.api.concurrent;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.sf.mpaxs.api.job.Progress;

/**
 *
 * @author Nils Hoffmann
 */
public class DefaultRunnable<V> implements ConfigurableRunnable<V> {

    private final Runnable r;
    private final Progress p = new Progress();
    private final V result;
    private AtomicBoolean done = new AtomicBoolean(false);
    private Throwable t;

    /**
     *
     * @param r
     * @param result
     */
    public DefaultRunnable(Runnable r, V result) {
        if (!(r instanceof Serializable)) {
            throw new IllegalArgumentException("Runnable must implement Serializable!");
        }
        this.r = r;
        this.result = result;
    }

    /**
     *
     * @param pathToConfig
     */
    @Override
    public void configure(File pathToConfig) {
    }

    /**
     *
     * @return
     */
    @Override
    public Progress getProgress() {
        return p;
    }

    /**
     *
     */
    @Override
    public void run() {
        p.setMessage("Starting execution of " + r.getClass().getName());
        p.setProgress(0);
        try {
            this.r.run();
            p.setProgress(100);
            done.set(true);
        } catch (Exception ex) {
            t = ex;
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        if(t!=null) {
            throw new ExecutionException("Computation of result failed!",t);
        }
        if(!done.get()) {
            throw new InterruptedException("Result not yet available!");
        }
        return result;
    }
}
