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
package net.sf.mpaxs.api.job;

import java.io.Serializable;

/**
 * Uses an int as a progress representation. The int value
 * can be between -1 (no progress set), 0 (just started) and 100 (complete).
 *
 * @author Kai Bernd Stadermann
 */
public class Progress implements Serializable {

	private int progressValue = -1;
	private String message = "";

	/**
	 * Set the current progress
	 *
	 * @param progressValue int values between 0 and 100.
	 * @return true if value is in allowed range, false otherwise
	 */
	public boolean setProgress(int progressValue) {
		boolean ret = false;
		if (progressValue >= 0 && progressValue <= 100) {
			this.progressValue = progressValue;
			ret = true;
		}
		return ret;
	}

	/**
	 * Return an int between 0 and 100 representing a progress or -1 if no
	 * progress is set.
	 *
	 * @return progressValue
	 */
	public int getProgressValue() {
		return progressValue;
	}

	/**
	 * Set the progress message.
	 *
	 * @param message the message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Returns the current message
	 *
	 * @return the message
	 */
	public String getMesaage() {
		return message;
	}
}
