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
package net.sf.maltcms.execution.masterServer.messages;

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

    public static Reporter getInstance(){
        return instance;
    }

    public boolean addListener(IReceiver receiver){
        return listener.add(receiver);
    }

    public boolean removeListener(IReceiver receiver){
        return listener.remove(receiver);
    }
    
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
