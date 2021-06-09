package logic.bean;

import logic.exception.UnexistingFileException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.Paths.get;

public class WekaBean {

    private File input;
    private File outputCSV;
    private File arff;

    private Boolean arffIsTemp = Boolean.FALSE;

    private File testing;
    private File training;

    public WekaBean(String csvInputFile, String csvOutputFile, String arfOutputFile)
            throws UnexistingFileException, IOException {

        var currDir = Paths.get(".").toAbsolutePath().normalize().toFile();
        this.input = new File(csvInputFile);
        if (!this.input.exists())
            throw new UnexistingFileException("Input file doesn't exist");

        this.outputCSV = new File(csvOutputFile);

        /*  arfOutputFile may be null. in this case, i need to create a temporary file  */
        var ext = ".arff";
        if (arfOutputFile == null) {
            this.arff = File.createTempFile("", ext, currDir);
            this.arffIsTemp = Boolean.TRUE;
        }
        else
            this.arff = new File(arfOutputFile);

        this.testing = File.createTempFile("test", ext, currDir);
        this.training = File.createTempFile("train", ext, currDir);
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

    public File getTesting() {
        return testing;
    }

    public File getTraining() {
        return training;
    }

    public void removeTempFiles() throws IOException {
        Files.delete(this.training.toPath());
        Files.delete(this.testing.toPath());
        if (this.arffIsTemp.equals(Boolean.TRUE))
            Files.delete(this.arff.toPath());
    }
}
