package org.perez.workflow.scheduler;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * Clase visitadora adecuadda a Java NIO
 * para obtener archivos con extension .json
 */
class WorkflowJSONFileVisitor
        extends SimpleFileVisitor<Path>
{
    final PathMatcher matcher;
    int numMatches;
    Set<Path> validPaths;

    WorkflowJSONFileVisitor()
    {
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:*.obj");
        this.numMatches = 0;
        this.validPaths = new HashSet<>();
    }

    public Set<Path> getValidPaths()
    {
        return validPaths;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException
    {
        //System.out.println(file);
        if (matcher.matches(file.getFileName())) {
            String fp = file.toString();
            System.out.println(fp);
            this.validPaths.add(file);
        }
        return FileVisitResult.CONTINUE;
    }
}
