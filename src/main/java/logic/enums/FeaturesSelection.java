package logic.enums;

public enum FeaturesSelection {
    NONE("none"),
    BEST_FIRST("best_first");

    public final String featuresSelection;
    FeaturesSelection(String type){ this.featuresSelection = type; }

    public String getType(){ return this.featuresSelection; }
}
