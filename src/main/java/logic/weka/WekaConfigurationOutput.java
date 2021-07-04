package logic.weka;

import logic.enums.CostSensitiveClassifier;
import logic.enums.FeaturesSelection;
import logic.enums.SamplingType;
import org.decimal4j.util.DoubleRounder;
import weka.classifiers.Evaluation;

import java.util.ArrayList;
import java.util.List;

public class WekaConfigurationOutput {
    /*  this class is meant to store weka analysis output of a given combination of
        FeaturesSelection, Cost Sensitive Classifier and Sampling   */

    private FeaturesSelection featuresSelection;
    private SamplingType samplingType;
    private CostSensitiveClassifier costSensitiveClassifier;

    private List<Evaluation> evaluations;

    private String datasetName;
    private String title;

    public WekaConfigurationOutput(FeaturesSelection fs, SamplingType st, CostSensitiveClassifier csc, String dn){
        this.featuresSelection = fs;
        this.samplingType = st;
        this.costSensitiveClassifier = csc;

        this.evaluations = new ArrayList<>();
        this.datasetName = dn;

        var s = new StringBuilder("Output for: ");
        this.title = s.append(this.featuresSelection.getType()).append(",")
                .append(this.samplingType.getType()).append(",")
                .append(this.costSensitiveClassifier.getType()).append("\n").toString();
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
