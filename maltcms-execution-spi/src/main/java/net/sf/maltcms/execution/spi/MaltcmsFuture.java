/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.spi;

import java.io.Serializable;
import java.util.concurrent.Future;

/**
 *
 * @author nilshoffmann
 */
public interface MaltcmsFuture<V extends Serializable> extends Future<V> {
    
}
