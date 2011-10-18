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
package net.sf.maltcms.execution.masterServer;

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

    public int getNumberOfJobs() {
        return numberOfJobs;
    }

    public int getFreeCores() {
        return freeCores;
    }

    public void oneCoreMoreUsed() {
        freeCores = freeCores - 1;
        numberOfJobs = numberOfJobs + 1;
    }

    public void oneCoreUnused() {
        freeCores = freeCores + 1;
        numberOfJobs = numberOfJobs - 1;
    }

    public UUID getId() {
        return hostID;
    }

    public int getJobID(){
        return jobID;
    }

    public void changejobID(int jobID){
        this.jobID = jobID;
    }

    public int getStatus(){
        return status;
    }

    public void changeStatus(int status){
        this.status = status;
    }

    public String getName(){
        return name;
    }

    public String getIP(){
        return ip;
    }

    public int getCores() {
        return cores;
    }

    @Override
    public String toString(){
        return ip;
    }
}
