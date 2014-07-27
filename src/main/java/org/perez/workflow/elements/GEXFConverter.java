package org.perez.workflow.elements;

import it.uniroma1.dis.wsngroup.gexf4j.core.*;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.Attribute;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by perez on 24/07/14.
 */
public class GEXFConverter {
    public static Workflow fromGEXF(Gexf gexf) {
        Task t;
        Map<Node, Task> nodos = new HashMap<Node,Task>();
        Workflow w = new Workflow();

        for(Node n: gexf.getGraph().getNodes()) {
            double cf = Double.valueOf(n.getAttributeValues().get(0).getValue());
            t = new Task(n.getLabel(), cf);
            nodos.put(n, t);
            w.addTask(t);
        }
        Task from, to;
        for(Edge e: gexf.getGraph().getAllEdges()) {
            from = nodos.get(e.getSource());
            to = nodos.get(e.getTarget());
            w.addDependency(from, to);
        }
        return w;
    }

    /**
     * Convert a Workflow to a DAG and generates a Gexf object
     * @param w
     * @return
     */
    public static Gexf toGEXF(Workflow w) {
        Map<Task, Node> nodeMap = new HashMap<Task,Node>();
        Gexf gexf = new GexfImpl();
        gexf.getMetadata().setCreator("workflow-simulator");
        gexf.getMetadata().setDescription("Workflow");
        gexf.setVisualization(true);

        Graph g = gexf.getGraph();

        g.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC);
        AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
        g.getAttributeLists().add(attrList);
        Attribute cf = attrList.createAttribute("0", AttributeType.DOUBLE, "complexityFactor");

        Task[] tasks = w.getTasks().toArray(new Task[w.getTasks().size()]);
        for(int i=0; i<tasks.length; i++) {
            Node n = g.createNode(Integer.toString(i));
            n.setLabel(tasks[i].getName());
            n.setSize(30);
            n.getAttributeValues()
                    .addValue(cf, Double.toString(tasks[i].getComplexityFactor()));

            nodeMap.put(tasks[i], n);
        }

        Pair<Task>[] deps = w.getDependencies().toArray(new Pair[w.getDependencies().size()]);
        for(int i=0; i<deps.length; i++) {
            Node from = nodeMap.get(deps[i]._1);
            Node to = nodeMap.get(deps[i]._2);
            Edge edge = from.connectTo(Integer.toString(i), to);
            edge.setEdgeType(EdgeType.DIRECTED);
        }

        return gexf;
    }

    /**
     * Writes a Gexf object to a file
     * @param gexf
     * @param filename
     */
    public static void export(Gexf gexf, String filename) {
        StaxGraphWriter graphWriter = new StaxGraphWriter();
        File f = new File(filename);
        Writer out;
        try {
            out = new FileWriter(f, false);
            graphWriter.writeToStream(gexf, out, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
