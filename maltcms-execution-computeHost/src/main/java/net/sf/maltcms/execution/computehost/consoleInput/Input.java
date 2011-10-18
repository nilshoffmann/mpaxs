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
package net.sf.maltcms.execution.computehost.consoleInput;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import net.sf.maltcms.execution.api.computeHost.IRemoteHost;

/**
 * Nimmt Konsoleneingaben entgegen.
 * @author Kai Bernd Stadermann
 */
public class Input {

    private IRemoteHost host;

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
