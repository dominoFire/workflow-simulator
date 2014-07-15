package org.perez.workflow.elements;

import java.util.Comparator;

public class EarliestStartTimeComparator
        implements Comparator<Resource>
{

    public static Comparator<Resource> getComp() {
        return new Comparator<Resource>() {
            @Override
            public int compare(Resource o1, Resource o2) {
                return Double.compare(o1.readyTime, o2.readyTime);
            }
        };
    }

    @Override
    public int compare(Resource o1, Resource o2) {
        return Double.compare(o1.readyTime, o2.readyTime);
    }
}
