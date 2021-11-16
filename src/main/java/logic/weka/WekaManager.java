package logic.weka;

import logic.enums.CostSensitiveClassifierType;
import logic.enums.FeaturesSelectionType;
import logic.enums.SamplingType;
import logic.exception.WalkStepFilterException;
import org.decimal4j.util.DoubleRounder;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class WekaManager {

    private ArrayList<WalkStep> steps;
    private WalkForwardFilesManager filesManager;

    private ArrayList<Classifier> classifiers;
    private FilterCreator filterCreator;


    public WekaManager(File input, File arff, File training, File testing) throws IOException, WalkStepFilterException {
        this.filesManager = new WalkForwardFilesManager(input, arff, training, testing);
        this.steps = new ArrayList<>();

        this.filterCreator = new FilterCreator();

        int i;
        var numRelease = this.filesManager.getNumOfRelease();
        for (i = 1; i < numRelease; i++) {
            this.filesManager.computeFiles(i);
            this.steps.add(new WalkStep(training, testing, this.filesManager.getNumberOfAttributes()));
        }

        //classifiers
        this.initializeClassifiers();

    }

    private void initializeClassifiers(){
        this.classifiers = new ArrayList<>();
        this.classifiers.add(new RandomForest());
        this.classifiers.add(new NaiveBayes());
        this.classifiers.add(new IBk());
    }

    public int getNumOfRelease() {
        return this.filesManager.getNumOfRelease();
    }

    public String getDatasetName() {
        return this.filesManager.getDatasetName();
    }

    public void applySampling(SamplingType st, WalkStep currentStep) throws Exception {
        int i;
        //re-initialize cassifiers
        this.initializeClassifiers();
        switch (st){
            case UNDERSAMPLING:
                // just replace the classifiers with a filtered classifier
                for (i = 0; i < this.classifiers.size(); i++) {
                    var filtered = this.filterCreator.getUnderSaplingClassifier();
                    filtered.setClassifier(this.classifiers.get(i));
                    this.classifiers.set(i, filtered);
                }
                break;
            case OVERSAMPLING:
                // just replace the classifiers with a filtered classifier
                for (i = 0; i < this.classifiers.size(); i++) {
                    var filtered = this.filterCreator.getOverSaplingClassifier(currentStep);
                    filtered.setClassifier(this.classifiers.get(i));
                    this.classifiers.set(i, filtered);
                }
                break;
          /*  case SMOTE:
                break;*/
            default:
                break;
        }
    }

    public void applyCostSensitive(CostSensitiveClassifierType csc){
        /*  this method is invoked after applySampling method. This means that this.classifier may contain
        *   FilteredClassifier, sampled. Just setting them as classifier for a CostSensitiveClassifier */

        int i;
        switch (csc){
            case SENSITIVE_THRESHOLD:
                for (i = 0; i < this.classifiers.size(); i++) {
                    var costSensitiveClassifier = this.filterCreator.sensitiveThresholdClassifier();
                    costSensitiveClassifier.setClassifier(this.classifiers.get(i));
                    this.classifiers.set(i, costSensitiveClassifier);
                }
                break;
            case SENSITIVE_LEARNING:
                for (i = 0; i < this.classifiers.size(); i++) {
                    var costSensitiveClassifier = this.filterCreator.sensitiveLearningClassifier();
                    costSensitiveClassifier.setClassifier(this.classifiers.get(i));
                    this.classifiers.set(i, costSensitiveClassifier);
                }
                break;
            default:
                break;
        }
    }


    public WekaConfigurationOutput computeMetrics(FeaturesSelectionType fs, CostSensitiveClassifierType csc,
                                            SamplingType st) throws Exception {

        var totalDataLen = this.getTotalDataLen();
        var output = new WekaConfigurationOutput(fs, st, csc);

        /*  feature selection is applied in walkStep: it keeps a train and a test for both cases: none and features
            selection best first.
            To apply sampling, it's needed the training set: in fact, for oversampling and smoote, it needs the
             percentage that should be used in the filter.  */

        int i;
        for (i = 0; i < this.getNumOfRelease() - 1; i++) {

            var stepOutput = new WekaStepOutput(this.classifiers.size());

            var step = this.steps.get(i);
            var trainingDataset = step.getTrainingSet(fs);
            var testingDataset = step.getTestingSet(fs);

            var instancesInTraining = trainingDataset.numInstances();
            var percentage = (double) instancesInTraining / totalDataLen;
            stepOutput.setTrainingPercentage(DoubleRounder.round(percentage, 3));

            var trainDefectNum = step.getPositivesTraining();
            percentage = (double) trainDefectNum / (trainDefectNum + step.getNegativesTraining());
            stepOutput.setDefectiveInTrainingPercentage(DoubleRounder.round(percentage, 3));

            var testDefectNum = step.getPositivesTesting();
            percentage = (double) testDefectNum / (testDefectNum + step.getNegativesTesting());
            stepOutput.setDefectiveInTestingPercentage(DoubleRounder.round(percentage, 3));

            this.applySampling(st, this.steps.get(i));
            this.applyCostSensitive(csc);

            //now i need to train the classifier
            int j;
            for (j = 0; j < this.classifiers.size(); j++) {
                var c = this.classifiers.get(j);
                c.buildClassifier(trainingDataset);
                var currEvaluation = new Evaluation(testingDataset);
                currEvaluation.evaluateModel(c, testingDataset);

                stepOutput.setEvaluation(currEvaluation, j);
            }
            output.appendStepEvaluation(stepOutput);
        }
        return output;
    }

    private int getTotalDataLen(){
        return this.filesManager.getTotalDataLen();
    }
}
