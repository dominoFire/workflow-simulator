package org.perez.workflow.scheduler;

/**
 * Get algorithms
 */
public class Algorithms
{
    public static WorkflowSchedulingAlgorithm[] getAlgorithms()
    {
        WorkflowSchedulingAlgorithm[] wfs_algorithms = {
                MaxMin.getInstance(),
                MinMin.getInstance(),
                Myopic.getInstance()
        };
        return wfs_algorithms;
    }
}
