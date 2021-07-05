package logic.weka;

import logic.enums.CostSensitiveClassifierType;
import logic.enums.FeaturesSelectionType;
import logic.enums.SamplingType;
import logic.exception.WalkStepFilterException;
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
    private int numRelease;
    private WalkForwardSetsManager filesManager;

    private ArrayList<Classifier> classifiers;


    public WekaManager(File input, File arff, File training, File testing) throws IOException, WalkStepFilterException {
        this.filesManager = new WalkForwardSetsManager(input, arff, training, testing);
        this.steps = new ArrayList<>();
        this.numRelease = this.filesManager.getNumOfRelease();

        int i;
        for (i = 1; i < this.numRelease; i++) {
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
                    var filtered = FilterCreator.getInstance().getUnderSaplingClassifier();
                    filtered.setClassifier(this.classifiers.get(i));
                    this.classifiers.set(i, filtered);
                }
                break;
            case OVERSAMPLING:
                // just replace the classifiers with a filtered classifier
                for (i = 0; i < this.classifiers.size(); i++) {
                    var filtered = FilterCreator.getInstance().getOverSaplingClassifier(currentStep);
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
                    var costSensitiveClassifier = FilterCreator.getInstance().sensitiveThresholdClassifier();
                    costSensitiveClassifier.setClassifier(this.classifiers.get(i));
                    this.classifiers.set(i, costSensitiveClassifier);
                }
                break;
            case SENSITIVE_LEARNING:
                for (i = 0; i < this.classifiers.size(); i++) {
                    var costSensitiveClassifier = FilterCreator.getInstance().sensitiveLearningClassifier();
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

        var output = new WekaConfigurationOutput(fs, st, csc);

        /*  feature selection is applied in walkStep: it keep a train and a test for both cases: none and features
            selection best first.
            To apply sampling, it's needed the training set: in fact, for oversampling and smoote, it needs the
             percentage that should be used in the filter.  */

        int i;
        for (i = 0; i < this.getNumOfRelease() - 1; i++) {
            var trainingDataset = this.steps.get(i).getTrainingSet(fs);
            var testingDataset = this.steps.get(i).getTestingSet(fs);

            this.applySampling(st, this.steps.get(i));
            this.applyCostSensitive(csc);

            //now i need to train the classifiers
            for (Classifier c : this.classifiers) {
                c.buildClassifier(trainingDataset);
                var currEvaluation = new Evaluation(testingDataset);
                currEvaluation.evaluateModel(c, testingDataset);

                output.appendEvaluation(currEvaluation);
            }
        }

        return output;
    }
}
