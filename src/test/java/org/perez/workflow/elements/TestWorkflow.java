package org.perez.workflow.elements;

import org.junit.Test;
import org.perez.workflow.elements.Task;
import org.perez.workflow.elements.Workflow;

import static org.junit.Assert.assertTrue;

/**
 * Created by perez on 14/07/14.
 */
public class TestWorkflow {

    @Test
    public void testNull() {
        Workflow w = new Workflow();
        try {
            w.addTask(null);
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(true);
        }

        try {
            w.addDependency(new Task("t1", 1), null);
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(true);
        }

        try {
            w.addDependency(null, new Task("t1", 1));
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(true);
        }

        try {
            w.addDependency(null, null);
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testProperties() {
        Task t1 = new Task("task1", 1.0);
        Task t2 = new Task("task2", 1.0);
        Task t3 = new Task("task3", 1.0);
        Task t4 = new Task("task4", 1.0);

        Workflow w = new Workflow();
        w.addTask(t1);
        w.addTask(t2);
        w.addTask(t3);
        w.addTask(t4);
        w.addDependency(t1,t3);
        w.addDependency(t1,t2);
        w.addDependency(t3,t4);
        w.addDependency(t2,t4);

        System.out.println(w);
    }
}
