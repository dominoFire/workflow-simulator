package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Workflow;

import java.util.List;

/**
 * Created by perez on 17/07/14.
 */
public class MaxMin
    implements WorkflowSchedulingAlgorithm
{
    private static MaxMin instance;

    public static WorkflowSchedulingAlgorithm getInstance() {
        if(instance==null)
            instance = new MaxMin();
        return instance;
    }

    @Override
    public List<Schedule> generateSchedule(Workflow w, List<Resource> resourceList) {
        return schedule(w, resourceList);
    }

    @Override
    public String getName() {
        return "MaxMin";
    }

    public static List<Schedule> schedule(Workflow w, List<Resource> resourceList) {
        return XMin.schedule(w, resourceList, XMinAlgorithm.MaxMin);
    }
}
