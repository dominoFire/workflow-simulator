package org.perez.workflow.scheduler;

import org.perez.resource.ResourceConfig;
import org.perez.workflow.elements.Task;

import java.util.*;

/**
 * Busqueda exhaustiva
 */
public class ExhaustiveBinPackerAssigner
    extends AbstractBinPackingAssigner
{
    private int[] coresUsed;
    private Map<ResourceConfig, List<BinPackingEntry>> resMap;
    private List<BinPackingEntry> mappings;
    private double current_min;

    public ExhaustiveBinPackerAssigner(
            Collection<Task> tasks,
            Collection<ResourceConfig> resourceConfigs,
            CostFunction cf)
    {
        super(tasks, resourceConfigs, cf);
    }

    private double take(int t_i, int rc_i)
    {
        if(rc_i==this.resourceConfigs.length) {
            return Double.POSITIVE_INFINITY;
        }

        if(t_i==this.tasks.length) {
            return checkMapping();
        }

        double taken_yes, taken_not;

        this.pushEntry(t_i, rc_i);
        this.coresUsed[rc_i] += 1;
        taken_yes = take(t_i+1, 0);

        this.coresUsed[rc_i] -= 1;
        this.popEntry(t_i, rc_i);
        taken_not = take(t_i, rc_i+1);

        if(taken_yes==Double.POSITIVE_INFINITY && taken_not==Double.POSITIVE_INFINITY)
            //non-feasible solution
            return Double.POSITIVE_INFINITY;

        if(taken_yes < taken_not)
            return taken_yes;

        return taken_not;
    }

    private double checkMapping()
    {
        //verificamos que se estén ocupando todos los cores
        int sum_cores = 0;
        for(int i=0; i<this.coresUsed.length; i++) {
            if(this.coresUsed[i] > 0) {
                ResourceConfig rc = this.resourceConfigs[i];
                if(this.coresUsed[i] % rc.getCores() != 0)
                    return Double.POSITIVE_INFINITY;

                sum_cores += this.coresUsed[i];
            }
        }

        if(sum_cores!=this.tasks.length)
            return Double.POSITIVE_INFINITY;

        // Calculamos el máximo del costo parcial
        double max_v = 0.;

        for(Collection<BinPackingEntry> li_bpe: this.resMap.values()) {
            for(BinPackingEntry bpe: li_bpe) {
                for(Task t: bpe.task) {
                    max_v = Math.max(max_v, this.f.apply(t, bpe.resourceConfig.toResource()));
                }
            }
        }

        // Agarramos la mejor solución
        if(max_v < this.current_min) {
            //copiamos mejor solucion
            this.mappings.clear();
            for(Collection<BinPackingEntry> li_bpe: this.resMap.values()) {
                for(BinPackingEntry bpe: li_bpe) {
                    List<Task> listTasks = new ArrayList<>();
                    for(Task t: bpe.task) {
                        listTasks.add(t);
                    }
                    BinPackingEntry clon = new BinPackingEntry(listTasks, bpe.resourceConfig);
                    this.mappings.add(clon);
                }
            }
        }

        return  max_v;
    }


    private void pushEntry(int t_i, int rc_i)
    {
        ResourceConfig rc = this.resourceConfigs[rc_i];
        Task t = this.tasks[t_i];

        List<BinPackingEntry> li_bpe = resMap.getOrDefault(rc, new ArrayList<>());
        if(this.coresUsed[rc_i] % rc.getCores() == 0) {
            li_bpe.add(new BinPackingEntry(new ArrayList<>(), rc));
        }
        BinPackingEntry bpe = li_bpe.get(li_bpe.size() - 1);
        bpe.task.add(t);

        resMap.put(rc, li_bpe);
    }

    private void popEntry(int t_i, int rc_i) {
        ResourceConfig rc = this.resourceConfigs[rc_i];
        Task t = this.tasks[t_i];

        List<BinPackingEntry> li_bpe = resMap.get(rc);
        if(li_bpe==null)
            throw new IllegalArgumentException("RC no encontrado");

        BinPackingEntry bpe = li_bpe.get(li_bpe.size() - 1);

        boolean result = bpe.task.remove(t);
        if(!result)
            throw new IllegalArgumentException("Tarea no encontrada");

        if(bpe.task.size()==0)
            li_bpe.remove(bpe);

        if(li_bpe.size()==0)
            this.resMap.remove(rc);
    }

    @Override
    public Collection<BinPackingEntry> getMappings()
    {
        //init
        this.coresUsed = new int[this.resourceConfigs.length];
        for(int i=0; i<this.coresUsed.length; i++)
            this.coresUsed[i] = 0;
        this.mappings = new ArrayList<>();
        this.resMap = new HashMap<>();
        this.current_min = Double.POSITIVE_INFINITY;

        // exhaustive search
        this.take(0, 0);

        return this.mappings;
    }
}
