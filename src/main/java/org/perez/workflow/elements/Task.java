package org.perez.workflow.elements;

import java.io.Serializable;

/**
 * Created by Fernando on 06/07/2014.
 */
public class Task
    implements Serializable
{
    /** Name of task **/
    protected String name;
    /** A number indicating how hard does it take to execute this task,
      * independent of resource (think as 'distance') */
    protected double complexityFactor;

    public Task(String name, double complexityFactor)
        throws NullPointerException, IllegalArgumentException {
        this.setName(name);
        this.setComplexityFactor(complexityFactor);
    }

    public String getName() {
        return name;
    }

    public void setName(String name)
        throws NullPointerException, IllegalArgumentException {
        if(name==null)
            throw new NullPointerException("Task name is null");
        if(name.equals(""))
            throw new IllegalArgumentException("Task name is empty");
        this.name = name;
    }

    public double getComplexityFactor() {
        return complexityFactor;
    }

    public void setComplexityFactor(double complexityFactor)
        throws NullPointerException, IllegalArgumentException {
        if(complexityFactor < 0)
            throw new IllegalArgumentException("Task complexity factor is less than zero");
        //see: http://stackoverflow.com/questions/3728309/difference-among-double-min-normal-and-double-min-value
        if(Math.abs(complexityFactor) < 0.000000001)
            throw new IllegalArgumentException("Task complexity factor is zero");
        this.complexityFactor = complexityFactor;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", complexityFactor=" + complexityFactor +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (!name.equals(task.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
