package org.perez.workflow.elements;

import org.junit.Test;
import org.perez.workflow.elements.Resource;
import static org.junit.Assert.*;

/**
 * Created by perez on 14/07/14.
 */
public class TestResource
{
	@Test
    public void testNulls() {
    	Resource r1 = new Resource("r1", 1);
    	try {
    		r1.setName(null);
    		assertTrue(false);
    	} catch(Exception e) {
    		assertTrue(true);
    	}
    	
    	try {
    		r1.setSpeedFactor(-1);
    		assertTrue(false);
    	} catch(Exception e) {
    		assertTrue(true);
    	}

        try {
            r1.setReadyTime(-1);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testEquals() {
        Resource r1 = new Resource("r1", 1);
        Resource r2 = new Resource("r1", 10);
        assertTrue(r1.equals(r2));
    }
}
