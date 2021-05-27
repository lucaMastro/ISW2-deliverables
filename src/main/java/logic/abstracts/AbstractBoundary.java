package logic.abstracts;

import logic.exception.InvalidRangeException;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public abstract class AbstractBoundary {
    protected String outputFile;
    protected String dirPath;
    protected String projectName;

    public abstract void runUseCase() throws GitAPIException, InvalidRangeException, IOException;

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getPath() {
        return dirPath;
    }

    public void setPath(String path) {
        this.dirPath = path;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
