package logic.bean;

import logic.abstracts.AbstractBean;
import logic.exception.InvalidInputException;

public class ProcessChartBean extends AbstractBean {

    private Integer threshold;

    public ProcessChartBean(String outputFile, String dirPath, String projectName, Integer thr)
            throws InvalidInputException {

        super(outputFile, dirPath, projectName);
        this.threshold = thr;
    }

    public Integer getThreshold() {
        return threshold;
    }
}
