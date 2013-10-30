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
package net.sf.mpaxs.spi.server.dirWatcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.spi.server.logging.EventLogger;
import net.sf.mpaxs.spi.server.MasterServer;
import net.sf.mpaxs.spi.server.messages.Reporter;
import net.sf.mpaxs.spi.server.settings.Settings;
import net.sf.mpaxs.api.job.Job;

/**
 * Watches a given directory for new JobConfig Files and adds them to the
 * Job Queue.
 * @author Kai Bernd Stadermann
 */
public class DirWatcher implements Runnable {

    private Settings settings = Settings.getInstance();
    private Reporter reporter = Reporter.getInstance();
    private MasterServer master;

    /**
     * Constructor.
     * @param master instance of MasterServer.
     */
    public DirWatcher(MasterServer master) {
        this.master = master;
        createDirs();
    }

    /**
     * Sets up the input directory. All folders needed are created automatically
     */
    private void createDirs() {
        File[] files = settings.getBaseDir().listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".cfg");
            }
        });
        settings.getDoneDir().mkdir();
        settings.getInputDir().mkdir();
        settings.getRunningDir().mkdir();
        settings.getErrorDir().mkdir();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                files[i].renameTo(new File(settings.getInputDir() + File.separator + files[i].getName()));
            }
        }
    }

    /**
     * Reads new Input files and creates new Jobs out of them. Afer that the
     * jobs are added to the job queue and the config files are moved to a new
     * directoy.
     */
    public void run() {
        File[] cfgs = settings.getInputDir().listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".cfg");
            }
        });

        if (cfgs != null) {
            for (int i = 0; i < cfgs.length; i++) {
                File location = new File(settings.getRunningDir() + File.separator + cfgs[i].getName());
                File error = new File(settings.getErrorDir() + File.separator + cfgs[i].getName());
                cfgs[i].renameTo(location);
                try {                                        
                    IJob job = new Job(location.getAbsolutePath());
                    master.submitJob(job);
                    reporter.report("New Job submitted out of input Directory!");
                } catch (ClassNotFoundException ex) {
                    location.renameTo(error);
                    reporter.report("No Class found! Are you shure your config file ("+cfgs[i].getName()+") is ok?");
                    File tmp = new File(settings.getRunningDir() + File.separator + cfgs[i].getName());
                    tmp.renameTo(new File(settings.getErrorDir() + File.separator + cfgs[i].getName()));
                    master.addFailedJob(cfgs[i].getName());
                    EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
                } catch (MalformedURLException ex) {
                    EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    location.renameTo(error);
                    reporter.report("No Class found! Are you shure your config file ("+cfgs[i].getName()+") is ok?");
                    File tmp = new File(settings.getRunningDir() + File.separator + cfgs[i].getName());
                    tmp.renameTo(new File(settings.getErrorDir() + File.separator + cfgs[i].getName()));
                    master.addFailedJob(cfgs[i].getName());
                    EventLogger.getInstance().getLogger().log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
