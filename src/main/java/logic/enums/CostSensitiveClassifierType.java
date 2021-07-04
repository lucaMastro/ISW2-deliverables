package logic.enums;

public enum CostSensitiveClassifierType {
    NONE("none"),
    SENSITIVE_THRESHOLD("sensitive_threshold"),
    SENSITIVE_LEARNING("sensitive_learning");

    public final String costSensitiveClassifier;
    CostSensitiveClassifierType(String type){ this.costSensitiveClassifier = type; }

    public String getType(){ return this.costSensitiveClassifier; }

}
