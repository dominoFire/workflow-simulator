package org.perez.workflow.scheduler;

import org.perez.resource.ResourceConfig;
import org.perez.workflow.elements.Task;

import java.io.PrintWriter;
import java.util.*;

/**
 * Explores all posibilities
 * In theory, It gives the optimal solution
 * for a segment
 */
public class CompleteBinPackingAssigner
    extends BinPackingAssigner
{
    /** Matrix of all possible costs (t, r) */
    double[][] C;
    /** Times a resource r is used */
    int resourcesUsed[];
    /** The same, buf for checkTake() method */
    int coresUsed[];
    Map<ResourceConfig, List<BinPackingEntry> >  resMap;

    public CompleteBinPackingAssigner(
            Collection<Task> tasks,
            Collection<ResourceConfig> resourceConfigs,
            CostFunction f)
    {
        super(tasks, resourceConfigs, f);
        this.C = new double[this.tasks.length][this.resourceConfigs.length];
        this.computeMatrix();
        this.resourcesUsed = new int[this.resourceConfigs.length];
        this.coresUsed = new int[this.resourceConfigs.length];
        this.resMap = new HashMap<>();
    }


    protected void computeMatrix()
    {
        for(int t=0; t<this.tasks.length; t++) {
            for(int r=0; r<this.resourceConfigs.length; r++) {
                this.C[t][r] = f.apply(this.tasks[t], this.resourceConfigs[r].toResource());
            }
        }
    }


    @Override
    public Collection<BinPackingEntry> getMappings()
    {
        super.getMappings();

        for(ResourceConfig rc: this.resMap.keySet())
            for(BinPackingEntry bpe: this.resMap.get(rc))
                this.resourceMappings.add(bpe);

        //this.printMatrix();

        return this.resourceMappings;
    }

    @Override
    protected double take(int t_i, int rc_i)
    {
        if(rc_i == this.resourceConfigs.length)
            return Double.POSITIVE_INFINITY;

        if (t_i == this.tasks.length)
            return allScheduled();

        this.resourcesUsed[rc_i] += 1;
        this.used[rc_i] += 1;
        double taken = take(t_i+1, 0);
        this.resourcesUsed[rc_i] -= 1;
        this.used[rc_i] -= 1;

        double taken_not = take(t_i, rc_i + 1);

        if(taken_not==Double.POSITIVE_INFINITY &&
                taken==Double.POSITIVE_INFINITY) {
            this.memCosts[t_i][rc_i] = -2;
            this.visited[t_i][rc_i] = -2;

            //non-feasible solution
            return Double.POSITIVE_INFINITY;
        }

        if(taken < taken_not) {
            this.memCosts[t_i][rc_i] = taken;
            this.visited[t_i][rc_i] = 1; //1 => Taken
        } else {
            this.memCosts[t_i][rc_i] = taken_not;
            this.visited[t_i][rc_i] = -1; //-1 => Taken NOT
        }

        return this.memCosts[t_i][rc_i];
    }

    @Override
    protected void checkTake(int t_i, int rc_i)
    {
        if (t_i < this.tasks.length && rc_i < this.resourceConfigs.length)
            if (this.visited[t_i][rc_i] == 1) {
                Task t = this.tasks[t_i];
                ResourceConfig rc = this.resourceConfigs[rc_i];

                List<BinPackingEntry> li_bpe = resMap.getOrDefault(rc, new ArrayList<>());
                if(this.coresUsed[rc_i] % rc.getCores() == 0) {
                    li_bpe.add(new BinPackingEntry(new ArrayList<>(), rc));
                }
                BinPackingEntry bpe = li_bpe.get(li_bpe.size() - 1);
                bpe.task.add(t);

                resMap.put(rc, li_bpe);

                this.coresUsed[rc_i]++;

                checkTake(t_i + 1, 0);
            } else if (this.visited[t_i][rc_i] == -1 || this.visited[t_i][rc_i] == 0) {
                checkTake(t_i, rc_i + 1);
            }
    }

    @Override
    protected double allScheduled()
    {
        int sum_cores = 0;
        int n = 0, nn = 0;
        for (int i = 0; i < this.used.length; i++) {
            ResourceConfig rc = this.resourceConfigs[i];
            n = this.used[i];
            nn = n / rc.getCores();
            if( n % rc.getCores() != 0) {
                //un valor muy grande
                return Double.POSITIVE_INFINITY;
            }
            if (nn != 0) {
                sum_cores += nn * rc.getCores();
            }
        }
        // no es suficiente con checar la suma de los nodos
        if (sum_cores != this.tasks.length)
            //para evitar overflow
            return Double.POSITIVE_INFINITY;

        return 1.;
    }

    public void printMatrix()
    {
        for(int t=0; t<this.tasks.length; t++) {
            for(int r=0; r<this.resourceConfigs.length; r++) {
                System.out.printf("%.3f ", this.C[t][r]);
            }
            System.out.println();
        }
        System.out.println();
    }
}
