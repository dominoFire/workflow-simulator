import org.junit.Test;
import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Task;
import org.perez.workflow.elements.Workflow;
import org.perez.workflow.scheduler.Myopic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by perez on 14/07/14.
 */
public class TestMyopic {

    @Test
    public void test1() {
        Workflow w = new Workflow();

        Task t1 = new Task("t1", 5);
        Task t2 = new Task("t2", 10);
        Task t3 = new Task("t3", 4);
        Task t4 = new Task("t4", 8);

        w.addTask(t1);
        w.addTask(t2);
        w.addTask(t3);
        w.addTask(t4);

        w.addDependency(t1,t2);
        w.addDependency(t1,t3);
        w.addDependency(t3,t4);
        w.addDependency(t2,t4);

        ArrayList<Resource> resources = new ArrayList<Resource>();
        resources.add(new Resource("r1", 5));
        resources.add(new Resource("r2", 10));

        List<Schedule> sched = Myopic.schedule(w, resources);
        for(Schedule s: sched) {
            System.out.println(s);
        }
    }
}
