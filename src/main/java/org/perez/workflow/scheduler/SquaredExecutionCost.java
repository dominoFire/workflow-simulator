package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Task;

/**
 * Multiplicando al cuadrado
 */
public class SquaredExecutionCost
    implements CostFunction
{
    private SquaredExecutionCost() {}

    @Override
    public double apply(Task t, Resource r)
    {
        double v = t.getComplexityFactor() / r.getSpeedFactor();
        return  v * v * r.getCostHour();
    }

    public static SquaredExecutionCost create()
    {
        return new SquaredExecutionCost();
    }
}
