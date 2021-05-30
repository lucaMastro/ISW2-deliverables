package logic.abstracts;

import logic.exception.InvalidRangeException;
import logic.exception.NotAvaiableAlgorithm;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public abstract class AbstractBoundary {
    protected String outputFile;
    protected String dirPath;
    protected String projectName;

    public abstract void runUseCase() throws GitAPIException, InvalidRangeException, IOException, NotAvaiableAlgorithm;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
