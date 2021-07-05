package logic.weka;

import logic.enums.CostSensitiveClassifierType;
import logic.enums.FeaturesSelectionType;
import logic.enums.SamplingType;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;

import java.io.File;
import java.util.ArrayList;

public class WekaManager {

    private ArrayList<WalkStep> steps;
    private int numRelease;
    private WalkForwardSetsManager filesManager;

    private ArrayList<Classifier> classifiers;


    public WekaManager(File input, File arff, File training, File testing) throws Exception {
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
                    var newInstance = this.classifiers.get(i).getClass().getDeclaredConstructor().newInstance();
                    var filtered = this.getUnderSaplingClassifier();
                    filtered.setClassifier(newInstance);
                    this.classifiers.set(i, filtered);
                }
                break;
            case OVERSAMPLING:
                // just replace the classifiers with a filtered classifier
                for (i = 0; i < this.classifiers.size(); i++) {
                    // create a new instance of a classifier
                    var newInstance = this.classifiers.get(i).getClass().getDeclaredConstructor().newInstance();
                    var filtered = this.getOverSaplingClassifier(currentStep);
                    filtered.setClassifier(newInstance);
                    this.classifiers.set(i, filtered);
                }
                break;
          /*  case SMOTE:
                break;*/
            default:
                break;
        }
    }

    private FilteredClassifier getOverSaplingClassifier(WalkStep currentStep) throws Exception {
        var classifier = new FilteredClassifier();

        var positive = currentStep.getPositives();
        var negative = currentStep.getNegatives();
        var minor = Math.min(positive, negative);
        var p = Math.abs(positive - negative) * 100 / minor;

        var resample = new Resample();
        //no decimal position
        String[] opts = new String[]{ "-B", "1.0", "-Z", String.valueOf(Integer.valueOf(p))};
        resample.setOptions(opts);
        classifier.setFilter(resample);

        return classifier;
    }

    private FilteredClassifier getUnderSaplingClassifier() throws Exception {
        //initialization of filteredClassifier for undersampling
        var classifier = new FilteredClassifier();
        var ss = new SpreadSubsample();
        String[] opts = new String[]{ "-M", "1.0"};
        ss.setOptions(opts);
        classifier.setFilter(ss);

        return classifier;
    }


    public void applyCostSensitive(CostSensitiveClassifierType csc){
        /*switch (csc){
            case SENSITIVE_THRESHOLD:
                break;
            case SENSITIVE_LEARNING:
                break;
            default:
                break;
        }*/
    }


    public WekaConfigurationOutput computeMetrics(FeaturesSelectionType fs, CostSensitiveClassifierType csc,
                                            SamplingType st) throws Exception {

        var output = new WekaConfigurationOutput(fs, st, csc);

        /*  feature selection is applied in walkStep: it keep a train and a test for both cases: none and features
            selection best first.
            To apply sampling, it's needed the training set: in fact, for oversampling and smoote, it needs the
             percentage that should be used in the filter.  */

        int i = 0;
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
