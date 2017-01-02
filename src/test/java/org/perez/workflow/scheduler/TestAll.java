package org.perez.workflow.scheduler;

import org.junit.Test;
import org.perez.resource.ResourceConfig;
import org.perez.workflow.elements.GEXFConverter;
import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Workflow;

import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Test para probar todos los algoritmos
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
            //w = Generator.randomWorkflow(System.currentTimeMillis(), 10, 12, 1.0, 10.0);
            w = Generator.connectedRandomWorkflow(System.currentTimeMillis(), 80, 1., 10.);
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

    @Test
    public void testBlindCost() {
        doTestBlind(1000, ExecutionCost.create());
    }

    @Test
    public void testBlindMakespan() {
        doTestBlind(1000, MakespanCost.create());
    }

    void doTestBlind(int n, CostFunction cf) {
        List<ResourceConfig> resourceConfigs = TestBlind.sampleConfigs();
        WorkflowSchedulingAlgorithm[] wfs_algorithms = Algorithms.getAlgorithms();
        StringBuffer sb = new StringBuffer();
        double makespan, makespan_blind, cost, cost_blind;
        boolean blind_mk_winner, blind_cost_winner, blind_absolute_mk_winner, blind_absolute_cost_winner;

        sb.append("id, num_nodes, num_edges, mk_blind, cost_blind, mk_maxmin, cost_maxmin, mk_minmin, cost_minmin, mk_myopic, cost_myopic, wk_connex, blind_makespan_winner, blind_cost_winner, blind_absolute_makespan_winner, blind_absolute_cost_winner\n");

        long global_seed = System.currentTimeMillis();
        Random global_rnd = new Random(global_seed);


        Set<Integer> hashWorkflows = new HashSet<>();

        for(int i=0; i<n; i++) {
            System.out.println("Testing workflow " + i);
            //Configuracion que funciona
            //Workflow w = Generator.randomWorkflow(System.currentTimeMillis(), 10, 18, 50., 100.0);
            boolean continue_generating = true;
            Workflow w;
            long millis;
            do {
                millis = System.currentTimeMillis();
                w = Generator.connectedRandomWorkflow(millis, 1 + global_rnd.nextInt(50), 50., 100.);
                int hash = w.hashCode();
                if(!hashWorkflows.contains(hash)) {
                    hashWorkflows.add(hash);
                    continue_generating = false;
                } else {
                    continue_generating = true;
                }
                System.out.printf("Semilla: %d\n", millis);
            } while(continue_generating);

            //Utils.writeObject("workflow" +i +".obj", w);
            Utils.writeJson(String.format("workflow%d.obj", i), w);
            Utils.writeFile(String.format("workflow%d.dot", i), w.toGraphviz("workflow" + i));
            Utils.writeFile(String.format("workflow%d.seed", i), Long.toString(millis));
            GEXFConverter.export(GEXFConverter.toGEXF(w), "workflow" + i + ".gexf");

            List<Schedule> blindSchedule = Blind.schedule(w, resourceConfigs, cf);
            makespan_blind = Utils.computeMakespan(blindSchedule);
            cost_blind = Utils.computeCostGlobal(blindSchedule);

            Utils.writeFile("schedule" + "Blind" + i + ".R", Utils.createRGanttScript(blindSchedule, "Blind" + i));
            Utils.writeFile("schedule" + "Blind" + i + ".csv", Utils.echoSchedule(blindSchedule));
            assertTrue(Utils.checkValidSchedule(blindSchedule));

            List<Resource> resourceList = Utils.getResourcesFromSchedule(blindSchedule);
            Utils.writeResourceList("resources" + i + ".csv", resourceList);

            sb.append(String.format("%d", i));
            sb.append(',').append(String.format("%d", w.getTasks().size()));
            sb.append(',').append(String.format("%d", w.getDependencies().size()));
            sb.append(',').append(String.format("%.6f", makespan_blind));
            sb.append(',').append(String.format("%.6f", cost_blind));

            System.out.printf("Blind makespan: %.6f, cost: %.6f\n", makespan_blind, cost_blind);

            blind_mk_winner = false;
            blind_cost_winner = false;
            blind_absolute_mk_winner = true;
            blind_absolute_cost_winner = true;

            for(WorkflowSchedulingAlgorithm algo: wfs_algorithms) {
                Utils.initResources(resourceList);

                List<Schedule> scheduleSimple = algo.generateSchedule(w, resourceList);
                makespan = Utils.computeMakespan(scheduleSimple);
                cost = Utils.computeCostGlobal(scheduleSimple);

                blind_mk_winner = blind_mk_winner | makespan_blind <= makespan;
                blind_cost_winner = blind_cost_winner | cost_blind <= cost;
                blind_absolute_mk_winner = blind_absolute_mk_winner &  makespan_blind < makespan;
                blind_absolute_cost_winner = blind_absolute_cost_winner & cost_blind < cost;

                Utils.writeFile("schedule" + algo.getName() + i + ".R", Utils.createRGanttScript(scheduleSimple, algo.getName() + i));
                Utils.writeFile("schedule" + algo.getName() + i + ".csv", Utils.echoSchedule(scheduleSimple));
                assertTrue(Utils.checkValidSchedule(scheduleSimple));
                sb.append(String.format(",%.6f,%.6f", makespan, cost));
                System.out.printf("%s makespan: %.6f, cost: %.6f\n", algo.getName(), makespan, cost);
            }
            sb.append(",").append(w.isFullyConnected());
            sb.append(",").append(blind_mk_winner);
            sb.append(",").append(blind_cost_winner);
            sb.append(",").append(blind_absolute_mk_winner);
            sb.append(",").append(blind_absolute_cost_winner);
            sb.append("\n");
        }
        Utils.writeFile("results.csv", sb.toString());
    }


}
