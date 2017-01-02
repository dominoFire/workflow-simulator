package org.perez.workflow.scheduler;

import org.perez.resource.ResourceConfig;
import org.perez.workflow.elements.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * Algoritmo ciego
 *
 */
public class Blind
{
    public static List<Schedule> schedule(Workflow w, List<ResourceConfig> resourceConfigs, CostFunction cf)
    {
        // Transform Workflow into segments
        Map<Task, Integer> segments = Blind.getSegments(w);
        Map<Integer, Set<Task>> segmentList = Blind.toSegmentList(segments);

        // Generate optimum scheduling for each segment
        Map<Integer, Collection<BinPackingEntry>> mappingsList = new HashMap<>();
        Map<ResourceConfig, List<Integer>> allConfigs = new HashMap<>();

        for(Map.Entry<Integer, Set<Task>> e: segmentList.entrySet()) {
            Collection<BinPackingEntry> mappings = Blind.binPacking(e.getValue(), resourceConfigs, cf);
            mappingsList.put(e.getKey(), mappings);
            //System.out.println(mappings);
            // Find out names
            for(BinPackingEntry pe: mappings) {
                List<Integer> segList = allConfigs.containsKey(pe.resourceConfig)?
                        allConfigs.get(pe.resourceConfig):
                        new ArrayList<Integer>();
                segList.add(new Integer(e.getKey()));
                allConfigs.put(pe.resourceConfig, segList);
            }
        }

        // Count how many resources are needed
        Map<ResourceConfig, List<String>> res_names = new HashMap<>();
        for(ResourceConfig config: allConfigs.keySet()) {
            // For each config, find out how many resources are needed
            Map<Integer, Integer> counter = new HashMap<>();
            for(Integer s: allConfigs.get(config))
                counter.merge(s, 1, Integer::sum);
            int res_n = 0;
            for(Integer c: counter.values())
                res_n = Math.max(res_n, c);
            // Generate the resource names
            List<String> names = new ArrayList<>();
            for(int i=0; i<res_n; i++)
                names.add(Utils.generateResourceName(config.getName()));
            res_names.put(config, names);
        }

        // Build Schedule mappings
        Map<String, Resource> resourceMap = new HashMap<>();
        List<Schedule> scheds = new ArrayList<>();
        double d_max = 0., st_ant = 0., d = 0;
        for(int s=1; s<=mappingsList.size(); s++) {
            Collection<BinPackingEntry> mappings = mappingsList.get(s);

            Map<ResourceConfig, Integer> res_idx = new HashMap<>();
            res_names.forEach((rc, li) -> res_idx.put(rc, 0));

            d_max = 0.;
            for(BinPackingEntry bpe: mappings) {
                ResourceConfig rc = bpe.resourceConfig;
                String res_name = res_names.get(rc).get(res_idx.get(rc));
                res_idx.merge(rc, 1, Integer::sum); // res_idx[rc]++;
                for(int j=0; j<bpe.task.size(); j++) {
                    Task t = bpe.task.get(j);
                    Resource r = Blind.getOrCreate(resourceMap, res_name, j, rc);
                    d = t.getComplexityFactor() / r.getSpeedFactor();
                    d_max = Math.max(d, d_max);

                    //Opcion 1
                    double st = Math.max(r.getReadyTime(), Utils.parentsReadyTime(t, scheds, w));
                    r.setReadyTime(st);
                    r.addReadyTime(d);
                    Schedule sched = new Schedule(t, r, d, st);

                    //Opcion 2
                        //Schedule sched = new Schedule(t, r, d, st_ant);

                    scheds.add(sched);
                }
            }
            st_ant = st_ant + d_max;
        }

        return scheds;
    }

    protected static Map<Integer, Set<Task>> toSegmentList(Map<Task, Integer> segments)
    {
        Map<Integer, Set<Task> > segmentList = new HashMap<>();
        Set<Task> tasks;
        int segment;

        for(Entry<Task, Integer> kv: segments.entrySet()) {
            segment = kv.getValue();
            if(segmentList.containsKey(segment)) {
                tasks = segmentList.get(segment);
            } else {
                tasks = new HashSet<>();
                segmentList.put(segment, tasks);
            }
            tasks.add(kv.getKey());
        }

        return segmentList;
    }

    protected static int estimateResources(Map<Task, Integer> segments)
    {
        int max_segment = 0;

        HashMap<Integer, Integer> segmentsHeight = new HashMap<Integer, Integer>();
        int val, segment;

        for(Entry<Task, Integer> kv: segments.entrySet()) {
            segment = kv.getValue();
            if(segmentsHeight.containsKey(segment))
                val = segmentsHeight.get(segment);
            else
                val = 0;
            val += 1;
            segmentsHeight.put(segment, val);
            max_segment = Math.max(val, max_segment);
        }

        return max_segment;
    }

    protected static Map<Task, Integer> getSegments(Workflow w)
    {
        HashMap<Task, Integer> segments = new HashMap<Task, Integer>();
        HashSet<Task> visited = new HashSet<Task>();

        // Build the
        for(Task t: w.getTasks()) {
            findSegment(t, segments, visited);
        }

        return segments;
    }

    protected static int findSegment(Task t, Map<Task, Integer> segments, Set<Task> visited)
    {
        visited.add(t);
        int max_seg = 0;

        if(!segments.containsKey(t)) {
            segments.put(t, 0);
            for(Task p: t.getDependencies()) {
                int v = findSegment(p, segments, visited);
                if(v > max_seg)
                    max_seg = v;
            }
            segments.put(t, max_seg + 1);
        }

        return segments.get(t);
    }

    protected static Collection<BinPackingEntry> binPacking(Collection<Task> tasks, Collection<ResourceConfig> resoureConfigs, CostFunction cf)
    {
        //BinPackingAssigner bpa = new BinPackingAssigner(tasks, resoureConfigs, cf);
        //CompleteBinPackingAssigner bpa = new CompleteBinPackingAssigner(tasks, resoureConfigs, cf);
        ExhaustiveBinPackerAssigner bpa = new ExhaustiveBinPackerAssigner(tasks, resoureConfigs, cf);

        return bpa.getMappings();
    }

    protected static Resource getOrCreate(Map<String, Resource> res_map, String res_name, int core, ResourceConfig rc)
    {
        String full_name = String.format("%s@Core%d", res_name, core);
        Resource res = null;
        if(res_map.containsKey(full_name))
            res = res_map.get(full_name);
        else {
            res = new Resource(full_name, rc.getSpeedFactor());
            res.setCostHour(rc.getCost());
            res_map.put(full_name, res);
        }
        return res;
    }
}
