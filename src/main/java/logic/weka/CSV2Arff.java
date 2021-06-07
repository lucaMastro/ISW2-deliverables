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

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CSV2Arff {

    public void convert() throws Exception {

        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("/home/luca/Scrivania/testing.csv"));
        Instances data = loader.getDataSet();//get instances object

        //removing name's column
        data.deleteAttributeAt(1);

        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);//set the dataset we want to convert

        //and save as ARFF
        saver.setFile(new File("/home/luca/Scrivania/testing.arff"));

        saver.writeBatch();

        if (data.attribute(10).value(0).equals("No")){
            //have to change this line
            this.changeLine("/home/luca/Scrivania/testing.arff");
        }

    }

    private void changeLine(String path){
        var f = new File(path);
        var buffer = new StringBuilder();
        var fileContent = "";
        var line ="";
        var oldLine = "@attribute Buggy {No,Yes}";
        var newLine = "@attribute Buggy {Yes,No}";

        try(Scanner sc = new Scanner(f)){
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
            e.printStackTrace();
        }

        try(FileWriter fw = new FileWriter(path)) {
            fw.append(fileContent);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        var a = new CSV2Arff();
        a.convert();
    }
}
