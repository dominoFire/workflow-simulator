package org.perez.workflow.scheduler;

import org.perez.resource.ResourceConfig;
import org.perez.workflow.elements.Task;

import java.util.*;

/**
 * Created by microkid on 9/17/16.
 */
class BinPackingAssigner {
    Task[] tasks;
    ResourceConfig[] resourceConfigs;
    double[][] memCosts;
    int[][] visited;
    int[] used;
    Collection<BinPackingEntry> resourceMappings;
    CostFunction f;

    public BinPackingAssigner(Collection<Task> tasks, Collection<ResourceConfig> resourceConfigs, CostFunction cf) {
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

        if(cf==null)
            throw new IllegalArgumentException("CostFunction cannot be null");
        this.f = cf;

        this.memCosts = new double[tasks.size()][resourceConfigs.size()];
        this.visited = new int[tasks.size()][resourceConfigs.size()];
        this.used = new int[resourceConfigs.size()];
        this.resourceMappings = new ArrayList<>();

        //hint: sort tasks by complexity
        Arrays.sort(this.tasks, (o1, o2) -> -1 * Double.compare(o1.getComplexityFactor(), o2.getComplexityFactor()));
        Arrays.sort(this.resourceConfigs, (o1, o2) -> -1 * Double.compare(o1.getSpeedFactor(), o2.getSpeedFactor()));
    }

    public Collection<BinPackingEntry> getMappings() {
        this.take(0, 0);
        this.checkTake(0, 0);
        return this.resourceMappings;
    }

    /**
     * Memoized-DP stuff
     *
     * @param t_i
     * @param rc_i
     * @return
     */
    protected double take(int t_i, int rc_i) {
        if (rc_i == this.resourceConfigs.length || t_i == this.tasks.length)
            return allScheduled();

        if (this.visited[t_i][rc_i] != 0)
            return this.memCosts[t_i][rc_i];

        for (int y = rc_i; y < this.resourceConfigs.length; y++) {
            ResourceConfig rc = this.resourceConfigs[y];
            int t_lim = Math.min(this.tasks.length, t_i + rc.getCores());
            double task_complexities = 0.;
            double taked_cost = 0.;
            for (int tt_i = t_i; tt_i < t_lim; tt_i++) {
                Task tt = this.tasks[tt_i];
                // Worst case scenario cost
                taked_cost = Math.max(
                        taked_cost,
                        this.f.apply(tt, rc.toResource()));

            }
            this.used[y] += 1;
            double taken = take(t_lim, 0) + taked_cost;
            this.used[y] -= 1;
            double taken_not = take(t_i, y + 1);
            // For single variable optimization, we want to always chose the
            // option with minimum cost (either time or money)
            this.memCosts[t_i][y] = Math.min(taken, taken_not);
            this.visited[t_i][y] = taken < taken_not ? 1 : -1; //1 => Taken
        }

        return this.memCosts[t_i][rc_i];
    }

    /**
     * Checks the 'visited' DP table for build the complete solution (mapping)
     *
     * @param t_i
     * @param rc_i
     */
    protected void checkTake(int t_i, int rc_i) {
        if (t_i < this.tasks.length && rc_i < this.resourceConfigs.length)
            if (this.visited[t_i][rc_i] == 1) {
                ResourceConfig rc = this.resourceConfigs[rc_i];
                int t_lim = Math.min(this.tasks.length, t_i + rc.getCores());
                checkTake(t_lim, 0);
                List<Task> tasks = new ArrayList<>();
                for (int t = t_i; t < t_lim; t++)
                    tasks.add(this.tasks[t]);
                this.resourceMappings.add(new BinPackingEntry(tasks, rc));
            } else if (this.visited[t_i][rc_i] == -1) {
                checkTake(t_i, rc_i + 1);
            }
    }

    /**
     * Returns a 0 <= x <= 1 value if all task have been mapped
     * Otherwise, returns a very large number called MAX_VALUE
     * You can think of this function as an predicate to determine if all tasks have been mapped
     * with a quality degree of that mapping
     *
     * @return Returns a 0 <= x <= 1 value if all task have been mapped
     */
    protected double allScheduled() {
        double sum_cores = 0.;
        double sum_sfs = 0.;
        int n = 0;
        for (int i = 0; i < this.used.length; i++) {
            n = this.used[i];
            if (n != 0) {
                sum_cores += this.resourceConfigs[i].getCores() * n;
                sum_sfs += this.resourceConfigs[i].getSpeedFactor() * n;
            }
        }
        if (sum_cores != this.tasks.length)
            return Double.POSITIVE_INFINITY;

        return 1. / sum_sfs;
    }
}
