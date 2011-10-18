/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.api.concurrent;

import net.sf.maltcms.execution.api.concurrent.ConfigurableRunnable;
import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import net.sf.maltcms.execution.api.job.Progress;

/**
 *
 * @author nilshoffmann
 */
public class DefaultCallable<V> implements ConfigurableRunnable<V> {
    private final Progress p = new Progress();
//    private FutureTask<V> f;
    private final Callable<V> c;
    private final String name;
    private V result;
    private boolean done = false;
    private Throwable t;

    public DefaultCallable(Callable<V> c) {
        if (!(c instanceof Serializable)) {
            throw new IllegalArgumentException("Callable must implement Serializable!");
        }
        this.c = c;
        this.name = c.getClass().getName();
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
//        this.f = new FutureTask<V>(c);
        p.setMessage("Starting execution of " + this.name);
        p.setProgress(0);
        try {
            result = this.c.call();
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
