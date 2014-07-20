package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Workflow;

import java.util.List;

/**
 * Created by perez on 17/07/14.
 */
public class MaxMin {
    public static List<Schedule> schedule(Workflow w, List<Resource> resourceList) {
        return XMin.schedule(w, resourceList, XMinAlgorithm.MaxMin);
    }
}
