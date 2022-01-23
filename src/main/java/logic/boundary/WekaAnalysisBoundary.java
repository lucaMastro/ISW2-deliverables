package logic.boundary;

import logic.bean.WekaBean;
import logic.controller.WekaController;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WekaAnalysisBoundary {

    String csvInputFile;
    String csvOutputFile;
    String arfOutputFile = null;

    public WekaAnalysisBoundary(String in, String csvOut, String arffOut){
        this(in, csvOut);
        this.arfOutputFile = arffOut;
    }

    public WekaAnalysisBoundary(String in, String csvOut) {
        this.csvInputFile = in;
        this.csvOutputFile = csvOut;
    }

    public void runAnalysis() throws IOException {
        WekaBean bean = null;
        try {
            bean = new WekaBean(this.csvInputFile, this.csvOutputFile, this.arfOutputFile);
            var controller = new WekaController();
            controller.run(bean);
        }catch (Exception e){
            var logger = Logger.getLogger(WekaAnalysisBoundary.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
        finally {
            if (bean != null)
                bean.removeTempFiles();
        }
    }

    public static void main(String[] args) throws Exception {

        var boundary = new WekaAnalysisBoundary("/home/luca/Scrivania/bookkeeperGUI.csv",
                "/home/luca/Scrivania/wekaOutput25.csv",
                "/home/luca/Scrivania/bookkeeperARFF2.arff");
        boundary.runAnalysis();

    }
}
