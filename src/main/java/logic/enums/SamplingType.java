package logic.enums;

public enum SamplingType {
    NONE("none"),
    OVERSAMPLING("oversampling"),
    UNDERSAMPLING("undersampling"),
    SMOTE("smote");


    public final String semplingType;
    SamplingType(String type){ this.semplingType = type; }

    public String getType(){ return this.semplingType; }

}
