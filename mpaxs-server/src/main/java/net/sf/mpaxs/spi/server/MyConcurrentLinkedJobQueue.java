/*
 * Mpaxs, modular parallel execution system. 
 * Copyright (C) 2010-2012, The authors of Mpaxs. All rights reserved.
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

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.sf.mpaxs.api.job.IJob;

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
