package org.perez.workflow.elements;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Fernando on 06/07/2014.
 */
public class Workflow
{
    /** Tasks that integrates workflow */
    protected ArrayList<Task> tasks;
    /** Dependencies between tasks in the format (from, to) */
    protected HashSet<Pair<Task>> dependencies;

    public Workflow()
    {
        this.tasks = new ArrayList<Task>();
        this.dependencies = new HashSet<Pair<Task>>();
    }

    /**
     * @param t
     * @return
     */
    public ArrayList<Task> getDependencies(Task t) {
        ArrayList<Task> depsList = new ArrayList<Task>();
        for(Pair<Task> p: this.dependencies) {
            if(p._2!=null && p._2.equals(t))
                depsList.add(p._1);
        }
        return depsList;
    }

    public void addTask(Task t) {
        if(t==null)
            throw new IllegalArgumentException("Task cannot be null");
        if(tasks.contains(t))
            throw new IllegalArgumentException("Task has already been added");

        this.tasks.add(t);
    }

    public void addDependency(Task from, Task to) {
        if(from==null || to==null)
            throw new IllegalArgumentException("Tasks cannot be null");
        if(!this.tasks.contains(from) || !this.tasks.contains(to))
            throw new IllegalArgumentException("Tasks need to be in workflow");

        //TODO: check no-cycles
        //TODO: check existent dependencies
        Pair<Task> dep = new Pair<Task>(from, to);
        if(this.dependencies.contains(dep))
            throw new IllegalArgumentException("This dependency has already been added");

        this.dependencies.add(dep);
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public HashSet<Pair<Task>> getDependencies() {
        return dependencies;
    }

    public void setDependencies(HashSet<Pair<Task>> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public String toString() {
        return "Workflow{" +
                "tasks=" + tasks +
                ", dependencies=" + dependencies +
                '}';
    }
}
