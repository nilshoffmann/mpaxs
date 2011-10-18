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

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.sf.maltcms.execution.api.job.IJob;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class MyConcurrentLinkedJobQueue extends ConcurrentLinkedQueue<IJob> {

    private HashMap<UUID, IJob> queueBack = new HashMap<UUID, IJob>();

    public MyConcurrentLinkedJobQueue() {
        super();
    }

    public IJob getJob(UUID jobID){
        return queueBack.get(jobID);
    }

    public boolean containsJobWithID(UUID jobID){
        return queueBack.containsKey(jobID);
    }

    public IJob remove(UUID jobId) {
        IJob job = queueBack.get(jobId);
        super.remove(job);
        queueBack.remove(jobId);
        return job;

    }

    @Override
    public boolean offer(IJob job) {
        if (super.offer(job)) {
            queueBack.put(job.getId(), job);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean add(IJob job) {
        queueBack.put(job.getId(), job);
        return super.offer(job);
    }

    @Override
    public IJob poll() {
        IJob ret = super.poll();
        if (ret != null) {
            queueBack.remove(ret.getId());
        }
        return ret;
    }
}
