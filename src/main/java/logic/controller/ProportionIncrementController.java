package logic.controller;

import logic.bean.ProcessChartBean;
import logic.bean.ProportionBean;
import logic.dataset_manager.ProportionDataset;
import logic.dataset_manager.Release;
import logic.dataset_manager.ReleaseFile;
import logic.exception.InvalidRangeException;
import logic.proportion_algo.ProportionIncrement;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProportionIncrementController {

    public void run(ProportionBean bean) throws IOException, GitAPIException, InvalidRangeException {
        var proportionIncrement = new ProportionIncrement(bean);
        var dataset = (ProportionDataset) proportionIncrement.getDataset();
        dataset.computeFeatures();
        proportionIncrement.computeProportionIncrement();
        proportionIncrement.setDatasetBugginess();

        var file = bean.getOutputFile();
        try (var fw = new FileWriter(file)) {
            var chosenFeatures = "Version,File Name,LOC,NR,NFix,NAuth,LOC_added,MAX_LOC_added,Churn,MAX_Churn,Age,Buggy\n";
            fw.append(chosenFeatures);
            for (Release r : dataset.getReleases()) {
                String rIndex = r.getIndex().toString() + ",";
                var line = new StringBuilder();
                for (ReleaseFile rf : r.getFiles())
                    line.append(rIndex).append(rf.toString());
                fw.append(line.toString());
            }

        } catch (Exception e) {
            var logger = Logger.getLogger(ProportionIncrement.class.getName());
            logger.log(Level.OFF, e.toString());
        }
    }
}
