package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Workflow;

import java.util.List;

/**
 * Created by microkid on 26/03/2016.
 */
public interface WorkflowSchedulingAlgorithm
{
    List<Schedule> generateSchedule(Workflow w, List<Resource> resourceList);

    String getName();
}
