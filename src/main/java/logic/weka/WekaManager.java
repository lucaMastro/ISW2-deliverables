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

    public void applySampling(SamplingType st) throws Exception {
        int i;
        //re-initialize cassifiers
        this.initializeClassifiers();
        switch (st){
            case UNDERSAMPLING:
                // just replace the classifiers with a filtered classifier
                for (i = 0; i < this.classifiers.size(); i++) {
                    var newInstance = this.classifiers.get(i).getClass().getDeclaredConstructor().newInstance();
                    var filtered = this.getUndersaplingClassifier();
                    filtered.setClassifier(newInstance);
                    this.classifiers.set(i, filtered);
                }
                break;
          /*  case OVERSAMPLING:
                break;
            case SMOTE:
                break;*/
            default:
                break;
        }
    }

    private FilteredClassifier getUndersaplingClassifier() throws Exception {
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
            selection best first.    */
        this.applySampling(st);
        this.applyCostSensitive(csc);

        int i;
        for (i = 0; i < this.getNumOfRelease() - 1; i++) {
            var trainingDataset = this.steps.get(i).getTrainingSet(fs);
            var testingDataset = this.steps.get(i).getTestingSet(fs);

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
