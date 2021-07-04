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
import weka.classifiers.lazy.IBk;
import weka.core.Instances;


import java.util.Random;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.evaluation.*;
//import weka.filters.supervised.instance.SMOTE;

public class TestWekaSampling{

	public int countPositive(Instances train){
		var count = 0;
		var classIndex = train.numAttributes() - 1;
		for (int i = 0; i < train.numInstances();i++){
			if (train.instance(i).toString(classIndex).equals("Yes"))
				count++;
		}
		return count;
	}

	public int countNegative(Instances train){
		return train.numInstances() - countPositive(train);
	}


	public static void main(String args[]) throws Exception{
		//load datasets
		DataSource source1 = new DataSource("/home/luca/Scrivania/train.arff");
		Instances training = source1.getDataSet();
		DataSource source2 = new DataSource("/home/luca/Scrivania/test.arff");
		Instances testing = source2.getDataSet();

		int numAttr = training.numAttributes();

		var d = new TestWekaSampling();
		var neg = d.countNegative(training);
		var pos = d.countPositive(training);
		System.out.println("neg = " + neg);
		System.out.println("pos = " + pos + "\n" );

		training.setClassIndex(numAttr - 1);
		testing.setClassIndex(numAttr - 1);

		var naiveBayes = new NaiveBayes();
		naiveBayes.buildClassifier(training);
		Evaluation eval = new Evaluation(testing);
		eval.evaluateModel(naiveBayes, testing); //not sampled



		//undersampling
		Resample resample = new Resample();
		resample.setInputFormat(training);
		FilteredClassifier fc = new FilteredClassifier();

		var naiveBayesSampeld = new NaiveBayes();
		fc.setClassifier(naiveBayesSampeld);

		/*fc.setFilter(resample);
		eventual parameters setting omitted
		*/

	   /* SMOTE smote = new SMOTE();
		smote.setInputFormat(training);
		fc.setFilter(smote);
		*/

		SpreadSubsample spreadSubsample = new SpreadSubsample();
		//String[] opts = new String[]{ "-M", "1.0"};
		//spreadSubsample.setOptions(opts);

		fc.setFilter(spreadSubsample);

		fc.buildClassifier(training);
		Evaluation eval2 = new Evaluation(testing);
		eval2.evaluateModel(fc, testing); //sampled


		fc.setClassifier(new IBk());
		//fc.setFilter(spreadSubsample);
		fc.buildClassifier(training);
		var eval3 = new Evaluation(testing);
		eval3.evaluateModel(fc, testing);



		neg = d.countNegative(training);
		pos = d.countPositive(training);
		System.out.println("neg = " + neg);
		System.out.println("pos = " + pos + "\n" );


		System.out.println("Correct% nonsampled = "+eval.pctCorrect());
		System.out.println("Correct% sampled= "+eval2.pctCorrect());
		System.out.println("Correct% sampled= "+eval3.pctCorrect()+ "\n");

		System.out.println("Precision nonsampled= "+eval.precision(0));
		System.out.println("Precision sampled= "+eval2.precision(0));
		System.out.println("Precision sampled= "+eval3.precision(0)+ "\n");

		System.out.println("Recall nonsampled= "+eval.recall(0));
		System.out.println("Recall sampled= "+eval2.recall(0));
		System.out.println("Recall sampled= "+eval3.recall(0)+ "\n");

		System.out.println("ROC nonsampled= "+eval.areaUnderROC(0));
		System.out.println("ROC sampled= "+eval2.areaUnderROC(0));
		System.out.println("ROC sampled= "+eval3.areaUnderROC(0)+ "\n");

		System.out.println("kappa nonsampled= "+eval.kappa());
		System.out.println("kappa sampled= "+eval2.kappa());
		System.out.println("kappa sampled= "+eval3.kappa()+ "\n");

	}
}
