package org.perez.workflow.scheduler;

import com.google.gson.Gson;
import org.junit.Test;
import org.perez.workflow.elements.Workflow;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test para generar archivos .dot de GraphViz
 */
public class TestGenerateDot
{
    @Test
    public void testGenerateDot()
    {
        generateDotFiles("results-existing", "results-existing-dot");
    }

    private void generateDotFiles(String inputFolderPath, String outputFolderPath)
    {
        WorkflowJSONFileVisitor finder = new WorkflowJSONFileVisitor();

        try {
            Files.walkFileTree(Paths.get(inputFolderPath), finder);
        } catch (IOException ex) {
            System.err.println("No pude recorrer el folder de entrada: " + ex.getMessage());
            System.exit(1);
        }

        Gson gson = Utils.getGSON();
        for(Path fp: finder.getValidPaths()) {
            try {
                String basename = fp.getFileName().toString().replaceFirst("[.][^.]+$", "");

                System.out.print("Procesando " + basename + "... ");

                Workflow w = gson.fromJson(new FileReader(fp.toAbsolutePath().toString()), Workflow.class);

                String dotOutputPath = outputFolderPath + "/" + basename + ".dot";
                String dotContent = w.toGraphviz(basename);
                Utils.writeFile(dotOutputPath, dotContent);

                System.out.println("OK");
            } catch (FileNotFoundException e) {
                System.err.println("Error al abrir achivo de de flujo de trabajo: " + e.getMessage());
            }
        }

    }
}
