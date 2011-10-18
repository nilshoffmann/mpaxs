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
package net.sf.maltcms.execution.api.job;

import java.io.Serializable;

/**
 *  Uses an int as a progress representation. The int value
 *  can be between 0 and 100.
 * @author Kai Bernd Stadermann
 */
public class Progress implements Serializable{

    private int progressValue = -1;
    private String message = "";

    /**
     * Set the current progress
     * @param progressValue int values between 0 and 100.
     * @return true if value is in allowed range, false if not.
     */
    public boolean setProgress(int progressValue){
        boolean ret = false;
        if(progressValue >= 0 && progressValue <= 100){
            this.progressValue = progressValue;
            ret = true;
        }
        return ret;
    }

    /**
     * Return an int between 0 and 100 representing a progress or -1 if no
     * progress is set.
     * @return progressValue
     */
    public int getProgressValue() {
        return progressValue;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMesaage() {
        return message;
    }
}
