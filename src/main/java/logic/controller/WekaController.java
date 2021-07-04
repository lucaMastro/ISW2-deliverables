
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
import logic.enums.CostSensitiveClassifierType;
import logic.enums.FeaturesSelectionType;
import logic.enums.SamplingType;
import logic.weka.WekaConfigurationOutput;
import logic.weka.WekaManager;
import org.decimal4j.util.DoubleRounder;
import weka.classifiers.Evaluation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WekaController {

    public void run(WekaBean bean) throws Exception {

        var wekaManager = new WekaManager(bean.getInput(),
                bean.getArff(),
                bean.getTraining(),
                bean.getTesting() );

        var list = new ArrayList<WekaConfigurationOutput>();

        for (FeaturesSelectionType fs : FeaturesSelectionType.values()){
            for (CostSensitiveClassifierType csc : CostSensitiveClassifierType.values()){
                for (SamplingType st : SamplingType.values()){
                    var output = wekaManager.computeMetrics(fs, csc, st);
                    list.add(output);
                }
            }
        }
        this.writeFile(list, bean.getOutputCSV(), wekaManager.getDatasetName(), wekaManager.getNumOfRelease());
    }


    private void writeFile(List<WekaConfigurationOutput> list, File outputCSV, String datasetName, int numOfRelease) {
        double precision;
        double recall;
        double auc;
        double kappa;

        var names = new String[]{"RandomForest", "NaiveBayes", "Ibk"};
        var legendLine = "Dataset,#TrainingRelease,Classifier,Precision,Recall,AUC,Kappa\n";


        try (var fw = new FileWriter(outputCSV)) {

            for (WekaConfigurationOutput output : list) {
                var currIndexRelease = 0; //should be incremented
                var currNameIndex = 0;
                fw.append(output.getTitle());
                fw.append(legendLine);
                var bld = new StringBuilder();

                for (Evaluation e : output.getEvaluations()) {
                    precision = DoubleRounder.round(e.precision(0), 3);
                    recall = DoubleRounder.round(e.recall(0), 3);
                    auc = DoubleRounder.round(e.areaUnderROC(0), 3);
                    kappa = DoubleRounder.round(e.kappa(), 3);

                    bld.append(datasetName).append(",")
                            .append(currIndexRelease + 1).append(",")
                            .append(names[currNameIndex]).append(",")
                            .append(precision).append(",")
                            .append(recall).append(",")
                            .append(auc).append(",")
                            .append(kappa).append("\n");

                    currNameIndex = (currNameIndex + 1) % 3;
                    if (currNameIndex == 0)
                        currIndexRelease = (currIndexRelease + 1) % numOfRelease;
                }
                fw.append(bld.toString());
            }
        } catch (IOException e) {
            var logger = Logger.getLogger(WekaController.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }
}
