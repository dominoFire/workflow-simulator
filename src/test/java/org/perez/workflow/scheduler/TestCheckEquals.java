package org.perez.workflow.scheduler;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Workflow;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

/**
 * Test para comprobar si existen iguales
 */
public class TestCheckEquals
{
    @Test
    public void testEquals()
    {
        Gson gson = Utils.getGSON();
        Workflow w = null;
        try {
            w = gson.fromJson(new FileReader("results-existing/workflow504.obj"), Workflow.class);
        } catch (FileNotFoundException e) {
            System.err.println("No pude leer archivo de workflow: " + e.getMessage());
            Assert.fail();
        }

        List<Schedule> scheduleMakespan = Blind.schedule(w, TestBlind.sampleConfigs(), MakespanCost.create());
        Utils.printSchedule(scheduleMakespan);
        List<Schedule> scheduleCost = Blind.schedule(w, TestBlind.sampleConfigs(), ExecutionCost.create());
        Utils.printSchedule(scheduleCost);

        double mk_cost = Utils.computeMakespan(scheduleCost);
        double mk_makespan = Utils.computeMakespan(scheduleMakespan);
        System.out.println(mk_cost);
        System.out.println(mk_makespan);
    }

    @Test
    public void testInfinity() {
        double v = Double.POSITIVE_INFINITY - 1.0;
        double min_v = Math.min(v, 1);
        System.out.println(v);
        System.out.println(min_v);
    }
}
