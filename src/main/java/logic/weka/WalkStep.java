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
import java.nio.file.Files;
import java.nio.file.Paths;

public class WalkStep {

    private Instances training;
    private Instances testing;

    private Instances featureSelectedTraining;
    private Instances featureSelectedTesting;

    public WalkStep(Instances totalData, int stepIndex) throws WalkStepFilterException {
        /* step index is the index of the last release index in train set for this step. */
        var sets = this.getInstancesSubsets(totalData, stepIndex);
        this.training = sets[0];
        this.testing = sets[1];
        this.training.setClassIndex(totalData.numAttributes() - 1);
        this.testing.setClassIndex(totalData.numAttributes() - 1);
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
            var totalDataFiltered = Filter.useFilter(totalData, filter);
            var numAttrFiltered = totalDataFiltered.numAttributes();

            // removing duplicated lines
            var currDir = Paths.get(".").toAbsolutePath().normalize().toFile();
            var ext = ".arff";
            var tmpFile = File.createTempFile("tmpArff", ext, currDir);
            ArffCreator.getInstance().createArff(tmpFile, totalDataFiltered);
            // reading filtered instances without replications from tmpFile
            var arffLoader = new ArffLoader();
            arffLoader.setSource(tmpFile);
            totalDataFiltered = arffLoader.getDataSet();
            // removing tmpFile
            Files.delete(tmpFile.toPath());

            // setting filtered sets
            sets = this.getInstancesSubsets(totalDataFiltered, stepIndex);
            this.featureSelectedTraining = sets[0];
            this.featureSelectedTesting = sets[1];
            this.featureSelectedTraining.setClassIndex(numAttrFiltered - 1);
            this.featureSelectedTesting.setClassIndex(numAttrFiltered - 1);
        }catch (Exception e){
            throw new WalkStepFilterException("Error creating features selectioned datasets");
        }



    }
    private Instances[] getInstancesSubsets(Instances totalData, int lastTrainReleaseIndex) {
        var sets = new Instances[2];
        int i;
        int currIndex;
        var countTrainingInstances = 0;
        var countTestingInstances = 0;
        for (i = 0; i < totalData.numInstances(); i++){
            currIndex = (int) totalData.instance(i).value(0);
            if (currIndex <= lastTrainReleaseIndex)
                countTrainingInstances++;
            else if (currIndex == lastTrainReleaseIndex + 1)
                countTestingInstances++;
        }
        // adding testing set
        sets[0] = new Instances(totalData, 0, countTrainingInstances);
        sets[1] = new Instances(totalData, countTrainingInstances, countTestingInstances);
        return sets;
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

    public int getPositivesTraining() {
        var count = 0;
        for (Instance instance : this.training){
            var curr = instance.toString(this.training.numAttributes() - 1);
            if (curr.equals("Yes"))
                count++;
        }
        return count;
    }

    public int getNegativesTraining() {
        return this.training.numInstances() - this.getPositivesTraining();
    }

    public int getPositivesTesting() {
        var count = 0;
        for (Instance instance : this.testing){
            var curr = instance.toString(this.testing.numAttributes() - 1);
            if (curr.equals("Yes"))
                count++;
        }
        return count;
    }

    public int getNegativesTesting() {
        return this.testing.numInstances() - this.getPositivesTesting();
    }
}
