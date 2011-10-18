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

/**
 *
 * @author Kai Bernd Stadermann
 */
public class MyConcurrentLinkedHostQueue extends ConcurrentLinkedQueue<Host> {

    private HashMap<UUID, Host> queueBack = new HashMap<UUID, Host>();

    public MyConcurrentLinkedHostQueue() {
        super();
    }

    public HashMap<UUID, Host> getAll() {
        return queueBack;
    }

    public Host get(UUID hostId){
        return queueBack.get(hostId);
    }

    public boolean containsKey(UUID hostId){
        return queueBack.containsKey(hostId);
    }

    public Host remove(UUID hostId) {
        Host host = queueBack.get(hostId);
        super.remove(host);
        queueBack.remove(hostId);
        return host;

    }

    @Override
    public boolean offer(Host host) {
        if (super.offer(host)) {
            queueBack.put(host.getId(), host);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean add(Host host) {
        queueBack.put(host.getId(), host);
        return super.offer(host);
    }

    @Override
    public Host poll() {
        Host ret = super.poll();
        if (ret != null) {
            queueBack.remove(ret.getId());
        }
        return ret;
    }
}
