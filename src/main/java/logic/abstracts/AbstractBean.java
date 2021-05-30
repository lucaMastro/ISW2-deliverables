package logic.abstracts;

import logic.exception.InvalidInputException;

import java.io.File;

public abstract class AbstractBean {
    protected File directory;
    protected File output;
    protected String project; //should be UPPERCASE

    protected AbstractBean(String outputFile, String dirPath, String projectName) throws InvalidInputException {
        this.directory = new File(dirPath);
        if (!this.directory.exists()) {
            throw new InvalidInputException("Directory doesn't exist.");
        }
        this.output = new File(outputFile);
        this.project = projectName.toUpperCase();
    }

    public File getDirectory() {
        return directory;
    }

    public File getOutputFile() {
        return output;
    }

    public String getProject() {
        return project;
    }
}
