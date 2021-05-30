package logic.bean;

import logic.exception.InvalidInputException;

import java.io.File;

public class ProportionBean {

    File directory;
    File output;
    String project; //should be UPPERCASE

    public ProportionBean(String outputFile, String dirPath, String projectName) throws InvalidInputException {
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
