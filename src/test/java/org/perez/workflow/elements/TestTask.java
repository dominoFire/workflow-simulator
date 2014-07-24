package org.perez.workflow.elements;

import org.junit.Test;
import org.perez.workflow.elements.Task;
import static org.junit.Assert.*;

/**
 * Created by perez on 14/07/14.
 */
public class TestTask
{
    @Test
    public void testNulls() {
        try {
            Task t1 = new Task(null, 1);
            assertTrue(false);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            assertTrue(true);
        }

        try {
            Task t1 = new Task("", 1);
            assertTrue(false);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            assertTrue(true);
        }

        try {
            Task t2 = new Task("tarea", 0);
            assertTrue(false);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            assertTrue(true);
        }

        try {
            Task t3 = new Task("tarea", -1);
            assertTrue(false);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            assertTrue(true);
        }
    }

    @Test
    public void testProperties() {
        Task t1 = new Task("tarea1", 10);
        System.out.println(t1);
        t1.setName("tarea2");
        t1.setComplexityFactor(20);
        assertEquals("tarea2", t1.getName());
        assertEquals(20, t1.getComplexityFactor(), 0.000000001);

        System.out.println(t1);
        try {
            t1.setName(null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(t1);
        try {
            t1.setComplexityFactor(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(t1);
        try {
            t1.setName("");
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(t1);
        try {
            t1.setComplexityFactor(-13);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(t1);
    }

    @Test
    public void testEquals() {
        Task t1 = new Task("tarea1", 20);
        Task t2 = new Task("tarea1", 40);
        assertTrue(t1.equals(t2));
    }
}
