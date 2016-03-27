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

    public static List<Schedule> schedule(Workflow w, List<Resource> resourceList)
    {
        return null;
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

    protected static void binPacking(List<Task> tasks, List<ResourceConfig> resoureConfigs) {
        BinPackingAssigner bpa = new BinPackingAssigner(tasks, resoureConfigs);

        bpa.getMappings();
    }

    static class BinPackingAssigner
    {
        Task[] tasks;
        ResourceConfig[] resourceConfigs;
        double[][] memCosts;
        int[][] visited;
        int[] used;
        List<BinPackingEntry> resourceMappings;

        public BinPackingAssigner(List<Task> tasks, List<ResourceConfig> resourceConfigs)
        {
            if(tasks==null || tasks.isEmpty())
                throw new IllegalArgumentException("tasks cannot be empty or null");
            if(resourceConfigs==null || resourceConfigs.isEmpty())
                throw  new IllegalArgumentException("resourceConfigs cannot be null or empty");

            this.tasks = new Task[tasks.size()];
            for(int i=0; i<tasks.size(); i++) {
                if(tasks.get(i)==null)
                    throw new IllegalArgumentException(String.format("Task list element %d cannot be null", i));
                this.tasks[i] = tasks.get(i);
            }

            this.resourceConfigs = new ResourceConfig[resourceConfigs.size()];
            for(int i=0; i<resourceConfigs.size(); i++) {
                if(resourceConfigs.get(i)==null)
                    throw new IllegalArgumentException(String.format("ResourceConfig list element %d cannot be null", i));
                this.resourceConfigs[i] = resourceConfigs.get(i);
            }

            this.memCosts = new double[tasks.size()][resourceConfigs.size()];
            this.visited = new int[tasks.size()][resourceConfigs.size()];
            this.used = new int[resourceConfigs.size()];
        }

        public List<BinPackingEntry> getMappings() {
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
                    taked_cost = Math.max(
                            taked_cost,
                            tt.getComplexityFactor() / rc.getSpeedFactor() * rc.getCost() * 1000);
                }
                this.used[y] += 1;
                double taken = take(t_lim, 0) + taked_cost;
                this.used[y] -= 1;
                double taken_not = take(t_i, y + 1);
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
}
