package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Workflow;

import java.util.List;

/**
 * Interfaz que deben implementar los algoritmos de planificacion
 * Con exepcion del algoritmo ciego
 */
public interface WorkflowSchedulingAlgorithm
{
    List<Schedule> generateSchedule(Workflow w, List<Resource> resourceList);

    String getName();
}
