package logic.enums;

public enum SamplingType {
    NONE("none"),
    OVERSAMPLING("oversampling"),
    UNDERSAMPLING("undersampling");//,
    //SMOTE("smote");


    public final String sampling;
    SamplingType(String type){ this.sampling = type; }

    public String getType(){ return this.sampling; }

}
