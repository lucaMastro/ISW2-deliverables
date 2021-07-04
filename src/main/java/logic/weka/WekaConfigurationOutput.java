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

    private List<Evaluation> evaluations;

    private String title;

    public WekaConfigurationOutput(FeaturesSelectionType fs, SamplingType st, CostSensitiveClassifierType csc){
        this.featuresSelection = fs;
        this.samplingType = st;
        this.costSensitiveClassifier = csc;

        this.evaluations = new ArrayList<>();

        var s = new StringBuilder("Output for: ");
        this.title = s.append("Features Selection = ").append(this.featuresSelection.getType()).append(",")
                .append("Sampling = ").append(this.samplingType.getType()).append(",")
                .append("Cost Sensitive = ").append(this.costSensitiveClassifier.getType()).append("\n").toString();
    }

    public void appendEvaluation(Evaluation eval){
        this.evaluations.add(eval);
    }

    public String getTitle() {
        return title;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }
}
