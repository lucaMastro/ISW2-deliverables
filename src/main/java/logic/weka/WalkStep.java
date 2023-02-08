package logic.weka;

import logic.enums.FeaturesSelectionType;
import logic.exception.WalkStepFilterException;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.pmml.jaxbbindings.True;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WalkStep {

    private Instances training;
    private Instances testing;

    private int yesTrainInstance = 0;
    private int noTrainInstance = 0;

    private int yesTestInstance = 0;
    private int noTestInstance = 0;

    private Instances featureSelectedTraining;
    private Instances featureSelectedTesting;

    private int yesFeaturedTrainInstance = 0;
    private int noFeaturedTrainInstance = 0;

    private int yesFeaturedTestInstance = 0;
    private int noFeaturedTestInstance = 0;


    public WalkStep(Instances totalData, int stepIndex) throws WalkStepFilterException {
        /* step index is the index of the last release index in train set for this step. */
        var counts = this.countTrainAndTestInstances(stepIndex, totalData);
        var countTrainingInstances = counts[0];
        var countTestingInstances = counts[1];
        int[] yes_no;

        this.training = new Instances(totalData, 0, countTrainingInstances);
        this.testing = new Instances(totalData, countTrainingInstances, countTestingInstances);

        this.training.setClassIndex(totalData.numAttributes() - 1);
        this.testing.setClassIndex(totalData.numAttributes() - 1);

        // computing Yes number in training:
        yes_no = this.getYesNoNumbers(true, false);
        this.yesTrainInstance = yes_no[0];
        this.noTrainInstance = yes_no[1];
        /*
        for (Instance instance : this.training){
            var curr = instance.toString(this.testing.numAttributes() - 1);
            if (curr.equals("Yes"))
                this.yesTrainInstance++;
            else
                this.noTrainInstance++;
        }
         */
        // computing Yes number in testing:
        yes_no = this.getYesNoNumbers(false, false);
        this.yesTestInstance = yes_no[0];
        this.noTestInstance = yes_no[1];
        /*
        for (Instance instance : this.testing){
            var curr = instance.toString(this.testing.numAttributes() - 1);
            if (curr.equals("Yes"))
                this.yesTestInstance++;
            else
                this.noTestInstance++;
        }
        */
        try {
            var totalDataFeatured = this.applyFeatureSelection(totalData);
            var numAttrFiltered = totalDataFeatured.numAttributes();

            // create featured subsets:
            var featuredTrainWithDup = new Instances(totalDataFeatured, 0, countTrainingInstances);
            var featuredTestWithDup = new Instances(totalDataFeatured, countTrainingInstances, countTestingInstances);

            this.featureSelectedTraining = this.removeDuplicated(featuredTrainWithDup);
            this.featureSelectedTesting = this.removeDuplicated(featuredTestWithDup);

            this.featureSelectedTraining.setClassIndex(numAttrFiltered - 1);
            this.featureSelectedTesting.setClassIndex(numAttrFiltered - 1);

            // computing No number in featured training:
            yes_no = this.getYesNoNumbers(true, true);
            this.yesFeaturedTrainInstance = yes_no[0];
            this.noFeaturedTrainInstance = yes_no[1];
        /*
            for (Instance instance : this.featureSelectedTraining){
                var curr = instance.toString(this.featureSelectedTraining.numAttributes() - 1);
                if (curr.equals("Yes"))
                    this.yesFeaturedTrainInstance++;
                else
                    this.noFeaturedTrainInstance++;
            }
         */
            // computing No number in featured testing:
            yes_no = this.getYesNoNumbers(false, true);
            this.yesFeaturedTestInstance = yes_no[0];
            this.noFeaturedTestInstance = yes_no[1];
        /*
            for (Instance instance : this.featureSelectedTesting){
                var curr = instance.toString(this.featureSelectedTesting.numAttributes() - 1);
                if (curr.equals("Yes"))
                    this.yesFeaturedTestInstance++;
                else
                    this.noFeaturedTestInstance++;
            }*/

        }catch (Exception e){
            throw new WalkStepFilterException("Error creating features selectioned datasets");
        }
    }

    private int[] getYesNoNumbers(boolean training, boolean featured){
        Instances dataset;
        int[] yes_no = new int[2];
        var yes = 0;
        var no = 0;
        if (training) {
            if (featured)
                dataset = this.featureSelectedTraining;
            else
                dataset = this.training;
        }
        else {
            if (featured)
                dataset = this.featureSelectedTesting;
            else
                dataset = this.testing;
        }
        for (Instance instance : dataset){
            var curr = instance.toString(dataset.numAttributes() - 1);
            if (curr.equals("Yes"))
                yes++;
            else
                no++;
        }
        yes_no[0] = yes;
        yes_no[1] = no;
        return yes_no;
    }


    private Instances removeDuplicated(Instances featuredWithDup) throws Exception {
        // create temp file
        var currDir = Paths.get(".").toAbsolutePath().normalize().toFile();
        var ext = ".arff";
        var tmpFile = File.createTempFile("tmpArff", ext, currDir);

        // write instances on tmpFile
        ArffCreator.createArffWithoutDuplicated(tmpFile, featuredWithDup);

        // reading instances without duplicated:
        var arffLoader = new ArffLoader();
        arffLoader.setSource(tmpFile);
        var withoutDuplicated = arffLoader.getDataSet();

        // removing temp file
        Files.delete(tmpFile.toPath());

        return withoutDuplicated;
    }

    private int[] countTrainAndTestInstances(int stepIndex, Instances totalData){
        var counts = new int[2];
        int i;
        int currIndex;
        var countTrainingInstances = 0;
        var countTestingInstances = 0;
        for (i = 0; i < totalData.numInstances(); i++){
            currIndex = (int) totalData.instance(i).value(0);
            if (currIndex <= stepIndex)
                countTrainingInstances++;
            else if (currIndex == stepIndex + 1)
                countTestingInstances++;
            else
                break;
        }
        // the order is: [training-count, testing-count]
        counts[0] = countTrainingInstances;
        counts[1] = countTestingInstances;

        return counts;
    }

    private Instances applyFeatureSelection(Instances set) throws Exception {
        Instances featuredSet;
        //create AttributeSelection object
        var filter = new AttributeSelection();
        //create evaluator and search algorithm objects
        var eval = new CfsSubsetEval();
        var search = new BestFirst();

        //set the filter to use the evaluator and search algorithm
        filter.setEvaluator(eval);
        filter.setSearch(search);

        //specify the dataset
        filter.setInputFormat(set);
        //apply
        featuredSet = Filter.useFilter(set, filter);

        return featuredSet;
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

    public int getPositivesTraining(FeaturesSelectionType fs) {
        if (fs.equals(FeaturesSelectionType.BEST_FIRST))
            return this.yesFeaturedTrainInstance;
        else
            return this.yesTrainInstance;
    }

    public int getNegativesTraining(FeaturesSelectionType fs) {
        if (fs.equals(FeaturesSelectionType.BEST_FIRST))
            return this.noFeaturedTrainInstance;
        else
            return this.noTrainInstance;
    }

    public int getPositivesTesting(FeaturesSelectionType fs) {
        if (fs.equals(FeaturesSelectionType.BEST_FIRST))
            return this.yesFeaturedTestInstance;
        else
            return this.yesTestInstance;
    }

    public int getNegativesTesting(FeaturesSelectionType fs) {
        if (fs.equals(FeaturesSelectionType.BEST_FIRST))
            return this.noFeaturedTestInstance;
        else
            return this.noTestInstance;
    }
}
