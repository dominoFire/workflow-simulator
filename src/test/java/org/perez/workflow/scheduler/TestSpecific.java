package org.perez.workflow.scheduler;

import org.junit.Assert;
import org.junit.Test;
import org.perez.workflow.elements.Task;
import org.perez.workflow.elements.Workflow;

import java.util.Map;

/**
 * Created by microkid on 8/08/16.
 */
public class TestSpecific
{
    @Test
    public void testWorkflow()
    {
        Workflow w = Utils.readJson("./workflow1.obj", Workflow.class);

        Map<Task, Integer> segments = Blind.getSegments(w);

        for(Task t: segments.keySet())
            System.out.printf("%s -> %d\n", t.toString(), segments.get(t));

        int r = Blind.estimateResources(segments);

        Assert.assertEquals(2, r);
    }
}
