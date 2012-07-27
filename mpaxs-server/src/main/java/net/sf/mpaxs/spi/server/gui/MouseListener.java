/*
 * Mpaxs, modular parallel execution system. 
 * Copyright (C) 2010-2012, The authors of Mpaxs. All rights reserved.
 *
 * Project Administrator: nilshoffmann A T users.sourceforge.net
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
 * under licenses/ for details.
 */
package net.sf.mpaxs.spi.server.gui;

import java.awt.event.MouseEvent;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class MouseListener implements java.awt.event.MouseListener{

    private ListType type;
    private MainFrame main;

    public MouseListener(ListType type, MainFrame main){
        this.type = type;
        this.main = main;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getClickCount() == 2) {
            switch (type) {
                case WAITING:
                    main.openJobInfo(ListType.WAITING);
                    break;
                case RUNNING:
                    main.openJobInfo(ListType.RUNNING);
                    break;
                case DONE:
                    main.openJobInfo(ListType.DONE);
                    break;
                case FAILED:
                    main.openJobInfo(ListType.FAILED);
                    break;
                case HOSTS:
                    main.openComputeHostInfo();
                    break;
                case MESSAGE:
                    break;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}
