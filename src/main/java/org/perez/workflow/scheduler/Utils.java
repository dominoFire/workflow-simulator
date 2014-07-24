package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Task;
import org.perez.workflow.elements.Workflow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by perez on 17/07/14.
 */
public class Utils {
    /** Check if parents (dependencies) of task t are on sched list */
    static boolean checkParents(Task t, ArrayList<Task> sched, Workflow w) {
        //TODO: do we need check parents recursively?
        ArrayList<Task> parents = w.getDependencies(t);
        for(Task parentTask: parents)
            if(!sched.contains(parentTask))
                return false;
        return true;
    }

    static double parentsReadyTime(Task t, List<Schedule> partialSchedule, Workflow w) {
        double parentsReadyTime = 0;
        ArrayList<Task> parents = w.getDependencies(t);
        //asumimos que tenemos tareas unicas y calendarizaciones unicas
        for(Task parentTask: parents)
            for(Schedule s: partialSchedule)
                if(parentTask.equals(s.getTask()))
                    parentsReadyTime = Math.max(parentsReadyTime, s.getStart() + s.getDuration());

        return parentsReadyTime;
    }

    static boolean checkScheduleParams(Workflow w, List<Resource> resourceList) {
        if(w==null)
            throw new IllegalArgumentException("Workflow cannot be null");
        if(resourceList==null)
            throw new IllegalArgumentException("Resource list cannot be null");
        if(resourceList.isEmpty())
            throw new IllegalArgumentException("Resource list is empty");
        if(w.getTasks().isEmpty())
            throw new IllegalArgumentException("Workflow has no tasks");

        return true;
    }

    public static double computeMakespan(List<Schedule> schedule) {
        double makespan_end = Double.MIN_VALUE, makespan_start = Double.MAX_VALUE;
        for(Schedule s: schedule) {
            makespan_start = Math.min(makespan_start, s.getStart());
            makespan_end = Math.max(makespan_end, s.getStart() + s.getDuration());
        }
        return (makespan_end - makespan_start);
    }

    public static void printSchedule(List<Schedule> sched) {
        for(Schedule s: sched) {
            System.out.println(s);
        }
        System.out.println("Makespan: " +computeMakespan(sched));
    }
}
