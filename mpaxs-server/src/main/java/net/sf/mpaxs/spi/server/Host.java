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
package net.sf.mpaxs.spi.server;

import java.util.UUID;


/**
 * Represents a ComputeHost instance.
 * @author Kai Bernd Stadermann
 */
public class Host {

    private UUID hostID;
    private int jobID = 0;
    private String name;
    private String ip;
    private int status = 0;
    private int cores = 0;
    private int freeCores = 0;
    private int numberOfJobs = 0;

    /**
     * Creates a new Compute Host instance representation
     * @param name Name used by the ComputeHost to register at his local RMI-Registry
     * @param ip IP-Adress of the Server on which the ComputeHost is running.
     * @param cores Number of availible cores an the ComputeHost server.
     * @param hostID ID to identify the host.
     */
    Host(String name, String ip, int cores, UUID hostID){
        this.name = name;
        this.ip = ip;
        this.cores = cores;
        this.freeCores = cores;
        this.hostID = hostID;
    }

	/**
	 *
	 * @return
	 */
	public int getNumberOfJobs() {
        return numberOfJobs;
    }

	/**
	 *
	 * @return
	 */
	public int getFreeCores() {
        return freeCores;
    }

	/**
	 *
	 */
	public void oneCoreMoreUsed() {
        freeCores = freeCores - 1;
        numberOfJobs = numberOfJobs + 1;
    }

	/**
	 *
	 */
	public void oneCoreUnused() {
        freeCores = freeCores + 1;
        numberOfJobs = numberOfJobs - 1;
    }

	/**
	 *
	 * @return
	 */
	public UUID getId() {
        return hostID;
    }

	/**
	 *
	 * @return
	 */
	public int getJobID(){
        return jobID;
    }

	/**
	 *
	 * @param jobID
	 */
	public void changejobID(int jobID){
        this.jobID = jobID;
    }

	/**
	 *
	 * @return
	 */
	public int getStatus(){
        return status;
    }

	/**
	 *
	 * @param status
	 */
	public void changeStatus(int status){
        this.status = status;
    }

	/**
	 *
	 * @return
	 */
	public String getName(){
        return name;
    }

	/**
	 *
	 * @return
	 */
	public String getIP(){
        return ip;
    }

	/**
	 *
	 * @return
	 */
	public int getCores() {
        return cores;
    }

    @Override
    public String toString(){
        return ip;
    }
}
