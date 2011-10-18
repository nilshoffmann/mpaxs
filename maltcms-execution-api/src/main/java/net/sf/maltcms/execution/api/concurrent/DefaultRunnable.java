/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.api.concurrent;

import net.sf.maltcms.execution.api.concurrent.ConfigurableRunnable;
import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import net.sf.maltcms.execution.api.job.Progress;

/**
 *
 * @author nilshoffmann
 */
public class DefaultRunnable<V> implements ConfigurableRunnable<V> {

    private final Runnable r;
    private final Progress p = new Progress();
    private final V result;
    private boolean done = false;
    private Throwable t;

    public DefaultRunnable(Runnable r, V result) {
        if (!(r instanceof Serializable)) {
            throw new IllegalArgumentException("Runnable must implement Serializable!");
        }
        this.r = r;
        this.result = result;
    }

    @Override
    public void configure(File pathToConfig) {
    }

    @Override
    public Progress getProgress() {
        return p;
    }

    @Override
    public void run() {
        p.setMessage("Starting execution of " + r.getClass().getName());
        p.setProgress(0);
        try {
            this.r.run();
            p.setProgress(100);
            done = true;
        } catch (Exception ex) {
            t = ex;
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        if(t!=null) {
            throw new ExecutionException("Computation of result failed!",t);
        }
        if(!done) {
            throw new InterruptedException("Result not yet available!");
        }
        return result;
    }
}
