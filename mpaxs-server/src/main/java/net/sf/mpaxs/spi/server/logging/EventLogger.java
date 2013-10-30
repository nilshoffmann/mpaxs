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
package net.sf.mpaxs.spi.server.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.spi.server.settings.Settings;

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
            logger.addHandler(net.sf.mpaxs.spi.server.messages.Reporter.getInstance());
            if(!settings.getErrorToConsole()){
                logger.setUseParentHandlers(false);
            } 
      } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

	/**
	 *
	 * @return
	 */
	public static EventLogger getInstance() {
        return instance;
    }

	/**
	 *
	 * @return
	 */
	public Logger getLogger() {
        return logger;
    }
}
