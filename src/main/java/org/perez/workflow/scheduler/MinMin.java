package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Workflow;

import java.util.List;

/**
 * Created by perez on 17/07/14.
 */
public class MinMin
    implements WorkflowSchedulingAlgorithm
{
    private static MinMin instance;

    public static WorkflowSchedulingAlgorithm getInstance() {
        if(instance==null)
            instance = new MinMin();
        return instance;
    }

    @Override
    public List<Schedule> generateSchedule(Workflow w, List<Resource> resourceList) {
        return schedule(w, resourceList);
    }

    @Override
    public String getName() {
        return "MinMin";
    }

    public static List<Schedule> schedule(Workflow w, List<Resource> resourceList) {
        return XMin.schedule(w,resourceList, XMinAlgorithm.MinMin);
    }
}
