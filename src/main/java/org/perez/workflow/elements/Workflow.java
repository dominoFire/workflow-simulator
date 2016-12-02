package org.perez.workflow.elements;

import org.perez.workflow.scheduler.Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Fernando on 06/07/2014.
 */
public class Workflow
    implements Serializable
{
    /** Tasks that integrates workflow */
    protected Set<Task> tasks;
    /** Dependencies between tasks in the format (from, to) */
    protected Set<Pair<Task>> dependencies;

    public Workflow()
    {
        this.tasks = new HashSet<Task>();
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
        //t.setWorkflow(this);
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
        to.addDependency(from);
        from.addSuccessor(to);
    }

    public void removeDependency(Task from, Task to) {
        if(from==null || to==null)
            throw new IllegalArgumentException("Tasks cannot be null");
        if(!this.tasks.contains(from) || !this.tasks.contains(to))
            throw new IllegalArgumentException("Tasks need to be in workflow");

        Pair<Task> dep = new Pair<Task>(from, to);
        if(this.dependencies.contains(dep)) {
            this.dependencies.remove(dep);

            from.removeSuccessor(to);
            to.removeDependency(from);
        }
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public Set<Pair<Task>> getDependencies() {
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

    public boolean hasCycle() {
        Map<Task, List<Task>> adj = constructAdjList();
        Map<Task, Boolean> visited = new HashMap<Task, Boolean>();
        Map<Task, Boolean> painted = new HashMap<Task, Boolean>();
        boolean validPath = true;
        for(Task t:this.tasks)
            if(painted.get(t)==null) {
                visited.clear();
                validPath = visitCycle(adj, t, painted, visited);
                if(!validPath)
                    return true;
            }

        return false;
    }

    /**
     * Does all tasks can be completed ?
     * @return
     */
    public boolean isFullyConnected()
    {
        Map<Task, List<Task>> adj = this.constructUndirectedAdjList();
        Set<Task> visited = new HashSet<>();
        Set<Task> painted = new HashSet<>();
        Task first = tasks.iterator().next();

        this.visitConnected(adj, first, painted, visited);
        for(Task t: this.tasks)
            if(!visited.contains(t))
                return false;

        return true;
    }

    void visitConnected(Map<Task, List<Task>> adj, Task startTask, Set<Task> painted, Set<Task> visited)
    {
        painted.add(startTask);
        visited.add(startTask);

        for(Task t_ch: adj.get(startTask))
            if(!painted.contains(t_ch))
                visitConnected(adj, t_ch, painted, visited);
    }

    //Devuelve true si no tiene ciclo
    boolean visitCycle(Map<Task, List<Task>> adj, Task startTask, Map<Task, Boolean> painted, Map<Task, Boolean> visited) {
        visited.put(startTask, Boolean.TRUE);
        painted.put(startTask, Boolean.TRUE);
        List<Task> neigh = adj.get(startTask);
        for(Task t: neigh) {
            if(visited.get(t)!=null)
                return false;
            if(!visitCycle(adj, t, painted, visited))
                return false;
        }
        visited.remove(startTask);
        return true;
    }

    Map<Task, List<Task>> constructAdjList() {
        Map<Task, List<Task>> adj = new HashMap<Task, List<Task>>();

        for(Pair<Task> p: this.dependencies)
            if(adj.containsKey(p._1)) {
                adj.get(p._1).add(p._2);
            } else {
                List<Task> list = new ArrayList<Task>();
                list.add(p._2);
                adj.put(p._1, list);
            }

        for(Task t: this.tasks)
            if(!adj.containsKey(t))
                adj.put(t, new ArrayList<Task>());

        return adj;

    }

    Map<Task, List<Task>> constructUndirectedAdjList() {
        Map<Task, List<Task>> adj = new HashMap<Task, List<Task>>();

        for(Task t: this.tasks)
            adj.put(t, new ArrayList<Task>());

        for(Pair<Task> p: this.dependencies) {
            adj.get(p._1).add(p._2);
            adj.get(p._2).add(p._1);
        }

        return adj;
    }

    public void write(String objectFile) {
        Utils.writeObject(objectFile, this);
    }

    public static Workflow read(String objectFile) throws IOException, ClassNotFoundException {
        FileInputStream fin = new FileInputStream(objectFile);
        ObjectInputStream ois = new ObjectInputStream(fin);
        Workflow w = (Workflow) ois.readObject();
        return w;
    }

    public String toGraphviz(String title) {
        List<List<Task>> paths = new PathVisitor(this).getPaths();
        StringBuffer sb = new StringBuffer();

        sb.append("digraph")
                .append(" ")
                .append(title)
                .append(" ")
                .append("{\n");

        /*
        for(List<Task> path: paths) {
            for(int i=0; i<path.size(); i++) {
                if(i>0)
                    sb.append(" -> ");
                sb.append(path.get(i).getName());
            }
            sb.append(";\n");
        }*/

        for(Pair<Task> p: this.getDependencies()) {
            sb
                .append(p.get_1().getName())
                .append(" -> ")
                .append(p.get_2().getName())
                .append(";\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Workflow workflow = (Workflow) o;

        if (!tasks.equals(workflow.tasks)) return false;
        return dependencies.equals(workflow.dependencies);

    }

    @Override
    public int hashCode() {
        int result = tasks.hashCode();
        result = 31 * result + dependencies.hashCode();
        return result;
    }

    class PathVisitor
    {
        List<List<Task>> paths;
        Workflow w;
        Stack<Task> currentPath;
        Set<Task> visited;
        Set<Task> colored;

        public PathVisitor(Workflow w)
        {
            this.w = w;
            this.currentPath = new Stack<>();
            this.visited = new HashSet<>();
            this.colored = new HashSet<>();
        }

        void visit(Task t, Map<Task, List<Task>> adjDirected)
        {
            currentPath.push(t);
            this.colored.add(t);

            if(adjDirected.get(t).size()==0) {
                //print path
                int s = currentPath.size();
                List<Task> p = Arrays.asList(currentPath.toArray(new Task[s]));
                this.paths.add(p);
            } else {
                for(Task tt: adjDirected.get(t)) {
                    if(!colored.contains(tt))
                        this.visit(tt, adjDirected);
                }
            }

            this.visited.add(t);
            currentPath.pop();
        }

        public List<List<Task>> getPaths()
        {
            this.currentPath.clear();
            this.visited.clear();
            this.colored.clear();
            this.paths = new ArrayList<>();

            Map<Task, List<Task>> adjDirected = this.w.constructAdjList();
            for(Task t: w.getTasks())
                if(!visited.contains(t)) {
                    this.colored.clear();
                    this.visit(t, adjDirected);
                }


            return this.paths;
        }
    }
}
