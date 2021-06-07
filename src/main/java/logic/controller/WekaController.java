
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

package logic.controller;

import weka.core.Instances;


import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.converters.ConverterUtils.DataSource;


public class WekaController {
    public static void main(String args[]) throws Exception{
        //load datasets
        DataSource source1 = new DataSource("/home/luca/Scrivania/training.arff");
        Instances training = source1.getDataSet();
        DataSource source2 = new DataSource("/home/luca/Scrivania/testing.arff");
        Instances testing = source2.getDataSet();

        int numAttr = training.numAttributes();
        training.setClassIndex(numAttr - 1);
        testing.setClassIndex(numAttr - 1);

        NaiveBayes classifier = new NaiveBayes();

        classifier.buildClassifier(training);

        Evaluation eval = new Evaluation(testing);

        eval.evaluateModel(classifier, testing);

        System.out.println("precision = "+eval.precision(0));
        System.out.println("recall = "+eval.recall(0));
        System.out.println("AUC = "+eval.areaUnderROC(0));
        System.out.println("kappa = "+eval.kappa());

    }
}
