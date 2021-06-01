package logic.controller;

import logic.bean.ProportionBean;
import logic.dataset_manager.ProportionDataset;
import logic.dataset_manager.Release;
import logic.dataset_manager.ReleaseFile;
import logic.exception.InvalidRangeException;
import logic.exception.NotAvaiableAlgorithm;
import logic.proportion_algo.ProportionIncrement;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProportionController {

    public void run(ProportionBean bean)
            throws IOException, GitAPIException, InvalidRangeException, NotAvaiableAlgorithm {

        ProportionDataset dataset = null;

        /*  Checking proportion algo to use */
        switch (bean.getProportionAlgo()){
            case PROPORTION_INCREMENT:
                dataset = new ProportionDataset(bean);
                dataset.computeFeatures();
                this.proportionIncrementMode(dataset);
                this.writeToFile(bean, dataset);
                break;
            case PROPORTION_MOVING_WINDOW: //i want to implement also this
                throw new NotAvaiableAlgorithm("Algorithm not avaiable.");
            case PROPORTION_COLD_START:
                throw new NotAvaiableAlgorithm("Algorithm not avaiable.");
        }
    }

    private void proportionIncrementMode(ProportionDataset dataset){
        var proportionIncrement = new ProportionIncrement(dataset);
        proportionIncrement.computeProportionIncrement();
    }

    private void writeToFile(ProportionBean bean, ProportionDataset dataset){
        assert dataset != null;

        var file = bean.getOutputFile();
        try (var fw = new FileWriter(file)) {
            var chosenFeatures = "Version,File Name,LOC,NR,NFix,NAuth,LOC_added,MAX_LOC_added,Churn,MAX_Churn,Age,Buggy\n";
            fw.append(chosenFeatures);
            //var bld = new StringBuilder(chosenFeatures);
            for (Release r : dataset.getReleases()){
                String rIndex = r.getIndex().toString() + ",";
                for (ReleaseFile rf : dataset.getFiles()) {
                    var line = rf.getOutputLine(r.getIndex());
                    if (!line.isEmpty()) {
                        //bld.append(rIndex).append(rf.getOutputLine(r.getIndex()));
                        fw.append(rIndex);
                        fw.append(rf.getOutputLine(r.getIndex()));
                    }
                }
            }


        } catch (Exception e) {
            var logger = Logger.getLogger(ProportionIncrement.class.getName());
            logger.log(Level.OFF, e.toString());
        }
    }
}
