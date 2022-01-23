package logic.weka;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.SwapValues;
import weka.filters.unsupervised.instance.RemoveDuplicates;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ArffCreator {

    private ArffCreator(){}

    public static void createArff(File arffOutputFile, Instances instances) throws Exception {

        Attribute a = instances.attribute(instances.numAttributes() - 1);
        var first = a.value(0);
        //this is the case when the header of "Buggy" attribute starts with "No" instead of "Yes"
        if (first.equals("No")){
            var f = new SwapValues();
            f.setInputFormat(instances);
            instances = Filter.useFilter(instances, f);
        }
        var saver = new ArffSaver();
        saver.setInstances(instances);//set the dataset we want to convert

        // create temp file
        var currDir = Paths.get(".").toAbsolutePath().normalize().toFile();
        var ext = ".arff";
        var tmpFile = File.createTempFile("tmpArff", ext, currDir);
        // write instances on tmpFile
        saver.setFile(tmpFile);
        saver.writeBatch();
        // deleting duplicates
        var opt = new String[]{"-i", tmpFile.toPath().toString(), "-o", arffOutputFile.toPath().toString(), "-c", "last"};
        RemoveDuplicates.main(opt);
        // removing temp file
        Files.delete(tmpFile.toPath());
    }
}
