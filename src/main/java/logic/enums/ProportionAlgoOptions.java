package logic.enums;

public enum ProportionAlgoOptions {
    PROPORTION_COLD_START("Cold start"),
    PROPORTION_INCREMENT("Increment"),
    PROPORTION_MOVING_WINDOW("Moving window");

    public final String algo;
    ProportionAlgoOptions(String algo){
        this.algo = algo;
    }

    public String getAlgo(){
        return algo;
    }

}
