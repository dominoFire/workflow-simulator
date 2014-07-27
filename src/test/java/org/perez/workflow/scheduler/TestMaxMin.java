package org.perez.workflow.scheduler;

import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import org.junit.Test;
import org.perez.workflow.elements.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by perez on 17/07/14.
 */
public class TestMaxMin {

    @Test
    public void test1() {
        Task t1 = new Task("t1", 5);
        Task t2 = new Task("t2", 10);
        Task t3 = new Task("t3", 4);
        Task t4 = new Task("t4", 8);

        Workflow w = new Workflow();
        w.addTask(t1);
        w.addTask(t2);
        w.addTask(t3);
        w.addTask(t4);
        w.addDependency(t1,t2);
        w.addDependency(t1,t3);
        w.addDependency(t3,t4);
        w.addDependency(t2,t4);
        assertFalse(w.hasCycle());

        ArrayList<Resource> resources = new ArrayList<Resource>();
        resources.add(new Resource("r1", 5));
        resources.add(new Resource("r2", 10));

        System.out.println("MaxMin");
        Utils.printSchedule(MaxMin.schedule(w, resources));
    }

    @Test
    public void testRandom() {
        ArrayList<Resource> resources = new ArrayList<Resource>();
        resources.add(new Resource("r1", 5));
        resources.add(new Resource("r2", 10));
        resources.add(new Resource("r3", 4));

        int n = 10;
        for(int i=0; i<n; i++) {
            System.out.println("Case " +i);
            for(Resource r: resources) //init
                r.setReadyTime(0);
            Workflow w = Generator.randomWorkflow(System.currentTimeMillis(), 10, 12, 1.0, 10.0);
            List<Schedule> s = MaxMin.schedule(w, resources);
            Utils.printSchedule(s);
            System.out.println(Utils.createRGanttScript(s, "MaxMin " +i));
            if(!Utils.checkValidSchedule(s))
                assertTrue("Erroneous schedule", false);

            Gexf gexf = GEXFConverter.toGEXF(w);
            GEXFConverter.export(gexf, i + ".gexf");
        }
    }
}
