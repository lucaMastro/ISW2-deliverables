package logic.weka;

import weka.classifiers.CostMatrix;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

public class FilterCreator {

    public FilteredClassifier getOverSaplingClassifier(WalkStep currentStep) throws Exception {
        var classifier = new FilteredClassifier();

        var positive = currentStep.getPositivesTraining();
        var negative = currentStep.getNegativesTraining();
        var minor = Math.min(positive, negative);
        var p = Math.abs(positive - negative) * 100 / minor;

        var resample = new Resample();
        //no decimal position
        String[] opts = new String[]{ "-B", "1.0", "-Z", String.valueOf(Integer.valueOf(p))};
        resample.setOptions(opts);
        classifier.setFilter(resample);

        return classifier;
    }

    public FilteredClassifier getUnderSaplingClassifier() throws Exception {
        //initialization of filteredClassifier for undersampling
        var classifier = new FilteredClassifier();
        var ss = new SpreadSubsample();
        String[] opts = new String[]{ "-M", "1.0"};
        ss.setOptions(opts);
        classifier.setFilter(ss);

        return classifier;
    }

    public FilteredClassifier getSMOTEClassifier() throws Exception {
        var classifier = new FilteredClassifier();
        var smote = new SMOTE();
        classifier.setFilter(smote);
        return classifier;
    }

    /* *********** CSC ************ */

    private CostMatrix createCostMatrix(){
        var costMatrix = new CostMatrix(2);
        var cfp = 1.0;

        costMatrix.setCell(0,0, 0.0);
        costMatrix.setCell(1,0, cfp); //cost false positive
        costMatrix.setCell(0,1, 10 * cfp);
        costMatrix.setCell(1,1, 0.0);
        return  costMatrix;
    }


    private CostSensitiveClassifier createCostSensitiveClassifier(Boolean b){
        //boolean should be true for sensitive threshold and false for sensitive learning
        var costMatrix = this.createCostMatrix();
        var sensitiveClassifier = new CostSensitiveClassifier();
        sensitiveClassifier.setCostMatrix(costMatrix);

        sensitiveClassifier.setMinimizeExpectedCost(b);

        return sensitiveClassifier;
    }

    public CostSensitiveClassifier sensitiveThresholdClassifier(){
        return this.createCostSensitiveClassifier(Boolean.TRUE);
    }

    public CostSensitiveClassifier sensitiveLearningClassifier(){
        return this.createCostSensitiveClassifier(Boolean.FALSE);
    }


}
