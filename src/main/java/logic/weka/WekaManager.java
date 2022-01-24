package logic.weka;

import logic.bean.WekaConfigurationOutputBean;
import logic.bean.WekaStepOutputBean;
import logic.enums.CostSensitiveClassifierType;
import logic.enums.FeaturesSelectionType;
import logic.enums.SamplingType;
import org.decimal4j.util.DoubleRounder;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class WekaManager {

    private ArrayList<WalkStep> steps;
    private ArrayList<Classifier> classifiers;
    private int numOfRelease;
    private Instances totalData;

    public WekaManager(File input, File arff) throws Exception {
        this.steps = new ArrayList<>();

        // reading instances
        var csvInstances = this.readInputCsv(input);
        // create the arff without replicated data
        ArffCreator.createArffWithoutDuplicated(arff, csvInstances);
        // writing totalData: we don't want replicated
        var arffLoader = new ArffLoader();
        arffLoader.setSource(arff);
        this.totalData = arffLoader.getDataSet();

        this.numOfRelease = this.totalData.numDistinctValues(0);
        int i;
        var numRelease = this.numOfRelease;
        for (i = 1; i < numRelease; i++) {
            this.steps.add(new WalkStep(this.totalData, i));
        }

        //classifiers
        this.initializeClassifiers();

    }

    private Instances readInputCsv(File inputCsv) throws IOException {
        var loader = new CSVLoader();
        loader.setSource(inputCsv);
        var csvInstances = loader.getDataSet();//get instances object
        //removing name's column
        csvInstances.deleteAttributeAt(1);
        return csvInstances;
    }

    private void initializeClassifiers(){
        this.classifiers = new ArrayList<>();
        this.classifiers.add(new RandomForest());
        this.classifiers.add(new NaiveBayes());
        this.classifiers.add(new IBk());
    }

    // *********************************************************************

    public int getNumOfRelease() {
        return this.numOfRelease;
    }

    public void applySampling(SamplingType st, WalkStep currentStep) throws Exception {
        int i;
        switch (st){
            case UNDERSAMPLING:
                // just replace the classifiers with a filtered classifier
                for (i = 0; i < this.classifiers.size(); i++) {
                    var filtered = FilterCreator.getUnderSaplingClassifier();
                    filtered.setClassifier(this.classifiers.get(i));
                    this.classifiers.set(i, filtered);
                }
                break;
            case OVERSAMPLING:
                // just replace the classifiers with a filtered classifier
                for (i = 0; i < this.classifiers.size(); i++) {
                    var filtered = FilterCreator.getOverSaplingClassifier(currentStep);
                    filtered.setClassifier(this.classifiers.get(i));
                    this.classifiers.set(i, filtered);
                }
                break;
            case SMOTE:
                for (i = 0; i < this.classifiers.size(); i++) {
                    var filtered = FilterCreator.getSMOTEClassifier();
                    filtered.setClassifier(this.classifiers.get(i));
                    this.classifiers.set(i, filtered);
                }
                break;
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
                    var costSensitiveClassifier = FilterCreator.sensitiveThresholdClassifier();
                    costSensitiveClassifier.setClassifier(this.classifiers.get(i));
                    this.classifiers.set(i, costSensitiveClassifier);
                }
                break;
            case SENSITIVE_LEARNING:
                for (i = 0; i < this.classifiers.size(); i++) {
                    var costSensitiveClassifier = FilterCreator.sensitiveLearningClassifier();
                    costSensitiveClassifier.setClassifier(this.classifiers.get(i));
                    this.classifiers.set(i, costSensitiveClassifier);
                }
                break;
            default:
                break;
        }
    }


    public WekaConfigurationOutputBean computeMetrics(FeaturesSelectionType fs, CostSensitiveClassifierType csc,
                                                      SamplingType st) throws Exception {

        var totalDataLen = this.getTotalDataLen();
        var output = new WekaConfigurationOutputBean(fs, st, csc);

        /*  feature selection is applied in walkStep: it keeps a train and a test for both cases: none and features
            selection best first.
            To apply sampling, it's needed the training set: in fact, for oversampling and smoote, it needs the
            percentage that shls ould be used in the filter.  */

        int i;
        int j = 0;
        Instances trainingDataset = null;
        // steps cycle
        for (i = 0; i < this.numOfRelease - 1; i++) {
            var stepOutput = new WekaStepOutputBean(this.classifiers.size());

            var step = this.steps.get(i);
            trainingDataset = step.getTrainingSet(fs);
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

            //re-initialize cassifiers
            this.initializeClassifiers();
            this.applySampling(st, this.steps.get(i));
            this.applyCostSensitive(csc);

            //now i need to train the classifier
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
        return this.totalData.numInstances();
    }
}
