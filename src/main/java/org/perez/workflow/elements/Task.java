package org.perez.workflow.elements;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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

    /**
     *
     */
    protected Set<Task> successors;

    protected Set<Task> dependencies;


    protected Workflow workflow;

    /**
     *
     * @param name
     * @param complexityFactor
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */

    public Task(String name, double complexityFactor)
        throws NullPointerException, IllegalArgumentException {
        this.setName(name);
        this.setComplexityFactor(complexityFactor);
        this.dependencies = new HashSet<Task>();
        this.successors = new HashSet<Task>();
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

    public Set<Task> getSuccessors() {
        return successors;
    }

    public void setSuccessors(Set<Task> successors) {
        this.successors = successors;
    }

    public Set<Task> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<Task> dependencies) {
        this.dependencies = dependencies;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public void addDependency(Task t) {
        this.checkNullOrSame(t);
        if(this.dependencies.contains(t))
            throw new IllegalArgumentException("This dependency has already been added");

        this.dependencies.add(t);
    }


    public void removeDependency(Task t) {
        this.checkNullOrSame(t);
        if(!this.dependencies.contains(t))
            throw new IllegalArgumentException("Task not in task's dependencies");

        this.dependencies.remove(t);
    }

    public void addSuccessor(Task t) {
        this.checkNullOrSame(t);
        if(this.successors.contains(t))
            throw new IllegalArgumentException("This successor has already been added");

        this.successors.add(t);
    }

    public void removeSuccessor(Task t) {
        this.checkNullOrSame(t);
        if(!this.successors.contains(t))
            throw new IllegalArgumentException("Task not in task's successors");

        this.successors.remove(t);
    }

    private void checkNullOrSame(Task t) {
        if(t==null)
            throw new IllegalArgumentException("Task cannot be null");
        if(this==t)
            throw new IllegalArgumentException("Task can't be the same than the caller");
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
