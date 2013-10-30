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
package net.sf.mpaxs.spi.server.consoleInput;

import net.sf.mpaxs.spi.server.Host;
import net.sf.mpaxs.spi.server.MasterServer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.spi.server.messages.IComputeHostEventListener;
import net.sf.mpaxs.spi.server.messages.IReceiver;
import net.sf.mpaxs.spi.server.settings.Settings;
import net.sf.mpaxs.api.event.IJobEventListener;

/**
 * Receives console inputs.
 * @author Kai Bernd Stadermann
 */
public class Input implements Runnable, IReceiver, IJobEventListener, IComputeHostEventListener{

    private MasterServer master;
    private boolean cancel = false;

	/**
	 *
	 * @param master
	 */
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

	/**
	 *
	 * @param message
	 */
	@Override
    public void newMessage(String message) {
        print(message);
    }

    @Override
    public void jobChanged(IJob job) {
        print("Job "+job.getId()+" changed status to: "+job.getStatus());
    }

	/**
	 *
	 * @param host
	 */
	@Override
    public void hostAdded(Host host) {
        print("New ComputeHost added with IP: "+host.getIP());
    }

	/**
	 *
	 * @param host
	 */
	@Override
    public void hostRemoved(Host host) {
        print("ComputeHost removed with IP: "+host.getIP());
    }

	/**
	 *
	 * @throws InterruptedException
	 */
	public void cancel() throws InterruptedException {
        //throw new InterruptedException();
    }

    public void run() {
        System.out.println("-- MasterServer ready! --");
        System.out.println("IP address used: " + net.sf.mpaxs.spi.server.settings.Settings.getInstance().getLocalIP());
        System.out.println("Port used: "+net.sf.mpaxs.spi.server.settings.Settings.getInstance().getLocalPort());
        System.out.println("Type \"exit\" to end program.");
        readCommand();
    }

	/**
	 *
	 * @param host
	 */
	@Override
	public void hostFree(Host host) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
