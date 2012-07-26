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
package net.sf.maltcms.execution.api.concurrent;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import net.sf.maltcms.execution.api.job.Progress;

/**
 * Take care that the type V returned is Serializable.
 * @author Kai Bernd Stadermann
 */
public interface ConfigurableRunnable<V> extends Runnable, Serializable{

    /**
     * An InterruptedException is thrown, when the result is not yet ready.
     * An ExecutionException is thrown, when the job has experienced an Exception
     * or Error of any kind.
     * @return
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    public V get() throws InterruptedException, ExecutionException;

    public void configure(File pathToConfig);

    public Progress getProgress();
}
