package logic.bean;

import logic.exception.UnexistingFileException;

import java.io.File;
import java.io.IOException;

public class WekaBean {

    private File input;
    private File outputCSV;
    private File arff;

    private Boolean arffIsTemp = Boolean.FALSE;

    private File testing;
    private File training;

    public WekaBean(String csvInputFile, String csvOutputFile, String arfOutputFile)
            throws UnexistingFileException, IOException {

        this.input = new File(csvInputFile);
        if (!this.input.exists())
            throw new UnexistingFileException("Input file doesn't exist");

        this.outputCSV = new File(csvOutputFile);

        /*  arfOutputFile may be null. in this case, i need to create a temporary file  */
        var ext = ".arff";
        if (arfOutputFile == null) {
            this.arff = File.createTempFile("tempArf", ext);
            this.arffIsTemp = Boolean.TRUE;
        }
        else
            this.arff = new File(arfOutputFile);

        this.testing = File.createTempFile("testing", ext);
        this.training = File.createTempFile("training", ext);
    }

    public File getInput() {
        return input;
    }

    public File getOutputCSV() {
        return outputCSV;
    }

    public File getArff() {
        return arff;
    }

    public Boolean getArffIsTemp() {
        return arffIsTemp;
    }

    public File getTesting() {
        return testing;
    }

    public File getTraining() {
        return training;
    }
}
