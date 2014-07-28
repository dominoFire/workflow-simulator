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
    public static List<Schedule> schedule(Workflow w, List<Resource> resourceList) {
        Utils.checkScheduleParams(w, resourceList);
        //lista de tareas no calendarizadas
        //TODO: do we need topological sort for tasks?

        ArrayList<Task> schedTasks = new ArrayList<Task>();
        ArrayList<Task> allTasks = new ArrayList<Task>(w.getTasks());
        ArrayList<Task> readyTasks = null;
        ArrayList<Schedule> scheduleList = new ArrayList<Schedule>();
        //necesito un heap para mantener a los recursos mas pronto disponibles
        PriorityQueue<Resource> R = new PriorityQueue<Resource>(resourceList.size(), EarliestStartTimeComparator.getComp());
        R.addAll(resourceList);
        Task t; Resource r;
        while(!allTasks.isEmpty()) {
            //puedes sacar subconjuntos de todas las tareas
            readyTasks = new ArrayList<Task>();
            for(Task tt: allTasks)
                if(!schedTasks.contains(tt) && Utils.checkParents(tt, schedTasks, w))
                    readyTasks.add(tt);
            Iterator<Task> it = readyTasks.iterator();
            while(it.hasNext()) {
                t = it.next(); r = R.peek();
                double d = t.getComplexityFactor() / r.getSpeedFactor();
                double st = Math.max(r.getReadyTime(), Utils.parentsReadyTime(t, scheduleList, w)); //TODO: agregar componente de padres
                Schedule s = new Schedule(t, r, d, st);
                allTasks.remove(t);
                schedTasks.add(t);
                r.setReadyTime(st);
                r.addReadyTime(d);
                //TODO: Update resource priority queue
                R = new PriorityQueue<Resource>(resourceList.size(), EarliestStartTimeComparator.getComp());
                R.addAll(resourceList);
                scheduleList.add(s);
            }
        }

        return scheduleList;
    }

}