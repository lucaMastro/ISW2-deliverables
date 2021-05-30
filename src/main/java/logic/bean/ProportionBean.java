package logic.bean;

import logic.abstracts.AbstractBean;
import logic.enums.ProportionAlgoOptions;
import logic.exception.InvalidInputException;

public class ProportionBean extends AbstractBean {

    ProportionAlgoOptions proportionAlgo;

    public ProportionBean(String outputFile, String dirPath, String projectName, String proportion)
            throws InvalidInputException {

        super(outputFile, dirPath, projectName);
        for (ProportionAlgoOptions obj : ProportionAlgoOptions.values()){
            if (obj.getAlgo().equals(proportion)){
                this.proportionAlgo = obj;
                break;
            }
        }
    }

    public ProportionAlgoOptions getProportionAlgo() {
        return proportionAlgo;
    }
}
