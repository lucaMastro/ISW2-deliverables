package logic.boundary;

import logic.bean.WekaBean;
import logic.controller.WekaController;
import logic.weka.WalkForwardSetsManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WekaAnalisysBoundary {

    String csvInputFile;
    String csvOutputFile;
    String arfOutputFile = null;

    public WekaAnalisysBoundary(String in, String csvOut, String arffOut){
        this(in, csvOut);
        this.arfOutputFile = arffOut;
    }

    public WekaAnalisysBoundary(String in, String csvOut) {
        this.csvInputFile = in;
        this.csvOutputFile = csvOut;
    }

    public void runAnalisys() throws IOException {
        WekaBean bean = null;
        try {
            bean = new WekaBean(this.csvInputFile, this.csvOutputFile, this.arfOutputFile);
            var controller = new WekaController();
            controller.run(bean);
        }catch (Exception e){
            var logger = Logger.getLogger(WalkForwardSetsManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
        finally {
            if (bean != null)
                bean.removeTempFiles();
        }
    }

    public static void main(String[] args) throws Exception {

        var boundary = new WekaAnalisysBoundary("/home/luca/Scrivania/bookkeeperGUI.csv",
                "/home/luca/Scrivania/wekaOutput3.csv",
                "/home/luca/Scrivania/bookkeeperARFF.arff");
        boundary.runAnalisys();

    }
}
