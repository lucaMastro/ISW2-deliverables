package logic.weka;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.unsupervised.instance.RemoveDuplicates;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArffCreator {

    private static ArffCreator instance = null;

    public static ArffCreator getInstance(){
        if (ArffCreator.instance == null)
            ArffCreator.instance = new ArffCreator();
        return ArffCreator.instance;
    }

    public void createArff(File arffOutputFile, Instances instances) throws IOException {

        var saver = new ArffSaver();
        saver.setInstances(instances);//set the dataset we want to convert

        // create temp file
        var currDir = Paths.get(".").toAbsolutePath().normalize().toFile();
        var ext = ".arff";
        var tmpFile = File.createTempFile("tmpArff", ext, currDir);
        // write instances on tmpFile
        saver.setFile(tmpFile);
        saver.writeBatch();
        //this is the case when the header of "Buggy" attribute starts with "No" instead of "Yes"
        if (instances.attribute(instances.numAttributes() - 1).value(0).equals("No")){
            this.changeLine(tmpFile);
        }
        // deleting duplicates
        var opt = new String[]{"-i", tmpFile.toPath().toString(), "-o", arffOutputFile.toPath().toString(), "-c", "last"};
        RemoveDuplicates.main(opt);
        // removing temp file
        Files.delete(tmpFile.toPath());
    }



    private void changeLine(File arffOutputFile){
        var buffer = new StringBuilder();
        var fileContent = "";
        var line ="";
        var oldLine = "@attribute Buggy {No,Yes}";
        var newLine = "@attribute Buggy {Yes,No}";

        try(var sc = new Scanner(arffOutputFile)){
            //instantiating the StringBuffer class

            //Reading lines of the file and appending them to StringBuffer
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if (line.equals(oldLine))
                    buffer.append(newLine);
                else
                    buffer.append(line);

                buffer.append(System.lineSeparator());
            }

            fileContent = buffer.toString();

        } catch (IOException e) {
            var logger = Logger.getLogger(WekaManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }

        try(var fw = new FileWriter(arffOutputFile.getPath())) {
            fw.append(fileContent);
            fw.flush();
        } catch (IOException e) {
            var logger = Logger.getLogger(WekaManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

}
