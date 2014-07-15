package org.perez.workflow;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Task;

/**
 * Created by Fernando on 06/07/2014.
 */
public class Main
{
    public static void main(String[] args)
    {
        Task t1 = new Task("task1", 1.0);
        Task t2 = new Task("task1", 1.0);
        Task t3 = new Task("task1", 1.0);
        Task t4 = new Task("task1", 1.0);

        Resource r1 = new Resource("r1", 1.0);
        Resource r2 = new Resource("r2", 1.0);
    }
}
