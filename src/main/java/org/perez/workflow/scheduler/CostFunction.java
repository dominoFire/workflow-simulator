package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Task;

/**
 * Interface que define una funcion de costo parcial
 */
public interface CostFunction
{
    /** Costo asociado a ejecutar una tarea t en el recurso r */
    double apply(Task t, Resource r);
}
