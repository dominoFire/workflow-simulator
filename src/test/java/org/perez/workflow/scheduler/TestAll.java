package org.perez.workflow.scheduler;

import org.junit.Test;
import org.perez.workflow.elements.GEXFConverter;
import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Workflow;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by perez on 25/07/14.
 */
public class TestAll {
    @Test
    public void testAll() {
        int n = 50;
        Workflow w;
        List<Schedule> s1, s2, s3;
        String g1, g2, g3;
        List<Resource> resourceList = Generator.randomResourceList(System.currentTimeMillis(),3, 1, 10);
        Utils.writeObject("resources.obj", resourceList);
        Utils.writeResourceList("resources.csv", resourceList);

        double[] mkMyopic = new double[n],
                mkMinMin = new double[n],
                mkMaxMin = new double[n];
        int[] nnodes = new int[n], nedges = new int[n];
        for(int i=0; i<n; i++) {
            System.out.println("Case " +(i+1));
            w = Generator.randomWorkflow(System.currentTimeMillis(), 10, 12, 1.0, 10.0);
            Utils.writeObject("workflow" +i +".obj", w);
            GEXFConverter.export(GEXFConverter.toGEXF(w), "workflow" + i + ".gexf");
            nnodes[i] = w.getTasks().size();
            nedges[i] = w.getDependencies().size();

            Utils.initResources(resourceList);
            s1 = Myopic.schedule(w, resourceList);
            mkMyopic[i] = Utils.computeMakespan(s1);
            g1 = Utils.createRGanttScript(s1, "Myopic " + i);
            Utils.writeFile("scheduleMyopic" + i + ".R", g1);
            Utils.writeFile("scheduleMyopic"+i+".csv", Utils.echoSchedule(s1));
            assertTrue(Utils.checkValidSchedule(s1));

            Utils.initResources(resourceList);
            s2 = MaxMin.schedule(w, resourceList);
            mkMinMin[i] = Utils.computeMakespan(s2);
            g2 = Utils.createRGanttScript(s2, "MaxMin " + i);
            Utils.writeFile("scheduleMaxMin" + i + ".R", g2);
            Utils.writeFile("scheduleMaxMin"+i+".csv", Utils.echoSchedule(s2));
            assertTrue(Utils.checkValidSchedule(s2));

            Utils.initResources(resourceList);
            s3 = MinMin.schedule(w, resourceList);
            mkMaxMin[i] = Utils.computeMakespan(s3);
            g3 = Utils.createRGanttScript(s3, "MinMin " +i);
            Utils.writeFile("scheduleMinMin" + i + ".R", g3);
            Utils.writeFile("scheduleMinMin"+i+".csv", Utils.echoSchedule(s3));
            assertTrue(Utils.checkValidSchedule(s3));
        }

        StringBuffer sb = new StringBuffer();
        sb.append("nodes,edges,mk_myopic,mk_maxmin,mk_minmin\n");
        for(int i=0; i<n; i++)
            sb.append(String.format("%d,%d,%f,%f,%f\n",
                    nnodes[i], nedges[i], mkMyopic[i], mkMaxMin[i], mkMinMin[i]));
        Utils.writeFile("results.csv", sb.toString());
    }

    @Test
    public void testError() {
        String workflowFile = "workflow3.obj";
        String resurcesFile = "resources.obj";
        Workflow w = Utils.readObject(workflowFile);
        List<Resource> resourceList = Utils.readObject(resurcesFile);
        List<Schedule> s = Myopic.schedule(w, resourceList);
        Utils.printSchedule(s);
        System.out.println(Utils.createRGanttScript(s, "Error"));
        assertTrue(Utils.checkValidSchedule(s));
    }
}