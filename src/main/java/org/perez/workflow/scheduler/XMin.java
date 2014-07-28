package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Task;
import org.perez.workflow.elements.Workflow;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.max;
import static org.perez.workflow.scheduler.Utils.parentsReadyTime;

/**
 * Created by perez on 15/07/14.
 */
public class XMin
{
    public static List<Schedule> schedule(Workflow w, List<Resource> resourceList, XMinAlgorithm algorithm) {
        Utils.checkScheduleParams(w, resourceList);

        ArrayList<Task> schedTasks = new ArrayList<Task>();
        ArrayList<Task> allTasks = new ArrayList<Task>(w.getTasks());
        ArrayList<Task> readyTasks = null;
        List<Schedule> scheduleList = new ArrayList<Schedule>();

        while(!allTasks.isEmpty()) {
            //puedes sacar subconjuntos de todas las tareas
            readyTasks = new ArrayList<Task>();
            for(Task t: allTasks)
                if(!schedTasks.contains(t) && Utils.checkParents(t, schedTasks, w))
                    readyTasks.add(t);
            scheduleList = scheduleXMin(w, resourceList, readyTasks, scheduleList, algorithm);
            for(Schedule s: scheduleList) {
                Task t = s.getTask();
                if(!schedTasks.contains(t))
                    schedTasks.add(t);
                if(allTasks.contains(t))
                    allTasks.remove(t);
            }
        }

        return scheduleList;
    }

    private static List<Schedule> scheduleXMin(Workflow w, List<Resource> resourceList, List<Task> availTasks, List<Schedule> partialSched, XMinAlgorithm algorithm) {
        int n_tasks = availTasks.size(),
                n_resources = resourceList.size();
        double ECT[][] = new double[n_tasks][n_resources];
        double MCT[] = new double[n_tasks];

        int min_mct_r_idx = -1, x_t_idx = -1; double min_mct, x_mct;
        while(!availTasks.isEmpty()) {
            int R_t_idx[] = new int[availTasks.size()];
            for(int i_t=0; i_t<availTasks.size(); i_t++) {
                //todos los recursos pueden ejecutar todas las tareas
                //por eso se usa resourceList
                Task t = availTasks.get(i_t);
                min_mct_r_idx = -1; min_mct = Double.MAX_VALUE;
                for(int i_r=0; i_r<resourceList.size(); i_r++) {
                    Resource r = resourceList.get(i_r);
                    ECT[i_t][i_r] = ECT(t, r, w, partialSched);
                    if(ECT[i_t][i_r] < min_mct) {
                        min_mct = ECT[i_t][i_r];
                        min_mct_r_idx = i_r;
                    }
                }
                MCT[i_t] = min_mct;
                R_t_idx[i_t] = min_mct_r_idx;
            }
            switch(algorithm) {
                case MaxMin:
                    //Max-Min: get a task with maximum ECT ( t , r ) over tasks
                    x_t_idx = -1; x_mct = Double.MIN_VALUE;
                    for(int i_t=0; i_t<availTasks.size(); i_t++) {
                        if(ECT[i_t][R_t_idx[i_t]] > x_mct) {
                            x_mct = ECT[i_t][R_t_idx[i_t]];
                            x_t_idx = i_t;
                        }
                    }
                    break;
                case MinMin:
                    //Min-Min: get a task with minimum ECT ( t , r ) over tasks
                    x_t_idx = -1; x_mct = Double.MAX_VALUE;
                    for(int i_t=0; i_t<availTasks.size(); i_t++) {
                        if(ECT[i_t][R_t_idx[i_t]] < x_mct) {
                            x_mct = ECT[i_t][R_t_idx[i_t]];
                            x_t_idx = i_t;
                        }
                    }
                    break;
            }

            //Schedule T on R_T
            Task t = availTasks.get(x_t_idx);
            Resource r = resourceList.get( R_t_idx[x_t_idx] );
            double duration =  t.getComplexityFactor() / r.getSpeedFactor();
            double startTime = Math.max(r.getReadyTime(), Utils.parentsReadyTime(t, partialSched, w));
            Schedule s = new Schedule(t,r , duration, startTime);
            partialSched.add(s);
            //remote T from availTasks
            availTasks.remove(t);
            //update EAT(R_t)
            r.setReadyTime( startTime );
            r.addReadyTime( duration );
        }

        return partialSched;
    }

    /** Estimated Execution Time : the amount of time the resource r will
     take to execute the task t, from the time the task starts to execute on
     the resource */
    private static double EET(Task t, Resource r) {
        return t.getComplexityFactor() / r.getSpeedFactor();
    }

    /** Estimated Availability Time: The time at which the resource r is
     available to execute task t. */
    private static double EAT(Task t, Resource r) {
        return r.getReadyTime();
    }

    /** File Available Time: the earliest time by which all the files
     required by the task t will be available at the resource r. */
    private static double FAT(Task t, Resource r, Workflow w, List<Schedule> partialSchedule) {
        //le vamos a dar una "semantica" diferente
        //tomamos el tiempo de los padres como el tiempo de archivos listo
        return max(r.getReadyTime(), parentsReadyTime(t, partialSchedule, w));
    }

    /** Estimated Completion Time: the estimated time by which task t
     will complete execution at resource r. */
    private static double ECT(Task t, Resource r, Workflow w, List<Schedule> partialSchedule) {
        return EET(t, r) + max(EAT(t,r), FAT(t,r, w, partialSchedule));
    }

    /** Minimum Estimated Completion Time: Minimum ECT for task t
     over all available resources. */
    private static double MCT(Task t, List<Resource> availResources, Workflow w, List<Schedule> partialSchedule) {
        double res = Double.MAX_VALUE;
        for(Resource r: availResources)
            res = max(res, ECT(t, r, w, partialSchedule));

        return res;
    }
}
