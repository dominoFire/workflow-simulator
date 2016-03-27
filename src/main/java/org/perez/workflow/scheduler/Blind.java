package org.perez.workflow.scheduler;

import org.perez.resource.ResourceConfig;
import org.perez.workflow.elements.*;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

/**
 * Created by microkid on 30/01/16.
 */
public class Blind
{

    public static List<Schedule> schedule(Workflow w, List<ResourceConfig> resourceConfigs)
    {
        // Transform Workflow into segments
        Map<Task, Integer> segments = Blind.getSegments(w);
        Map<Integer, Set<Task>> segmentList = Blind.toSegmentList(segments);

        // Generate optimum scheduling for each segment
        Map<Integer, Collection<Blind.BinPackingEntry>> mappingsList = new HashMap<>();
        Map<ResourceConfig, List<Integer>> dict_res = new HashMap<>();

        for(Map.Entry<Integer, Set<Task>> e: segmentList.entrySet()) {
            Collection<Blind.BinPackingEntry> mappings = Blind.binPacking(e.getValue(), resourceConfigs);
            mappingsList.put(e.getKey(), mappings);
            System.out.println(mappings);
            // Find out names
            for(Blind.BinPackingEntry pe: mappings) {
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
                names.add(Utils.generateResoureName(0));
            res_names.put(config, names);
        }

        // Build Schedule mappings
        Map<String, Resource> resourceMap = new HashMap<>();
        List<Schedule> scheds = new ArrayList<>();
        double st = 0., st_ant = 0.;
        for(Integer s: mappingsList.keySet()) {
            Collection<Blind.BinPackingEntry> mappings = mappingsList.get(s);
            Map<ResourceConfig, Integer> res_idx = new HashMap<>();
            res_names.forEach((rc, li) -> res_idx.put(rc, 0));
            for(Blind.BinPackingEntry bpe: mappings) {
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

    protected static Collection<BinPackingEntry> binPacking(Collection<Task> tasks, Collection<ResourceConfig> resoureConfigs) {
        BinPackingAssigner bpa = new BinPackingAssigner(tasks, resoureConfigs);

        return bpa.getMappings();
    }

    static class BinPackingAssigner
    {
        Task[] tasks;
        ResourceConfig[] resourceConfigs;
        double[][] memCosts;
        int[][] visited;
        int[] used;
        Collection<BinPackingEntry> resourceMappings;

        public BinPackingAssigner(Collection<Task> tasks, Collection<ResourceConfig> resourceConfigs)
        {
            if(tasks==null || tasks.isEmpty())
                throw new IllegalArgumentException("tasks cannot be empty or null");
            if(resourceConfigs==null || resourceConfigs.isEmpty())
                throw  new IllegalArgumentException("resourceConfigs cannot be null or empty");


            this.tasks = new Task[tasks.size()];
            Iterator<Task> it = tasks.iterator();
            for(int i=0; i<tasks.size(); i++) {
                Task t = it.next();
                if(t==null)
                    throw new IllegalArgumentException(String.format("Task list element %d cannot be null", i));
                this.tasks[i] = t;
            }

            this.resourceConfigs = new ResourceConfig[resourceConfigs.size()];
            Iterator<ResourceConfig> itrc = resourceConfigs.iterator();
            for(int i=0; i<resourceConfigs.size(); i++) {
                ResourceConfig rc = itrc.next();
                if(rc==null)
                    throw new IllegalArgumentException(String.format("ResourceConfig list element %d cannot be null", i));
                this.resourceConfigs[i] = rc;
            }

            this.memCosts = new double[tasks.size()][resourceConfigs.size()];
            this.visited = new int[tasks.size()][resourceConfigs.size()];
            this.used = new int[resourceConfigs.size()];
            this.resourceMappings = new ArrayList<>();
        }

        public Collection<BinPackingEntry> getMappings() {
            this.take(0, 0);
            this.checkTake(0, 0);
            return this.resourceMappings;
        }

        /**
         * Memoized-DP stuff
         * @param t_i
         * @param rc_i
         * @return
         */
        protected double take(int t_i, int rc_i) {
            if(rc_i == this.resourceConfigs.length || t_i == this.tasks.length)
                return allScheduled();

            if(this.visited[t_i][rc_i] != 0)
                return this.memCosts[t_i][rc_i];

            for(int y=rc_i; y<this.resourceConfigs.length; y++) {
                ResourceConfig rc = this.resourceConfigs[y];
                int t_lim = Math.min(this.tasks.length, t_i + rc.getCores());
                double task_complexities = 0.;
                double taked_cost = 0.;
                for(int tt_i=t_i; tt_i<t_lim; tt_i++) {
                    Task tt = this.tasks[tt_i];
                    // TODO: change this line to minimize either cost or time

                    // For minimizing cost
                    taked_cost = Math.max(
                            taked_cost,
                            tt.getComplexityFactor() / rc.getSpeedFactor() * rc.getCost() * 1000);

                    // For minimizing time
                    //taked_cost = Math.max(
                    //        taked_cost,
                    //        tt.getComplexityFactor() / rc.getSpeedFactor());
                }
                this.used[y] += 1;
                double taken = take(t_lim, 0) + taked_cost;
                this.used[y] -= 1;
                double taken_not = take(t_i, y + 1);
                // For single variable optimization, we want to always chose the
                // option with minimum cost (either time or money)
                this.memCosts[t_i][y] = Math.min(taken, taken_not);
                this.visited[t_i][y] = taken < taken_not? 1: -1; //1 => Taken
            }

            return this.memCosts[t_i][rc_i];
        }

        /**
         * Checks the 'visited' DP table for build the complete solution (mapping)
         * @param t_i
         * @param rc_i
         */
        protected void checkTake(int t_i, int rc_i) {
            if(t_i < this.tasks.length && rc_i < this.resourceConfigs.length)
                if(this.visited[t_i][rc_i]==1) {
                    ResourceConfig rc = this.resourceConfigs[rc_i];
                    int t_lim = Math.min(this.tasks.length, t_i + rc.getCores());
                    checkTake(t_lim, 0);
                    List<Task> tasks = new ArrayList<>();
                    for(int t=t_i; t<t_lim; t++)
                        tasks.add(this.tasks[t]);
                    this.resourceMappings.add(new BinPackingEntry(tasks, rc));
                } else if(this.visited[t_i][rc_i]== -1) {
                    checkTake(t_i, rc_i + 1);
                }
        }

        /**
         * Returns a 0 <= x <= 1 value if all task have been mapped
         * Otherwise, returns a very large number called MAX_VALUE
         * You can think of this function as an predicate to determine if all tasks have been mapped
         * with a quality degree of that mapping
         * @return Returns a 0 <= x <= 1 value if all task have been mapped
         */
        protected double allScheduled() {
            double sum_cores = 0.;
            double sum_sfs = 0.;
            int n = 0;
            for(int i=0; i<this.used.length; i++) {
                n = this.used[i];
                if (n != 0) {
                    sum_cores += this.resourceConfigs[i].getCores() * n;
                    sum_sfs += this.resourceConfigs[i].getSpeedFactor() * n;
                }
            }
            if(sum_cores != this.tasks.length)
                return Double.MAX_VALUE;

            return 1./sum_sfs;
        }
    }

    static class BinPackingEntry {
        public List<Task> task;
        public ResourceConfig resourceConfig;

        public BinPackingEntry(List<Task> task, ResourceConfig resourceConfig) {
            this.task = task;
            this.resourceConfig = resourceConfig;
        }
    }

    public static Resource getOrCreate(Map<String, Resource> res_map, String res_name, int core, ResourceConfig rc) {
        String full_name = String.format("%s@Core%d", res_name, core);
        Resource res = null;
        if(res_map.containsKey(full_name))
            res = res_map.get(full_name);
        else {
            res = new Resource(full_name, rc.getSpeedFactor());
            res_map.put(full_name, res);
        }
        return res;
    }
}
