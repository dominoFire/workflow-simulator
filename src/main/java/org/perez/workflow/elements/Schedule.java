package org.perez.workflow.elements;

/**
 * Created by Fernando on 06/07/2014.
 * Represents an entry in a scheduling.
 */
public class Schedule
{
    protected Task task;
    protected Resource resource;
    protected double duration;
    protected double start;

    public Schedule(Task t, Resource r, double duration, double start)
    {
        this.task = t;
        this.resource = r;
        this.duration = duration;
        this.start = start;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "task=" + task.getName() +
                ", resource=" + resource.getName() +
                ", duration=" + duration +
                ", start=" + start +
                '}';
    }
}
