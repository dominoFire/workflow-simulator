package org.perez.workflow.scheduler;

import com.google.gson.Gson;
import org.junit.Test;
import org.perez.resource.ResourceConfig;
import org.perez.workflow.elements.Resource;
import org.perez.workflow.elements.Schedule;
import org.perez.workflow.elements.Workflow;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.perez.workflow.scheduler.Algorithms.getAlgorithms;

/**
 * Test para probar resultados existentes
 */
public class TestExisting
{

    @Test
    public void testWithExistingWorkflows()
    {
        testExisting("results-existing", MakespanCost.create(), "results-existing-makespan-4");
        testExisting("results-existing", ExecutionCost.create(), "results-existing-cost-4");
        testExisting("results-existing", SquaredExecutionCost.create(), "results-existing-squaredcost-4");
    }

    /**
     * Hace un test de todos los algoritmos sobre un flujo de trabajo
     * evaluando ambs funciones de costo del algoritmo ciego
     * @param w
     */
    WorkflowBenchmarkResult doTestAll(Workflow w, String basename, List<ResourceConfig> sampleConfigs, CostFunction cf, String outputFolderPath)
    {
        WorkflowBenchmarkResult bench = new WorkflowBenchmarkResult();
        bench.setBasename(basename);
        bench.setWorkflow(w);

        String codenameBlind = basename + "Blind";
        List<Schedule> scheduleBlind = Blind.schedule(w, sampleConfigs, cf);
        saveSchedule(scheduleBlind, codenameBlind, outputFolderPath);
        AlgorithmExecutionResult resultBlindMakespan = TestUtils.fill(scheduleBlind, scheduleBlind.hashCode(), codenameBlind);
        bench.setResultBlind(resultBlindMakespan);

        List<Resource> resources = Utils.getResourcesFromSchedule(scheduleBlind);
        Utils.writeResourceList( outputFolderPath + "/resources" + basename + ".csv", resources);

        for(WorkflowSchedulingAlgorithm algo: getAlgorithms()) {
            Utils.initResources(resources);

            String codename = basename + algo.getName();
            List<Schedule> scheduleAlgo = algo.generateSchedule(w, resources);
            saveSchedule(scheduleAlgo, codename, outputFolderPath);
            AlgorithmExecutionResult resultAlgo = TestUtils.fill(scheduleAlgo, scheduleAlgo.hashCode(), codename);
            bench.setResultByAlgorithm(algo, resultAlgo);
        }

        bench.setBasename(basename);

        return bench;
    }

    void saveSchedule(List<Schedule> sched, String basename, String outputFolderPath)
    {
        assertTrue(Utils.checkValidSchedule(sched));

        Utils.writeFile( outputFolderPath + "/schedule" + basename + ".R", Utils.createRGanttScript(sched, "schedule" + basename));
        Utils.writeFile( outputFolderPath + "/schedule" + basename + ".csv", Utils.echoSchedule(sched));
    }

    void testExisting(String inputFolderPath, CostFunction cf, String outputFolderPath)
    {
        WorkflowJSONFileVisitor finder = new WorkflowJSONFileVisitor();

        try {
            Files.walkFileTree(Paths.get(inputFolderPath), finder);
        } catch (IOException ex) {
            System.err.println("No pude recorrer el folder de entrada: " + ex.getLocalizedMessage());
            System.exit(1);
        }

        Gson gson = Utils.getGSON();
        String outputCSVPath = outputFolderPath + "/ResultsGeneral.csv";
        try (PrintWriter pw = new PrintWriter(outputCSVPath)) {

            pw.println(WorkflowBenchmarkResult.CSVHeader());

            for(Path fp: finder.getValidPaths()) {
                System.out.println("Procesando: " + fp.toString());
                Workflow w = gson.fromJson(new FileReader(fp.toAbsolutePath().toString()), Workflow.class);

                String basename = fp.getFileName().toString().replaceFirst("[.][^.]+$", "");
                WorkflowBenchmarkResult r_cost = doTestAll(w, basename, TestBlind.sampleConfigs(), cf, outputFolderPath);
                pw.println(r_cost.CSVRow());
            }
        } catch (FileNotFoundException e) {
            System.err.println("No pude crear archivo de resultados '" + outputCSVPath + "'");
            System.exit(1);
        }
    }

}
