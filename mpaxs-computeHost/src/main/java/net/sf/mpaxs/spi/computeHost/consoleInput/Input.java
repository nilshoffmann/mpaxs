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
package net.sf.mpaxs.spi.computeHost.consoleInput;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import net.sf.mpaxs.api.computeHost.IRemoteHost;

/**
 * Nimmt Konsoleneingaben entgegen.
 * @author Kai Bernd Stadermann
 */
public class Input {

    private IRemoteHost host;

	/**
	 *
	 * @param host
	 */
	public Input(IRemoteHost host){
        this.host = host;
        System.out.println("-- ComputeHost ready! --");
        System.out.println("Type \"exit\" to end program.");
        readCommand();
    }

    /**
     * Prints the given String on the Command line standart out.
     * @param str String to print
     */
    public static void print(final String str){
        System.out.println();
        System.out.println(str);
        System.out.print("> ");
    }

    /**
     * Prints the given String on the Command line err out.
     * @param str String to print
     */
    public static void printErr(final String str){
        System.out.println();
        System.err.println(str);
        System.out.print("> ");
    }

    /**
     * Ließt ein Zeile von der Kommandozeile ein.
     */
    private void readCommand() {
        System.out.print("> ");
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            commandInterpreter(input.readLine().toLowerCase());
        } catch (Exception e) {
            // TODO: Exceptionhandling
        }
        readCommand();
    }

    /**
     * Interpretiert die Eingabe auf der Kommandozeile und führt eine
     * entsprechende Aktion aus.
     * @param input Kommandozeileneingabe
     */
    private void commandInterpreter(String input){
        if(input.equals("exit")){
            if(host.disconnectFromMasterServer()){
                System.exit(0);
            }else{
                System.exit(1);
            }
        }
        System.out.println("Unknown command!");
    }
}
