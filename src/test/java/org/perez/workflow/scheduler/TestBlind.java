package org.perez.workflow.scheduler;

import org.junit.Test;
import org.perez.workflow.elements.Task;
import org.perez.workflow.elements.TestWorkflow;
import org.perez.workflow.elements.Workflow;

import java.util.Map;

/**
 * Created by microkid on 31/01/16.
 */
public class TestBlind {

    @Test
    public void testEstimateResources() {
        Workflow w = TestWorkflow.generateSimpleWorkflow();

        Map<Task, Integer> segments = Blind.getSegments(w);

        int maxResources = Blind.estimateResources(segments);

        org.junit.Assert.assertEquals(2, maxResources);
    }
}
