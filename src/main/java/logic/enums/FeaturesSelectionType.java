package logic.enums;

public enum FeaturesSelectionType {
    NONE("none"),
    BEST_FIRST("best_first");

    public final String featuresSelection;
    FeaturesSelectionType(String type){ this.featuresSelection = type; }

    public String getType(){ return this.featuresSelection; }
}
