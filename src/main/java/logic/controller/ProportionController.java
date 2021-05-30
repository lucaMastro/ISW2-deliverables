package logic.controller;

import logic.bean.ProportionBean;
import logic.dataset_manager.ProportionDataset;
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

        ProportionDataset dataset;

        /*  Checking proportion algo to use */
        switch (bean.getProportionAlgo()){
            case PROPORTION_INCREMENT:
                dataset = new ProportionDataset(bean);
                dataset.computeFeatures();
                this.proportionIncrementMode(dataset);
                break;
            case PROPORTION_MOVING_WINDOW: //i want to implement also this
                throw new NotAvaiableAlgorithm("Algorithm not avaiable.");
            case PROPORTION_COLD_START:
                throw new NotAvaiableAlgorithm("Algorithm not avaiable.");
        }

        var file = bean.getOutputFile();
        try (var fw = new FileWriter(file)) {
            fw.append(dataset.toString());
        } catch (Exception e) {
            var logger = Logger.getLogger(ProportionIncrement.class.getName());
            logger.log(Level.OFF, e.toString());
        }
    }

    private void proportionIncrementMode(ProportionDataset dataset){
        var proportionIncrement = new ProportionIncrement(dataset);
        proportionIncrement.computeProportionIncrement();
        proportionIncrement.setDatasetBugginess();
    }
}
