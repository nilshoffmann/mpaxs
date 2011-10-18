/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.spi;

import net.sf.maltcms.execution.api.ExecutionFactory;
import net.sf.maltcms.execution.api.Impaxs;

/**
 * Factory attempts to create a new instance of Impaxs,
 * if such an instance does not exist.
 * @author nilshoffmann
 */
public class ComputeServerFactory {

    private static Impaxs imp = null;

    private ComputeServerFactory() {
    }
    
    public static Impaxs getComputeServer(boolean autoCreate) {
        if(autoCreate) {
            return getComputeServer();
        }
        return imp;
    }

    public static Impaxs getComputeServer() {
        if (imp == null) {
            imp = ExecutionFactory.getDefaultComputeServer();
        }
        return imp;
    }
    
}
