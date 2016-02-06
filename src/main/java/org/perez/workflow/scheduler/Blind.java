package org.perez.workflow.scheduler;

import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Task;
import org.perez.workflow.elements.Workflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by microkid on 30/01/16.
 */
public class Blind
{

    public static List<Schedule> schedule(Workflow w, List<Resource> resourceList) {
        return null;
    }



    public static int estimateResources(Workflow w) {
        HashMap<Task, Integer> segments = new HashMap<Task, Integer>();
        HashSet<Task> visited = new HashSet<Task>();
        int max_segment = 0;

        // Build the
        for(Task t: w.getTasks()) {
            findSegment(t, segments, visited);
        }

        HashMap<Integer, Integer> segmentsHeight = new HashMap<Integer, Integer>();
        int val, segment;

        for(Map.Entry<Task, Integer> kv: segments.entrySet()) {
            segment = kv.getValue();
            if(segmentsHeight.containsKey(segment))
                val = segmentsHeight.get(segment);
            else
                val = 0;
            val += 1;
            segmentsHeight.put(segment, val);
            max_segment = Math.max(val, max_segment);
        }

        return max_segment;
    }

    private static int findSegment(Task t, HashMap<Task, Integer> segments, HashSet<Task> visited)
    {
        visited.add(t);
        int max_seg = 0;

        if(!segments.containsKey(t)) {
            segments.put(t, 0);
            for(Task p: t.getDependencies()) {
                int v = findSegment(p, segments, visited);
                if(v > max_seg)
                    max_seg = v;
            }
            segments.put(t, max_seg + 1);
        }

        return segments.get(t);
    }
}
