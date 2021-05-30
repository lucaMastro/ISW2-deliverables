package logic.controller;

import logic.bean.ProcessChartBean;
import logic.dataset_manager.ProcessControlChartDataset;
import logic.proportion_algo.ProportionIncrement;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessControlChartController {

    public void run(ProcessChartBean bean) throws IOException {
        var dataset = new ProcessControlChartDataset(bean);
        dataset.computeFeatures();

        var file = bean.getOutputFile();
        try (var fw = new FileWriter(file)) {
            fw.append(dataset.toString());
        } catch (Exception e) {
            var logger = Logger.getLogger(ProportionIncrement.class.getName());
            logger.log(Level.OFF, e.toString());
        }
    }
}
