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
package net.sf.maltcms.execution.masterServer.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.maltcms.execution.masterServer.settings.Settings;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class EventLogger {

    private static EventLogger instance = new EventLogger();
    private Settings settings = Settings.getInstance();
    private Logger logger = Logger.getAnonymousLogger();
    FileHandler fh;

    private EventLogger() {
        try {
            fh = new FileHandler(settings.getBaseDir().getAbsolutePath() + File.separator + "log.xml");
            logger.addHandler(fh);
            logger.addHandler(net.sf.maltcms.execution.masterServer.messages.Reporter.getInstance());
            if(!settings.getErrorToConsole()){
                logger.setUseParentHandlers(false);
            } 
      } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public static EventLogger getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }
}
