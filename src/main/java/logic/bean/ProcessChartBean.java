package logic.bean;

import logic.exception.InvalidInputException;

public class ProcessChartBean extends ProportionBean {

    private Integer threshold;

    public ProcessChartBean(String outputFile, String dirPath, String projectName, Integer thr)
            throws InvalidInputException {
        super(outputFile,dirPath,projectName);
        this.threshold = thr;
    }

    public Integer getThreshold() {
        return threshold;
    }
}
