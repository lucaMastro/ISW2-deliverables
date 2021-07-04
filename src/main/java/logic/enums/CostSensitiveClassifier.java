package logic.enums;

public enum CostSensitiveClassifier {
    NONE("none"),
    SENSITIVE_THRESHOLD("sensitive_threshold"),
    SENSITIVE_LEARNING("sensitive_learning");

    public final String costSensitiveClassifier;
    CostSensitiveClassifier(String type){ this.costSensitiveClassifier = type; }

    public String getType(){ return this.costSensitiveClassifier; }

}
