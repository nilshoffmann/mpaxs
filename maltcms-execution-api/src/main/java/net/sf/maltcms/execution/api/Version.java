/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.execution.api;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author nilshoffmann
 */
public class Version {

    public static String getVersion() throws IOException {
        Properties props = new Properties();
        props.load(Version.class.getResourceAsStream("/net/sf/maltcms/execution/api/version.properties"));
        return props.getProperty("api.version");

    }
}
