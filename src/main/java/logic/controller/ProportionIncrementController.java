package logic.controller;

import logic.bean.BugginessAndProcessChartBean;
import logic.dataset_manager.Dataset;
import logic.dataset_manager.Release;
import logic.dataset_manager.ReleaseFile;
import logic.exception.InvalidRangeException;
import logic.proportion_algo.ProportionIncrement;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProportionIncrementController {

    public void run(BugginessAndProcessChartBean bean) throws IOException, GitAPIException, InvalidRangeException {
        ProportionIncrement proportionIncrement = new ProportionIncrement(bean);
        Dataset dataset = proportionIncrement.getDataset();
        dataset.computeFeatures();
        proportionIncrement.computeProportionIncrement();
        proportionIncrement.setDatasetBugginess();

        File file = bean.getOutputFile();
        try (FileWriter fw = new FileWriter(file)) {
            String chosenFeatures = "Version,File Name,LOC,NR,NFix,NAuth,LOC_added,MAX_LOC_added,Churn,MAX_Churn,Age,Buggy\n";
            fw.append(chosenFeatures);
            for (Release r : proportionIncrement.getDataset().getReleases()) {
                String rIndex = r.getIndex().toString() + ",";
                StringBuilder line = new StringBuilder();
                for (ReleaseFile rf : r.getFiles())
                    line.append(rIndex).append(rf.toString());
                fw.append(line.toString());
            }

        } catch (Exception e) {
            Logger logger = Logger.getLogger(ProportionIncrement.class.getName());
            logger.log(Level.OFF, e.toString());
        }
    }
}
