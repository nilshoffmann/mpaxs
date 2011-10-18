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
package net.sf.maltcms.execution.masterServer.gui;

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
