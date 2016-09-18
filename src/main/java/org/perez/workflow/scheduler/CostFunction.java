package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Task;

/**
 * Created by microkid on 9/17/16.
 */
public interface CostFunction
{
    double apply(Task t, Resource r);
}
