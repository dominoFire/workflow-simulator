package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Task;

/**
 * Created by microkid on 9/17/16.
 */
public class ExecutionCost
    implements CostFunction
{
    private ExecutionCost() {}

    @Override
    public double apply(Task t, Resource r) {
        return  t.getComplexityFactor() / r.getSpeedFactor() * r.getCostHour();
    }

    public static ExecutionCost create() {
        return new ExecutionCost();
    }
}
