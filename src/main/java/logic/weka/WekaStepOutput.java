package logic.weka;

import weka.classifiers.Evaluation;

public class WekaStepOutput {
    private double trainingPercentage;
    private double defectiveInTrainingPercentage;
    private double defectiveInTestingPercentage;
    private Evaluation[] evaluation;

    public WekaStepOutput(int evaluationsNumber){
        this.evaluation = new Evaluation[evaluationsNumber];
    }

    public double getTrainingPercentage() {
        return trainingPercentage;
    }

    public void setTrainingPercentage(double trainingPercentage) {
        this.trainingPercentage = trainingPercentage;
    }

    public double getDefectiveInTrainingPercentage() {
        return defectiveInTrainingPercentage;
    }

    public void setDefectiveInTrainingPercentage(double defectiveInTrainingPercentage) {
        this.defectiveInTrainingPercentage = defectiveInTrainingPercentage;
    }

    public double getDefectiveInTestingPercentage() {
        return defectiveInTestingPercentage;
    }

    public void setDefectiveInTestingPercentage(double defectiveInTestingPercentage) {
        this.defectiveInTestingPercentage = defectiveInTestingPercentage;
    }

    public void setEvaluation(Evaluation evaluation, int i) {
        this.evaluation[i] = evaluation;
    }

    public Evaluation[] getEvaluationsArray(){
        return this.evaluation;
    }
}
