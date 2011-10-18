/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 */
package net.sf.maltcms.execution.masterServer.dirWatcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.masterServer.logging.EventLogger;
import net.sf.maltcms.execution.masterServer.MasterServer;
import net.sf.maltcms.execution.masterServer.messages.Reporter;
import net.sf.maltcms.execution.masterServer.settings.Settings;
import net.sf.maltcms.execution.api.job.Job;

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
