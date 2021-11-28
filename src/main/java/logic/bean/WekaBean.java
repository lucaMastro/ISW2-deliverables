package logic.bean;

import logic.exception.UnexistingFileException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WekaBean {

    private File inputCSV;
    private File outputCSV;
    private File arff;

    private Boolean arffIsTemp = Boolean.FALSE;

    public WekaBean(String csvInputFile, String csvOutputFile, String arfOutputFile)
            throws UnexistingFileException, IOException {

        var currDir = Paths.get(".").toAbsolutePath().normalize().toFile();
        this.inputCSV = new File(csvInputFile);
        if (!this.inputCSV.exists())
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

    }

    public File getInputCSV() {
        return inputCSV;
    }

    public File getOutputCSV() {
        return outputCSV;
    }

    public File getArff() {
        return arff;
    }

    public void removeTempFiles() throws IOException {
        if (this.arffIsTemp.equals(Boolean.TRUE))
            Files.delete(this.arff.toPath());
    }
}
