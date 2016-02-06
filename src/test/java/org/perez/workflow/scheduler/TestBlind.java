package org.perez.workflow.scheduler;

import org.junit.Test;
import org.perez.workflow.elements.TestWorkflow;
import org.perez.workflow.elements.Workflow;

/**
 * Created by microkid on 31/01/16.
 */
public class TestBlind {

    @Test
    public void testEstimateResources() {
        Workflow w = TestWorkflow.generateSimpleWorkflow();

        int maxResources = Blind.estimateResources(w);

        org.junit.Assert.assertEquals(2, maxResources);
    }
}
