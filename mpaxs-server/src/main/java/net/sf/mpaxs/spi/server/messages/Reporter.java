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
package net.sf.mpaxs.spi.server.messages;

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class Reporter extends Handler{

    private ArrayList<IReceiver> listener = new ArrayList<IReceiver>();
    private static Reporter instance = new Reporter();

    private Reporter(){
    }

	/**
	 *
	 * @return
	 */
	public static Reporter getInstance(){
        return instance;
    }

	/**
	 *
	 * @param receiver
	 * @return
	 */
	public boolean addListener(IReceiver receiver){
        return listener.add(receiver);
    }

    public boolean removeListener(IReceiver receiver){
        return listener.remove(receiver);
    }

	/**
	 *
	 * @param message
	 */
	public void report(String message){
        sendMessageToListner(message);
    }
    
    private void sendMessageToListner(final String message){
        for(int i = 0; i < listener.size(); i++){
            listener.get(i).newMessage(message);
        }
    }

    @Override
    public void publish(LogRecord record) {
        String errorMessage = "Error in class: "+ record.getSourceClassName() + ". "
                               + "Method causing the error: " + record.getSourceMethodName()+"; Cause: "+record.getThrown();
        report(errorMessage);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
