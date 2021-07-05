package logic.weka;/*
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

//import required classes
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;


import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.supervised.instance.SMOTE;


public class TestWekaSampling{
	public static void main(String args[]) throws Exception{
		//load datasets
		DataSource source1 = new DataSource("/home/luca/Scrivania/train.arff");
		Instances training = source1.getDataSet();
		DataSource source2 = new DataSource("/home/luca/Scrivania/test.arff");
		Instances testing = source2.getDataSet();

		int numAttr = training.numAttributes();
		training.setClassIndex(numAttr - 1);
		testing.setClassIndex(numAttr - 1);

		var naiveBayes0 = new NaiveBayes();
		naiveBayes0.buildClassifier(training);
		Evaluation eval = new Evaluation(testing);
		eval.evaluateModel(naiveBayes0, testing); //not sampled



		/* ************************************************************* */
		Resample resample = new Resample();
		resample.setInputFormat(training);
		resample.setOptions(new String[]{"-B", "1", "-Z", "158.1"});

		//SMOTE smote = new SMOTE();
		//smote.setInputFormat(training);
		//smote.setOptions(new String[]{"-P", "158"} );

		FilteredClassifier fc = new FilteredClassifier();
		var naiveBayes = new NaiveBayes();
		fc.setClassifier(naiveBayes);

		fc.setFilter(resample);
		//fc.setFilter(smote);


		fc.buildClassifier(training);
		Evaluation eval2 = new Evaluation(testing);
		eval2.evaluateModel(fc, testing); //sampled

		System.out.println("Correct% nonsampled = "+eval.pctCorrect());
		System.out.println("Correct% sampled= "+eval2.pctCorrect()+ "\n");

		System.out.println("Precision nonsampled= "+eval.precision(0));
		System.out.println("Precision sampled= "+eval2.precision(0)+ "\n");

		System.out.println("Recall nonsampled= "+eval.recall(0));
		System.out.println("Recall sampled= "+eval2.recall(0)+ "\n");

	}
}
