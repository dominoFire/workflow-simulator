package org.perez.workflow.scheduler;

import org.perez.resource.ResourceConfig;
import org.perez.workflow.elements.Task;

import java.util.Collection;
import java.util.Iterator;

/**
 * Clase abstracta que deben implementar todos
 * los asignadores por algoritmo de bolsa
 */
public abstract class AbstractBinPackingAssigner
{
    protected Task[] tasks;
    protected ResourceConfig[] resourceConfigs;
    protected CostFunction f;

    public AbstractBinPackingAssigner(Collection<Task> tasks, Collection<ResourceConfig> resourceConfigs, CostFunction cf) {
        if (tasks == null || tasks.isEmpty())
            throw new IllegalArgumentException("tasks cannot be empty or null");
        if (resourceConfigs == null || resourceConfigs.isEmpty())
            throw new IllegalArgumentException("resourceConfigs cannot be null or empty");

        this.tasks = new Task[tasks.size()];
        Iterator<Task> it = tasks.iterator();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = it.next();
            if (t == null)
                throw new IllegalArgumentException(String.format("Task list element %d cannot be null", i));
            this.tasks[i] = t;
        }

        this.resourceConfigs = new ResourceConfig[resourceConfigs.size()];
        Iterator<ResourceConfig> itrc = resourceConfigs.iterator();
        for (int i = 0; i < resourceConfigs.size(); i++) {
            ResourceConfig rc = itrc.next();
            if (rc == null)
                throw new IllegalArgumentException(String.format("ResourceConfig list element %d cannot be null", i));
            this.resourceConfigs[i] = rc;
        }

        if (cf == null)
            throw new IllegalArgumentException("CostFunction cannot be null");
        this.f = cf;
    }

    public abstract Collection<BinPackingEntry> getMappings();
}
