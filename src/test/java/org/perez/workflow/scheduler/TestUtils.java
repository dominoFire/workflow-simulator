package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Schedule;

import java.util.List;

/**
 * Created by microkid on 11/18/16.
 */
public class TestUtils
{
    public static AlgorithmExecutionResult fill(List<Schedule> schedule, long id, String algo_name)
    {
        AlgorithmExecutionResult r = new AlgorithmExecutionResult();

        double makespan = Utils.computeMakespan(schedule);
        double cost = Utils.computeCostGlobal(schedule);

        r.setMakespan(makespan);
        r.setCost(cost);
        r.setId(id);
        r.setAlgorithmName(algo_name);

        return r;
    }
}
