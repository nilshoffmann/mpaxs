/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.api.server;

import net.sf.maltcms.execution.api.ExecutionType;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author nilshoffmann
 */
public interface IComputeHostLauncher {
    ExecutionType getExecutionType();
    void startComputeHost(Configuration cfg);
}
