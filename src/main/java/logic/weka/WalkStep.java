package logic.weka;

import logic.enums.FeaturesSelectionType;
import logic.exception.WalkStepFilterException;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.io.File;
import java.io.IOException;

public class WalkStep {

    private Instances training;
    private Instances testing;

    private Instances featureSelectedTraining;
    private Instances featureSelectedTesting;

    public WalkStep(File train, File test, int numAttr) throws IOException, WalkStepFilterException {

        var loader = new ArffLoader();

        loader.setSource(train);
        this.training = loader.getDataSet();

        loader.setSource(test);
        this.testing = loader.getDataSet();

        this.training.setClassIndex(numAttr - 1);
        this.testing.setClassIndex(numAttr - 1);


        //create AttributeSelection object
        var filter = new AttributeSelection();
        //create evaluator and search algorithm objects
        var eval = new CfsSubsetEval();
        var search = new BestFirst();

        //set the filter to use the evaluator and search algorithm
        filter.setEvaluator(eval);
        filter.setSearch(search);

        try {
            //specify the dataset
            filter.setInputFormat(this.training);
            //apply
            this.featureSelectedTraining = Filter.useFilter(this.training, filter);
            this.featureSelectedTesting = Filter.useFilter(this.testing, filter);

            var numAttrFiltered = this.featureSelectedTraining.numAttributes();
            this.featureSelectedTraining.setClassIndex(numAttrFiltered - 1);
            this.featureSelectedTesting.setClassIndex(numAttrFiltered - 1);
        }catch (Exception e){
            throw new WalkStepFilterException("Error creating features selectioned datasets");
        }

    }

    public Instances getTrainingSet(FeaturesSelectionType fs) {
        if (fs.equals(FeaturesSelectionType.BEST_FIRST))
            return this.featureSelectedTraining;
        else
            return this.training;
    }

    public Instances getTestingSet(FeaturesSelectionType fs) {
        if (fs.equals(FeaturesSelectionType.BEST_FIRST))
            return this.featureSelectedTesting;
        else
            return this.testing;
    }

    public int getPositives() {
        var count = 0;
        for (Instance instance : this.training){
            var curr = instance.toString(this.training.numAttributes() - 1);
            if (curr.equals("Yes"))
                count++;
        }
        return count;
    }

    public int getNegatives() {
        return this.training.numInstances() - this.getPositives();
    }
}
