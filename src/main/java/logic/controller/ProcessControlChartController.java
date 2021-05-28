package logic.controller;

import logic.bean.BugginessAndProcessChartBean;
import logic.dataset_manager.ProcessControlChartDataset;
import logic.exception.InvalidRangeException;
import logic.proportion_algo.ProportionIncrement;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessControlChartController {

    public void run(BugginessAndProcessChartBean bean) throws IOException {
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
