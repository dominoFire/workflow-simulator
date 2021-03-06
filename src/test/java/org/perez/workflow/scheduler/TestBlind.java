package org.perez.workflow.scheduler;

import org.junit.Test;
import org.perez.resource.ResourceConfig;
import org.perez.workflow.elements.*;

import java.util.*;

/**
 * Created by microkid on 31/01/16.
 */
public class TestBlind {

    @Test
    public void testEstimateResources() {
        Workflow w = TestWorkflow.generateSimpleWorkflow();

        Map<Task, Integer> segments = Blind.getSegments(w);

        int maxResources = Blind.estimateResources(segments);

        org.junit.Assert.assertEquals(2, maxResources);
    }


    @Test
    public void testBinPacking() {
        Workflow w = TestWorkflow.generateSimpleWorkflow();

        Map<Task, Integer> segments = Blind.getSegments(w);

        Map<Integer, Set<Task>> segmentList = Blind.toSegmentList(segments);

        List<ResourceConfig> resourceConfigs = this.sampleConfigs();
        Map<Integer, Collection<BinPackingEntry>> mappings = new HashMap<>();

        for(Map.Entry<Integer, Set<Task>> e: segmentList.entrySet()) {
            Collection<BinPackingEntry> m = Blind.binPacking(e.getValue(), resourceConfigs, ExecutionCost.create());
            mappings.put(e.getKey(), m);
            System.out.println(m);
        }
    }

    @Test
    public void testMerge() {
        Workflow w = TestWorkflow.generateSimpleWorkflow();

        Map<Task, Integer> segments = Blind.getSegments(w);

        Map<Integer, Set<Task>> segmentList = Blind.toSegmentList(segments);

        List<ResourceConfig> resourceConfigs = this.sampleConfigs();
        Map<Integer, Collection<BinPackingEntry>> mappingsList = new HashMap<>();
        Map<ResourceConfig, List<Integer>> dict_res = new HashMap<>();


        for(Map.Entry<Integer, Set<Task>> e: segmentList.entrySet()) {
            Collection<BinPackingEntry> mappings = Blind.binPacking(e.getValue(), resourceConfigs, ExecutionCost.create());
            mappingsList.put(e.getKey(), mappings);
            System.out.println(mappings);
            // Find out names
            for(BinPackingEntry pe: mappings) {
                List<Integer> coll = dict_res.containsKey(pe.resourceConfig)? dict_res.get(pe.resourceConfig): new ArrayList<Integer>();
                coll.add(e.getKey());
                dict_res.put(pe.resourceConfig, coll);
            }
        }

        // Count how many resources are needed
        Map<ResourceConfig, List<String>> res_names = new HashMap<>();
        for(ResourceConfig config: dict_res.keySet()) {
            Map<Integer, Integer> counter = new HashMap<>();
            for(Integer s: dict_res.get(config))
                counter.merge(s, 1, Integer::sum);
            int res_n = 0;
            for(Integer c: counter.values())
                res_n = Math.max(res_n, c);
            // Generate the resource names
            List<String> names = new ArrayList<>();
            for(int i=0; i<res_n; i++)
                names.add(Utils.generateResourceName(0));
            res_names.put(config, names);
        }

        // Build Schedule mappings
        Map<String, Resource> resourceMap = new HashMap<>();
        List<Schedule> scheds = new ArrayList<>();
        double st = 0., st_ant = 0.;
        for(Integer s: mappingsList.keySet()) {
            Collection<BinPackingEntry> mappings = mappingsList.get(s);
            Map<ResourceConfig, Integer> res_idx = new HashMap<>();
            res_names.forEach((rc, li) -> res_idx.put(rc, 0));
            for(BinPackingEntry bpe: mappings) {
                ResourceConfig rc = bpe.resourceConfig;
                String res_name = res_names.get(rc).get(res_idx.get(rc));
                res_idx.merge(rc, 1, Integer::sum);
                st = 0.;
                for(int j=0; j<bpe.task.size(); j++) {
                    Task t = bpe.task.get(j);
                    double d = t.getComplexityFactor() / rc.getSpeedFactor();
                    st = Math.max(d, st);
                    Resource r = Blind.getOrCreate(resourceMap, res_name, j+1, rc);
                    Schedule sched = new Schedule(t, r, d, st_ant);
                    scheds.add(sched);
                }
            }
            st_ant = st_ant + st;
        }

        for(Schedule s: scheds)
            System.out.println(s);
    }

    @Test
    public void testBlind() {
        Workflow w = TestWorkflow.generateSimpleWorkflow();
        List<ResourceConfig> resourceConfigs = this.sampleConfigs();
        List<Schedule> schedules = Blind.schedule(w, resourceConfigs, ExecutionCost.create());

        for(Schedule s: schedules)
            System.out.println(s);
    }

    public static List<ResourceConfig> sampleConfigs() {
        List<ResourceConfig> resourceConfigs = new ArrayList<>();

        /*
        resourceConfigs.add(new ResourceConfig("Small", 1, 768., "TestCloud", 100, 2.3));
        resourceConfigs.add(new ResourceConfig("Medium", 2, 4096., "TestCloud", 200, 4.6));
        resourceConfigs.add(new ResourceConfig("Large", 4, 8192., "TestCloud", 400, 9.2));
        resourceConfigs.add(new ResourceConfig("ExtraLarge", 8, 2 * 8192., "TestCloud", 800, 18.4));
        */

        resourceConfigs.add(new ResourceConfig("Small", 1, 768., "TestCloud", 100, 2.3));
        resourceConfigs.add(new ResourceConfig("Medium", 2, 4096., "TestCloud", 200, 4.0));
        resourceConfigs.add(new ResourceConfig("Large", 6, 4096. + 1024., "TestCloud", 400, 7.0));
        resourceConfigs.add(new ResourceConfig("ExtraLarge", 8, 4 * 8192., "TestCloud", 800, 10.0));


        /*
        resourceConfigs.add(new ResourceConfig("Small", 1, 768., "TestCloud", 100, 4.6));
        resourceConfigs.add(new ResourceConfig("Medium", 1, 4096., "TestCloud", 300, 4.6));
        resourceConfigs.add(new ResourceConfig("Large", 1, 8192., "TestCloud", 600, 9.2));
        resourceConfigs.add(new ResourceConfig("ExtraLarge", 1, 2 * 8192., "TestCloud", 2400, 18.4));
        */

        return  resourceConfigs;
    }
}
