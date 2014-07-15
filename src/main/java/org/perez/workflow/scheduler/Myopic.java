package org.perez.workflow.scheduler;

import org.perez.workflow.elements.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;


/**
 * Created by Fernando on 06/07/2014.
 */
public class Myopic
{
    public static List<Schedule> schedule(Workflow w, ArrayList<Resource> resourceList) {
        //lista de tareas no calendarizadas
        //TODO: do we need topological sort for tasks?
        ArrayList<Task> readyTasks = new ArrayList<Task>(w.getTasks());
        ArrayList<Task> schedTasks = new ArrayList<Task>();
        ArrayList<Schedule> schedules = new ArrayList<Schedule>();
        //necesito un heap para mantener a los recursos mas pronto disponibles
        PriorityQueue<Resource> R = new PriorityQueue<Resource>(resourceList.size(), EarliestStartTimeComparator.getComp());
        R.addAll(resourceList);
        Task t; Resource r;
        while(!readyTasks.isEmpty()) {
            Iterator<Task> it = readyTasks.iterator();
            if(it.hasNext()) {
                t = it.next();
                if(checkParents(t, schedTasks, w)) {
                    r = R.peek();
                    double d = t.getComplexityFactor() / r.getSpeedFactor();
                    double st = Math.max(r.getReadyTime(), parentsReadyTime(t, schedules, w)); //TODO: agregar componente de padres
                    Schedule s = new Schedule(t, r, d, st);
                    readyTasks.remove(t);
                    schedTasks.add(t);
                    r.addReadyTime(d);
                    //TODO: Update resource priority queue
                    R = new PriorityQueue<Resource>(resourceList.size(), EarliestStartTimeComparator.getComp());
                    R.addAll(resourceList);
                    schedules.add(s);
                }
            }
        }

        return schedules;
    }

    /** Check if parents (dependencies) of task t are on sched list */
    static boolean checkParents(Task t, ArrayList<Task> sched, Workflow w) {
        //TODO: do we need check parents?
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
        for(Task parentTask: parents) {
            for(Schedule s: partialSchedule) {
                if(parentTask.equals(s.getTask())) {
                    parentsReadyTime = Math.max(parentsReadyTime, s.getStart() + s.getDuration());
                }
            }
        }
        return parentsReadyTime;
    }

}