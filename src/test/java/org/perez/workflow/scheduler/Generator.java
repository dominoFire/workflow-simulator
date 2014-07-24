package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Task;
import org.perez.workflow.elements.Workflow;

import java.util.Random;

/**
 * Created by perez on 23/07/14.
 */
public class Generator
{
    /** Generate a random DAG with a very naive algorithm */
    public static Workflow generateRandom(long seed, int n_nodes, int n_edges, double min_cf, double max_cf) {
        Random rnd = new Random(seed);
        Task[] tasks = new Task[n_nodes];
        Workflow w = new Workflow();
        for(int i=0; i<n_nodes; i++) {
            tasks[i] = new Task("t" + i, min_cf + (max_cf - min_cf) * rnd.nextDouble());
            w.addTask(tasks[i]);
        }

        int from, to;
        for(int i=0; i<n_edges; ) {
            from = rnd.nextInt(n_nodes);
            to = rnd.nextInt(n_nodes);
            try {
                w.addDependency(tasks[from], tasks[to]);
                if(w.hasCycle())
                    w.removeDependency(tasks[from], tasks[to]);
                else
                    i++;
            } catch(Exception e) {

            }
        }

        return w;
    }
}
