package logic.bean;

import logic.enums.CostSensitiveClassifierType;
import logic.enums.FeaturesSelectionType;
import logic.enums.SamplingType;
import java.util.ArrayList;
import java.util.List;

public class WekaConfigurationOutputBean {
    /*  this class is meant to store weka analysis output of a given combination of
        FeaturesSelection, Cost Sensitive Classifier and Sampling   */

    private FeaturesSelectionType featuresSelection;
    private SamplingType samplingType;
    private CostSensitiveClassifierType costSensitiveClassifier;
    private List<WekaStepOutputBean> stepEvaluations;


    public WekaConfigurationOutputBean(FeaturesSelectionType fs, SamplingType st, CostSensitiveClassifierType csc){
        this.featuresSelection = fs;
        this.samplingType = st;
        this.costSensitiveClassifier = csc;
        this.stepEvaluations = new ArrayList<>();
    }


    public FeaturesSelectionType getFeaturesSelection() {
        return featuresSelection;
    }

    public SamplingType getSamplingType() {
        return samplingType;
    }

    public CostSensitiveClassifierType getCostSensitiveClassifier() {
        return costSensitiveClassifier;
    }

    public List<WekaStepOutputBean> getStepEvaluations() {
        return this.stepEvaluations;
    }

    public void appendStepEvaluation(WekaStepOutputBean stepOutput) {
        this.stepEvaluations.add(stepOutput);
    }
}
