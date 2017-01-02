package org.perez.workflow.scheduler;

import com.sun.xml.internal.ws.api.pipe.FiberContextSwitchInterceptor;
import org.perez.workflow.elements.Workflow;

/**
 * Tratando de refactorizar
 */
public class WorkflowBenchmarkResult
{
    /** Basename for workflow name */
    private String basename;

    private AlgorithmExecutionResult resultBlind;

    private AlgorithmExecutionResult resultMinmin;

    private AlgorithmExecutionResult resultMaxmin;

    private AlgorithmExecutionResult resultMyopic;

    private Workflow workflow;

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public String getBasename() {
        return basename;
    }
    public void setBasename(String basename) {
        this.basename = basename;
    }

    public AlgorithmExecutionResult getResultBlind() {
        return resultBlind;
    }

    public void setResultBlind(AlgorithmExecutionResult resultMakespanBlind) {
        this.resultBlind = resultMakespanBlind;
    }

    public AlgorithmExecutionResult getResultMinmin() {
        return resultMinmin;
    }

    public void setResultMinmin(AlgorithmExecutionResult resultMinmin) {
        this.resultMinmin = resultMinmin;
    }

    public AlgorithmExecutionResult getResultMaxmin() {
        return resultMaxmin;
    }

    public void setResultMaxmin(AlgorithmExecutionResult resultMaxmin) {
        this.resultMaxmin = resultMaxmin;
    }

    public AlgorithmExecutionResult getResultMyopic() {
        return resultMyopic;
    }

    public void setResultMyopic(AlgorithmExecutionResult resultMyopic) {
        this.resultMyopic = resultMyopic;
    }

    public void setResultByAlgorithm(WorkflowSchedulingAlgorithm wsa, AlgorithmExecutionResult result) {
        switch (wsa.getName()) {
            case "MaxMin":
                this.setResultMaxmin(result);
                break;
            case "MinMin":
                this.setResultMinmin(result);
                break;
            case "Myopic":
                this.setResultMyopic(result);
                break;
            default:
                throw new IllegalArgumentException("Algoritmo incorrecto: " + wsa.getName());
        }
    }

    public boolean isBlindCostWinner()
    {
        double cost = this.getResultBlind().getCost();
        return compareCost(cost);
    }

    public boolean isBlindMakespanWinner()
    {
        double makespan = this.getResultBlind().getMakespan();
        return compareMakespan(makespan);
    }

    public boolean isBlindAbsoluteCostWinner()
    {
        double cost = this.getResultBlind().getCost();
        return compareCostAbsolute(cost);
    }

    public boolean isBlindAbsoluteMakespanWinner()
    {
        double makespan = this.getResultBlind().getMakespan();
        return compareMakespanAbsolute(makespan);
    }

    private boolean compareCostAbsolute(double cost)
    {
        return cost < this.getResultMaxmin().getCost() &&
                cost < this.getResultMinmin().getCost() &&
                cost < this.getResultMyopic().getCost();
    }

    private boolean compareMakespanAbsolute(double makespan)
    {
        return makespan < this.getResultMaxmin().getMakespan() &&
                makespan < this.getResultMinmin().getMakespan() &&
                makespan < this.getResultMyopic().getMakespan();
    }

    private boolean compareMakespan(double makespan)
    {
        return makespan < this.getResultMaxmin().getMakespan() ||
                makespan < this.getResultMinmin().getMakespan() ||
                makespan < this.getResultMyopic().getMakespan();
    }

    private boolean compareCost(double cost)
    {
        return cost < this.getResultMaxmin().getCost() ||
                cost < this.getResultMinmin().getCost() ||
                cost < this.getResultMyopic().getCost();
    }


    public String CSVRow()
    {
        AlgorithmExecutionResult blind = this.getResultBlind();
        AlgorithmExecutionResult maxmin = this.getResultMaxmin();
        AlgorithmExecutionResult minmin = this.getResultMinmin();
        AlgorithmExecutionResult myopic = this.getResultMyopic();
        Workflow w = this.getWorkflow();

        return new StringBuffer()
                .append(getBasename())
                .append(',').append(w.getTasks().size()).append(',').append(w.getDependencies().size())
                .append(',').append(blind.getCost()).append(',').append(blind.getMakespan())
                .append(',').append(myopic.getCost()).append(',').append(myopic.getMakespan())
                .append(',').append(minmin.getCost()).append(',').append(minmin.getMakespan())
                .append(',').append(maxmin.getCost()).append(',').append(maxmin.getMakespan())
                .append(',').append(isBlindCostWinner()).append(',').append(isBlindMakespanWinner())
                .append(',').append(isBlindAbsoluteCostWinner()).append(',').append(isBlindAbsoluteMakespanWinner())
                .toString();
    }

    public static String CSVHeader()
    {
        return  "id" +
                ",num_nodes,num_edges" +
                ",cost_blind,mk_blind" +
                ",cost_myopic,mk_myopic" +
                ",cost_minmin,mk_minmin" +
                ",cost_maxmin,mk_maxmin" +
                ",blind_cost_winner,blind_makespan_winner" +
                ",blind_absolute_cost_winner,blind_absolute_makespan_winner";
    }
}
