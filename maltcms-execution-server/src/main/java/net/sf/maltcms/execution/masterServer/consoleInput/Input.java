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
package net.sf.maltcms.execution.masterServer.consoleInput;

import net.sf.maltcms.execution.masterServer.Host;
import net.sf.maltcms.execution.masterServer.MasterServer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.masterServer.messages.IComputeHostEventListener;
import net.sf.maltcms.execution.masterServer.messages.IReceiver;
import net.sf.maltcms.execution.masterServer.settings.Settings;
import net.sf.maltcms.execution.api.event.IJobEventListener;

/**
 * Receives console inputs.
 * @author Kai Bernd Stadermann
 */
public class Input implements Runnable, IReceiver, IJobEventListener, IComputeHostEventListener{

    private MasterServer master;
    private boolean cancel = false;

    public Input(MasterServer master) {
        this.master = master;
    }

    /**
     * Prints the given String on the Command line standart out.
     * @param str String to print
     */
    public static void print(final String str) {
        if (!Settings.getInstance().getGuiMode()) {
            System.out.println();
            System.out.println(str);
            System.out.print("> ");
        }
    }

    /**
     * Prints the given String on the Command line err out.
     * @param str String to print
     */
    public static void printErr(final String str) {
        if (!Settings.getInstance().getGuiMode()) {
            System.out.println();
            System.err.println(str);
            System.out.print("> ");
        }
    }

    /**
     * Reads a line from the command prompt.
     */
    private void readCommand() {
        System.out.print("> ");
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            commandInterpreter(input.readLine().toLowerCase());
        } catch (Exception e) {
            // TODO: Exceptionhandling
        }
    }

    /**
     * Interprets the command prompt input and starts actions according to them.
     * @param input Kommandozeileneingabe
     */
    @SuppressWarnings("element-type-mismatch")
    private void commandInterpreter(String input) {
        if (input.equals("exit")) {
            print("MasterServer will shutdown now! This may take a moment.");
            master.shutdown();
        } else if(input.equals("list hosts")) {
            System.out.println("UUID\tIP");
            for(UUID uid:master.getHosts().keySet()) {
                System.out.println(uid+"\t"+master.getHosts().get(uid));
//                master.getHosts();
            }
        } 
        System.out.print("> ");
    }

    @Override
    public void newMessage(String message) {
        print(message);
    }

    @Override
    public void jobChanged(IJob job) {
        print("Job "+job.getId()+" changed status to: "+job.getStatus());
    }

    @Override
    public void hostAdded(Host host) {
        print("New ComputeHost added with IP: "+host.getIP());
    }

    @Override
    public void hostRemoved(Host host) {
        print("ComputeHost removed with IP: "+host.getIP());
    }
    
    public void cancel() throws InterruptedException {
        //throw new InterruptedException();
    }

    public void run() {
        System.out.println("-- MasterServer ready! --");
        System.out.println("IP address used: " + net.sf.maltcms.execution.masterServer.settings.Settings.getInstance().getLocalIP());
        System.out.println("Port used: "+net.sf.maltcms.execution.masterServer.settings.Settings.getInstance().getLocalPort());
        System.out.println("Type \"exit\" to end program.");
        readCommand();
    }
}