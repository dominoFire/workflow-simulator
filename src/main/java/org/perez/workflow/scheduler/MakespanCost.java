package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Task;

/**
 * Created by microkid on 9/17/16.
 */
public class MakespanCost
    implements CostFunction
{
    protected MakespanCost() {
    }

    @Override
    public double apply(Task t, Resource r) {
        return t.getComplexityFactor() / r.getSpeedFactor();
    }

    public static MakespanCost create()
    {
        return new MakespanCost();
    }
}
