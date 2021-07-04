package logic.weka;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;

import java.io.File;

public class WalkStep {

    private Instances training;
    private Instances testing;

    private Instances undersampledTraining;
    private Instances oversampledTraining;
    private Instances smoteSampledTraining;

    public WalkStep(File train, File test, int numAttr) throws Exception {

        var loader = new ArffLoader();

        loader.setSource(train);
        this.training = loader.getDataSet();

        loader.setSource(test);
        this.testing = loader.getDataSet();

        this.training.setClassIndex(numAttr - 1);
        this.testing.setClassIndex(numAttr - 1);


        //undersampling
        var undersamplingCopy = new Instances(this.training);
        var resample = new Resample();
        resample.setInputFormat(undersamplingCopy);
        var fc = new FilteredClassifier();
        var naiveBayesSampeld = new NaiveBayes();
        fc.setClassifier(naiveBayesSampeld);

        SpreadSubsample spreadSubsample = new SpreadSubsample();
        String[] opts = new String[]{ "-M", "1.0"};
        spreadSubsample.setOptions(opts);

        fc.setFilter(spreadSubsample);


        fc.buildClassifier(training);
        Evaluation eval2 = new Evaluation(testing);
        eval2.evaluateModel(fc, testing); //sampled
    }

    public Instances getTrainingSet() {
        return training;
    }

    public Instances getTestingSet() {
        return testing;
    }
}
