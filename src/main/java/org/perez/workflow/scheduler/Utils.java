package org.perez.workflow.scheduler;

import org.perez.workflow.elements.*;

import java.io.*;
import java.util.*;

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

    /**
     * Determines wheter a schedule is correct
     * @param schedule
     * @return
     */
    public static boolean checkValidSchedule(List<Schedule> schedule) {
        if(schedule==null)
            throw new RuntimeException("schedule is null");

        Map<Resource, List< Pair<Double>>> r_t = new HashMap< Resource, List<Pair<Double>> >();
        for(Schedule s: schedule) {
            Resource r = s.getResource(); Task t = s.getTask();
            //TODO: check Schedule object properties
            if(!r_t.containsKey(r))
                r_t.put(r, new ArrayList<Pair<Double>>());
            r_t.get(r).add(new Pair<Double>(s.getStart(), s.getStart() + s.getDuration()));
        }
        for(Resource r: r_t.keySet()) {
            List<Pair<Double>> l_t = r_t.get(r);
            Pair<Double>[] arr = l_t.toArray(new Pair[l_t.size()]);
            Arrays.sort(arr, new Comparator<Pair<Double>>() {
                @Override
                public int compare(Pair<Double> o1, Pair<Double> o2) {
                    int r1 = Double.compare(o1._1, o2._1);
                    if(r1==0)
                        return Double.compare(o1._2, o2._2);
                    return r1;
                }
            });
            //inicia en menor a 0, rareza
            if(arr.length>0 && arr[0]._1 <0)
                return false;

            for(int i=1; i<arr.length; i++)
                if(arr[i-i]._2 > arr[i]._1) // se traslapan
                    return false;
        }

        return true;
    }

    public static String createRGanttScript(List<Schedule> schedule, String plotTitle) {
        StringBuffer
                sb = new StringBuffer(),
                sbResources = new StringBuffer(),
                sbIni = new StringBuffer(),
                sbFin = new StringBuffer(),
                sbCoord = new StringBuffer(),
                sbLabels = new StringBuffer();

        double min_ini = Double.MAX_VALUE, max_fin = Double.MIN_VALUE, ini, fin;
        Task t; Resource r;
        Map<Resource, Integer> coordRes = new HashMap<Resource, Integer>();
        int ncoord = 0;

        //generamos tareas
        sb.append("library(plotrix)\n");
        sbResources.append("labels=c(");
        sbIni.append("starts=c(");
        sbFin.append("ends=c(");
        sbCoord.append("c(");
        sbLabels.append("c(");

        //creamos arreglo ordenado por nombre de recurso
        Schedule[] arr = schedule.toArray(new Schedule[schedule.size()]);
        Arrays.sort(arr, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule o1, Schedule o2) {
                return o1.getResource().getName().compareTo(o2.getResource().getName());
            }
        });
        if(arr.length>0) {
            ini = arr[0].getStart(); fin = arr[0].getEnd();
            t = arr[0].getTask(); r = arr[0].getResource();
            min_ini = Math.min(min_ini, ini); max_fin = Math.max(max_fin, fin);
            sbIni.append( String.format("%.2f", ini) );
            sbFin.append( String.format("%.2f", fin) );
            sbResources.append("\"" +r.getName() +"\"");
            if(!coordRes.containsKey(r))
                coordRes.put(r, ++ncoord);
            sbCoord.append(coordRes.get(r));
            sbLabels.append("\"" +t.getName() +"\"");
            for(int i=1; i<arr.length; i++) {
                ini = arr[i].getStart(); fin = arr[i].getEnd();
                t = arr[i].getTask(); r = arr[i].getResource();
                min_ini = Math.min(min_ini, ini); max_fin = Math.max(max_fin, fin);
                sbIni.append( String.format(",%.2f", ini) );
                sbFin.append( String.format(",%.2f", fin) );
                sbResources.append(",\"" +r.getName() +"\"");
                if(!coordRes.containsKey(r))
                    coordRes.put(r, ++ncoord);
                sbCoord.append("," +coordRes.get(r));
                sbLabels.append(",\"" +t.getName() +"\"");
            }
        }
        sbResources.append(")");
        sbIni.append(")");
        sbFin.append(")");
        sbCoord.append(")");
        sbLabels.append(")");

        sb.append(String.format("schedList <- list(\n\t%s,\n\t%s,\n\t%s)\n",
                sbResources.toString(), sbIni.toString(), sbFin.toString()));
        int i_ini = (int)Math.floor(min_ini), i_fin = (int)Math.ceil(max_fin);
        sb.append(String.format("pdf('%s', width=6.53, height=3.71)\n", plotTitle +".pdf"));
        sb.append(String.format("gantt.chart(schedList, vgridpos=%d:%d, vgridlab=%d:%d, taskcolors=rainbow(%d), main=\"%s\")\n",
                i_ini, i_fin, i_ini, i_fin, arr.length, plotTitle));

        sb.append(String.format("text(x=schedList$starts+0.05,y=%d-%s+1,labels=%s)\n", coordRes.size(), sbCoord.toString(), sbLabels.toString()));
        sb.append("dev.off()\n");

        return sb.toString();
    }

    public static void writeObject(String objectFile, Object o) {
        try {
            OutputStream file = new FileOutputStream(objectFile);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(o);
            output.close();
        } catch (IOException e) {
            System.err.println("Cannot write object file: " +e.getMessage());
            System.exit(1);
        }
    }

    public static <T> T readObject(String objectFile) {
        T w = null;
        try {
            FileInputStream fin = new FileInputStream(objectFile);
            ObjectInputStream ois = new ObjectInputStream(fin);
            w = (T) ois.readObject();
        } catch (IOException ioEx) {
            System.err.println("Cannot read object file: " + ioEx);
            System.exit(1);
        } catch (ClassNotFoundException cnfEx) {
            System.err.println("Cannot read object file: " + cnfEx);
            System.exit(1);
        }
        return w;
    }

    public static void writeFile(String filename, String content) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename);
            pw.write(content);
            pw.close();
        } catch (FileNotFoundException e) {
            System.err.println("Cannot writeFile: " +e.getMessage());
            System.exit(1);
        }
    }

    public static void writeResourceList(String filename, List<Resource> resourceList) {
        StringBuffer sb = new StringBuffer();
        sb.append("name,speedFactor,readyTime\n");
        for(Resource r: resourceList)
            sb.append(String.format("\"%s\",%f,%f\n",
                    r.getName(), r.getSpeedFactor(), r.getReadyTime()));
        writeFile(filename, sb.toString());
    }

    static void initResources(List<Resource> list) {
        for(Resource r: list)
            r.setReadyTime(0);
    }
}
