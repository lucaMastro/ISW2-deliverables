/*
 *  How to use WEKA API in Java
 *  Copyright (C) 2014
 *  @author Dr Noureddin M. Sadawi (noureddin.sadawi@gmail.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it as you wish ...
 *  I ask you only, as a professional courtesy, to cite my name, web page
 *  and my YouTube Channel!
 *
 */

package logic.weka;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WalkForwardSetsManager {

    private File basicArff;
    private int numOfRelease;
    private Instances data;

    private ArrayList<Attribute> attributes;
    private ArrayList<Instance> trainingInstances;
    private ArrayList<Instance> testingInstances;
    private File testingSet;
    private File trainingSet;

    /* *******************************************************************************************************/

    public WalkForwardSetsManager(File csvFile, File arffFile, File training, File testing) throws IOException {
    //public WalkForwardSetsManager(String csvPath, String outputArff, String training, String testing) throws IOException {
        var loader = new CSVLoader();
        loader.setSource(csvFile);
        this.data = loader.getDataSet();//get instances object
        //removing name's column
        this.data.deleteAttributeAt(1);

        this.convert(arffFile);
        this.computeNumOfReleases(loader);

        this.testingSet = testing;
        this.trainingSet = training;
        this.initializeAttributes();
        this.trainingInstances = new ArrayList<>();
    }


    /* convert a csv to a arff standard file    */
    private void convert(File arffOutputFile) throws IOException {

        var saver = new ArffSaver();
        saver.setInstances(this.data);//set the dataset we want to convert

        //save as arff
        this.basicArff = arffOutputFile;
        saver.setFile(this.basicArff);

        saver.writeBatch();

        if (this.data.attribute(10).value(0).equals("No")){
            //have to change this line
            this.changeLine();
        }

    }

    private void changeLine(){
        var buffer = new StringBuilder();
        var fileContent = "";
        var line ="";
        var oldLine = "@attribute Buggy {No,Yes}";
        var newLine = "@attribute Buggy {Yes,No}";

        try(var sc = new Scanner(this.basicArff)){
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
            var logger = Logger.getLogger(WalkForwardSetsManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }

        try(var fw = new FileWriter(this.basicArff.getPath())) {
            fw.append(fileContent);
            fw.flush();
        } catch (IOException e) {
            var logger = Logger.getLogger(WalkForwardSetsManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

    /* *******************************************************************************************************/

    private void computeNumOfReleases(CSVLoader loader) throws IOException {

        //need a replication of data
        var copy = new Instances(this.data);//get instances object

        var numAttrib = copy.numAttributes();
        int i;
        for (i = 1; i < numAttrib; i++)
            copy.deleteAttributeAt(1);

        //removing name's column
        var num = 1;
        var previous = copy.instance(0);
        for (i = 0; i < copy.numInstances(); i++){
            if (copy.instance(i).value(0) != previous.value(0)){
                previous = copy.instance(i);
                num++;
            }
        }
        this.numOfRelease = num;
    }

    private void initializeAttributes(){
        this.attributes = new ArrayList<>();
        int i;
        for (i = 0; i < this.data.numAttributes(); i++)
            this.attributes.add(this.data.attribute(i));
    }

    private void computeTestingSet(int indexRelease){
        int i;
        int currIndex;

        // re-initialize the list
        this.testingInstances = new ArrayList<>();
        for (i = 0; i < this.data.numInstances(); i++){
            currIndex = (int) this.data.instance(i).value(0);
            if (currIndex == indexRelease)
                testingInstances.add(this.data.instance(i));
            else if (currIndex > indexRelease)
                break;
        }
    }


    private void writeHeader(PrintWriter pf) {
        //just write the attributes in the given file. f is either this.testingSet or this.trainingSet

        pf.append("@relation " + this.data.relationName() + "\n\n");
        pf.flush();
        for (Attribute line : this.attributes){
            pf.append(line.toString() + "\n");
            pf.flush();
        }
        pf.append("\n@data\n");
        pf.flush();
    }

    public void computeTestingFile(int indexRelease){
        this.computeTestingSet(indexRelease);
        try(var fp = new PrintWriter(this.testingSet)) {

            this.writeHeader(fp);
            for (Instance line : this.testingInstances){
                fp.append(line.toString() + "\n");
                fp.flush();
            }
        } catch (IOException e) {
            var logger = Logger.getLogger(WalkForwardSetsManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

    public void computeTrainingFile(int indexRelease){
        /* this code is optimized for increment creation. on an iterative loop:
        *   - train = 1 -> test = 2
        *   - train = 1,2 -> test = 3
        *   */
        assert indexRelease > 0;

        int i;

        try(var fw = new FileWriter(this.trainingSet, true);
            var fp = new PrintWriter(fw)) {
            if (indexRelease == 1) {
                fp.print("");
                fp.flush();
                this.writeHeader(fp);
                for (i = 0; i < this.data.numInstances(); i++) {
                    if ( (int) this.data.instance(i).value(0) > indexRelease)
                        break;
                    fp.append(this.data.instance(i).toString() + "\n");
                    fp.flush();
                }
            }
            else {
                //in this case just append the testing instances:
                for (Instance obj : this.testingInstances){
                    fp.append(obj.toString() + "\n");
                    fp.flush();
                }
            }
        } catch (IOException e) {
            var logger = Logger.getLogger(WalkForwardSetsManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

    public int getNumOfRelease() {
        return numOfRelease;
    }

    public int getNumberOfAttributes() {
        return this.data.numAttributes();
    }

    public String getDatasetName() {
        return this.data.relationName();
    }

    /*
    public static void main(String[] args) throws IOException {

        var d = new WalkForwardSetsManager("/home/luca/Scrivania/bookkeeperCSV.csv",
                "/home/luca/Scrivania/bookkeeperCSV.arff",
                "/home/luca/Scrivania/training.arff",
                "/home/luca/Scrivania/testing.arff");

        for (var i = 0; i < d.numOfRelease - 1; i++){
            d.computeTrainingFile(i + 1);
            d.computeTestingFile(i + 2);
            System.out.println("insert to continue");
            System.in.read();
        }
    }*/
}