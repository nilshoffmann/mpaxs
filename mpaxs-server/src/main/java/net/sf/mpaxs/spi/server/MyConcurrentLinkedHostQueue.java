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

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class MyConcurrentLinkedHostQueue extends LinkedBlockingQueue<Host> {

	private final HashMap<UUID, Host> queueBack = new HashMap<UUID, Host>();

	/**
	 *
	 */
	public MyConcurrentLinkedHostQueue() {
		super();
	}

	/**
	 *
	 * @return
	 */
	public HashMap<UUID, Host> getAll() {
		return queueBack;
	}

	/**
	 *
	 * @param hostId
	 * @return
	 */
	public Host get(UUID hostId) {
		return queueBack.get(hostId);
	}

	/**
	 *
	 * @param hostId
	 * @return
	 */
	public boolean containsKey(UUID hostId) {
		return queueBack.containsKey(hostId);
	}

	/**
	 *
	 * @param hostId
	 * @return
	 */
	public Host remove(UUID hostId) {
		Host host = queueBack.get(hostId);
		super.remove(host);
		queueBack.remove(hostId);
		return host;

	}

	@Override
	public void put(Host e) throws InterruptedException {
		super.put(e);
		queueBack.put(e.getId(), e);
	}

	@Override
	public boolean offer(Host e, long timeout, TimeUnit unit) throws InterruptedException {
		if (super.offer(e, timeout, unit)) {
			queueBack.put(e.getId(), e);
			return true;
		}
		return false;
	}

	@Override
	public Host take() throws InterruptedException {
		Host host = super.take();
		queueBack.remove(host.getId());
		return host;
	}

	@Override
	public Host poll(long timeout, TimeUnit unit) throws InterruptedException {
		Host host = super.poll(timeout, unit);
		queueBack.remove(host.getId());
		return host;
	}

	@Override
	public Host peek() {
		return super.peek();
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
