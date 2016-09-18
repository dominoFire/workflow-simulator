package org.perez.workflow.scheduler;

import org.perez.resource.ResourceConfig;
import org.perez.workflow.elements.Task;

import java.util.List;

/**
 * Created by microkid on 9/17/16.
 */
class BinPackingEntry {
    public List<Task> task;
    public ResourceConfig resourceConfig;

    public BinPackingEntry(List<Task> task, ResourceConfig resourceConfig) {
        this.task = task;
        this.resourceConfig = resourceConfig;
    }

    public double getRuntime() {
        double d = 0.;
        for (Task t : this.task)
            d = Math.max(d, t.getComplexityFactor() / this.resourceConfig.getSpeedFactor());
        return d;
    }

}
