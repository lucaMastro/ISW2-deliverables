
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

import logic.bean.WekaBean;
import logic.weka.WalkForwardSetsManager;
import org.decimal4j.util.DoubleRounder;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.converters.ArffLoader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WekaController {

    private List<Evaluation> computeMetrics(WekaBean bean, int numAttr) throws Exception {
        int i;
        Classifier classifier;
        Evaluation evaluation;

        /*  sets settings    */
        var trainingFile = bean.getTraining();
        var testingFile = bean.getTesting();

        //load datasets
        var loader = new ArffLoader();

        loader.setSource(trainingFile);
        var trainingDataset = loader.getDataSet();

        loader.setSource(testingFile);
        var testingDataset = loader.getDataSet();

        trainingDataset.setClassIndex(numAttr - 1);
        testingDataset.setClassIndex(numAttr - 1);


        /*  classifier creation */
        var classifiers = new ArrayList<Classifier>();
        classifiers.add(new RandomForest());
        classifiers.add(new NaiveBayes());
        classifiers.add(new IBk());
        for (Classifier c : classifiers)
            c.buildClassifier(trainingDataset);

        /*  evaluation creation */
        var evaluations = new ArrayList<Evaluation>();
        for (i = 0; i < classifiers.size(); i++)
            evaluations.add(new Evaluation(testingDataset));

        for (i = 0; i < classifiers.size(); i++){
            classifier = classifiers.get(i);
            evaluation = evaluations.get(i);
            evaluation.evaluateModel(classifier, testingDataset);
        }
        return evaluations;
    }

    public void run(WekaBean bean) throws Exception {

        var filesManager = new WalkForwardSetsManager(bean.getInput(),
                bean.getArff(),
                bean.getTraining(),
                bean.getTesting() );

        var list = new ArrayList<List<Evaluation>>();
        var numRelease = filesManager.getNumOfRelease();
        int i;
        for (i = 1; i < numRelease; i++){
            filesManager.computeTrainingFile(i);
            filesManager.computeTestingFile(i + 1);

            var currEvaluation = this.computeMetrics(bean, filesManager.getNumberOfAttributes());

            list.add(currEvaluation);
        }
        this.writeFile(list, bean.getOutputCSV(), filesManager.getDatasetName());

    }


    private void writeFile(ArrayList<List<Evaluation>> list, File outputCSV, String dataset) {
        int i;
        double precision;
        double recall;
        double auc;
        double kappa;

        var names = new String[]{"RandomForest", "NaiveBayes", "Ibk"};
        var currNameIndex = 0;
        try(var fw = new FileWriter(outputCSV)){
            fw.append("Dataset,#TrainingRelease,Classifier,Precision,Recall,AUC,Kappa\n");
            var bld = new StringBuilder();
            for (i = 0; i < list.size(); i++){
                var l = list.get(i);
                for (Evaluation e : l){
                    precision = DoubleRounder.round(e.precision(0), 3);
                    recall = DoubleRounder.round(e.recall(0), 3);
                    auc = DoubleRounder.round(e.areaUnderROC(0), 3);
                    kappa = DoubleRounder.round(e.kappa(), 3);

                    bld.append(dataset).append(",")
                    .append(i + 1).append(",")
                    .append(names[currNameIndex]).append(",")
                    .append(precision).append(",")
                    .append(recall).append(",")
                    .append(auc).append(",")
                    .append(kappa).append(",")
                    .append("\n");

                    currNameIndex = (currNameIndex + 1) % 3;
                }
            }
            fw.append(bld.toString());
        } catch (IOException e) {
            var logger = Logger.getLogger(WekaController.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }
}
