package logic.weka;

import logic.enums.CostSensitiveClassifierType;
import logic.enums.FeaturesSelectionType;
import logic.enums.SamplingType;
import weka.classifiers.Evaluation;
import java.util.ArrayList;
import java.util.List;

public class WekaConfigurationOutput {
    /*  this class is meant to store weka analysis output of a given combination of
        FeaturesSelection, Cost Sensitive Classifier and Sampling   */

    private FeaturesSelectionType featuresSelection;
    private SamplingType samplingType;
    private CostSensitiveClassifierType costSensitiveClassifier;
    private List<WekaStepOutput> stepEvaluations;


    public WekaConfigurationOutput(FeaturesSelectionType fs, SamplingType st, CostSensitiveClassifierType csc){
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

    public List<WekaStepOutput> getStepEvaluations() {
        return this.stepEvaluations;
    }

    public void appendStepEvaluation(WekaStepOutput stepOutput) {
        this.stepEvaluations.add(stepOutput);
    }
}
