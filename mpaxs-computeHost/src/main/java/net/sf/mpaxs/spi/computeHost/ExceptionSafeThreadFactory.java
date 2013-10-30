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
package net.sf.mpaxs.spi.computeHost;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 *
 * @author Kai Bernd Stadermann
 */
public final class ExceptionSafeThreadFactory implements ThreadFactory {

	private final ThreadFactory threadFactory = Executors.defaultThreadFactory();
	private final Thread.UncaughtExceptionHandler handler;

	/**
	 *
	 * @param handler
	 */
	public ExceptionSafeThreadFactory(Thread.UncaughtExceptionHandler handler) {
		this.handler = handler;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = threadFactory.newThread(r);
		t.setUncaughtExceptionHandler(handler);
		return t;
	}

}
