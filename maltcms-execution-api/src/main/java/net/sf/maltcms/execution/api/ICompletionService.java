package net.sf.maltcms.execution.api;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author nilshoffmann
 */
public interface ICompletionService<T> extends Callable<List<T>> {

    List<Callable<T>> getFailedTasks();
    
    Future<T> submit(Callable<T> c) throws RejectedExecutionException, NullPointerException;
    
    Future<T> submit(Runnable r, T t) throws RejectedExecutionException, NullPointerException;
    
}
