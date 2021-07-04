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
import java.util.List;

public class WekaManager {

    private ArrayList<WalkStep> steps;
    private int numRelease;
    private WalkForwardSetsManager filesManager;

    private FilteredClassifier undersapling;

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

        //initialization of filteredClassifier for undersampling
        this.undersapling = new FilteredClassifier();
        var ss = new SpreadSubsample();
        String[] opts = new String[]{ "-M", "1.0"};
        ss.setOptions(opts);
        this.undersapling.setFilter(ss);

        //classifiers
        this.classifiers = new ArrayList<>();
        this.classifiers.add(new RandomForest());
        this.classifiers.add(new NaiveBayes());
        this.classifiers.add(new IBk());

    }


    public List<Evaluation> computeMetrics(int releseIndex) throws Exception {
        int i;
        Classifier classifier;
        Evaluation evaluation;

        /*  sets settings    */
        var trainingDataset = this.steps.get(releseIndex - 1).getTrainingSet();
        var testingDataset = this.steps.get(releseIndex - 1).getTestingSet();

        /*  classifiers train */
        for (Classifier c : this.classifiers)
            c.buildClassifier(trainingDataset);

        /*  evaluation creation */
        var evaluations = new ArrayList<Evaluation>();
        for (i = 0; i < this.classifiers.size(); i++)
            evaluations.add(new Evaluation(testingDataset));

        for (i = 0; i < this.classifiers.size(); i++){
            classifier = this.classifiers.get(i);
            evaluation = evaluations.get(i);
            evaluation.evaluateModel(classifier, testingDataset);
        }
        return evaluations;
    }

    public int getNumOfRelease() {
        return this.filesManager.getNumOfRelease();
    }

    public String getDatasetName() {
        return this.filesManager.getDatasetName();
    }


    public void applyFeaturesSelection(FeaturesSelectionType fs){
        /*  just 2 options  */
        if (fs.equals(FeaturesSelectionType.BEST_FIRST)){
            // TODO: 03/07/21
        }
    }

    public void applySampling(SamplingType st){

        switch (st){
            case UNDERSAMPLING:
                break;
            case OVERSAMPLING:
                break;
            case SMOTE:
                break;
            default:
                break;
        }
    }

    public void applyCostSensitive(CostSensitiveClassifierType csc){
        switch (csc){
            case SENSITIVE_THRESHOLD:
                break;
            case SENSITIVE_LEARNING:
                break;
            default:
                break;
        }
    }


    public WekaConfigurationOutput computeMetrics(FeaturesSelectionType fs, CostSensitiveClassifierType csc, SamplingType st)
            throws Exception {

        var output = new WekaConfigurationOutput(fs, st, csc);

        this.applyFeaturesSelection(fs);
        this.applySampling(st);
        this.applyCostSensitive(csc);

        int i;
        for (i = 0; i < this.getNumOfRelease() - 1; i++) {
            var trainingDataset = this.steps.get(i).getTrainingSet();
            var testingDataset = this.steps.get(i).getTestingSet();

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
