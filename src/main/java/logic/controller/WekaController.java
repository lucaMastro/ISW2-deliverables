package logic.controller;

import logic.bean.WekaBean;
import logic.enums.CostSensitiveClassifierType;
import logic.enums.FeaturesSelectionType;
import logic.enums.SamplingType;
import logic.bean.WekaConfigurationOutputBean;
import logic.weka.WekaManager;
import logic.bean.WekaStepOutputBean;
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
        var wekaManager = new WekaManager(bean.getInputCSV(),
                bean.getArff());
        var list = new ArrayList<WekaConfigurationOutputBean>();

        for (FeaturesSelectionType fs : FeaturesSelectionType.values()){
            for (CostSensitiveClassifierType csc : CostSensitiveClassifierType.values()){
                for (SamplingType st : SamplingType.values()){
                    var output = wekaManager.computeMetrics(fs, csc, st);
                    list.add(output);
                }
            }
        }
        var name = bean.getInputCSV().getName().split("\\.")[0] + "_without_duplicated_lines";
        this.writeFile(list, bean.getOutputCSV(), name, wekaManager.getNumOfRelease());
    }


    private void writeFile(List<WekaConfigurationOutputBean> list, File outputCSV, String datasetName, int numOfRelease) {
        double precision;
        double recall;
        double auc;
        double kappa;
        double tp;
        double fp;
        double tn;
        double fn;

        var names = new String[]{"RandomForest", "NaiveBayes", "Ibk"};
        var legendLine = new StringBuilder("Dataset,");
        legendLine.append("#TrainingRelease,")
                .append("%Training (data on training / total data),")
                .append("%Defective in training,")
                .append("%Defective in testing,")
                .append("Classifier,")
                .append("Balancing,")
                .append("Feature Selection,")
                .append("Sensitivity,")
                .append("TP,")
                .append("FP,")
                .append("TN,")
                .append("FN,")
                .append("Precision,")
                .append("Recall,")
                .append("AUC,")
                .append("Kappa\n").toString();

        try (var fw = new FileWriter(outputCSV)) {
            fw.append(legendLine);
            for (WekaConfigurationOutputBean output : list) {
                var currIndexRelease = 1; //should be incremented: it's the number of releases in training

                for (WekaStepOutputBean wekaStepOutput : output.getStepEvaluations()) {
                    var currNameIndex = 0; // to parse classifier name
                    for (Evaluation e : wekaStepOutput.getEvaluationsArray()) {
                        var bld = new StringBuilder();
                        precision = DoubleRounder.round(e.precision(0), 3);
                        recall = DoubleRounder.round(e.recall(0), 3);
                        auc = DoubleRounder.round(e.areaUnderROC(0), 3);
                        kappa = DoubleRounder.round(e.kappa(), 3);

                        tp = DoubleRounder.round(e.numTruePositives(0), 3);
                        fp = DoubleRounder.round(e.numFalsePositives(0), 3);
                        tn = DoubleRounder.round(e.numTrueNegatives(0), 3);
                        fn = DoubleRounder.round(e.numFalseNegatives(0), 3);

                        bld.append(datasetName).append(",") //Dataset
                                .append(currIndexRelease).append(",") // # TrainingRelease
                                .append(wekaStepOutput.getTrainingPercentage()).append(",") //train %
                                .append(wekaStepOutput.getDefectiveInTrainingPercentage()).append(",") // defects in train
                                .append(wekaStepOutput.getDefectiveInTestingPercentage()).append(",") // defects in test
                                .append(names[currNameIndex]).append(",") // Classifier
                                .append(output.getSamplingType()).append(",") // Sampling
                                .append(output.getFeaturesSelection()).append(",") // Features
                                .append(output.getCostSensitiveClassifier()).append(",") // Cost sens. class
                                .append(tp).append(",") // TP
                                .append(fp).append(",") // FP
                                .append(tn).append(",") // TN
                                .append(fn).append(",") // FN
                                .append(precision).append(",") //Precision
                                .append(recall).append(",") //Recall
                                .append(auc).append(",") // AUC
                                .append(kappa).append("\n"); // Kappa

                        currNameIndex++;
                        fw.append(bld.toString());
                    }
                    currIndexRelease = (currIndexRelease + 1) % numOfRelease;
                }
            }
        } catch (IOException e) {
            var logger = Logger.getLogger(WekaController.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }
}
