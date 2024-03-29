package logic.boundary;

import logic.bean.WekaBean;
import logic.controller.WekaController;

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

    public void runAnalysis() throws Exception {
        WekaBean bean = null;
        try {
            bean = new WekaBean(this.csvInputFile, this.csvOutputFile, this.arfOutputFile);
            var controller = new WekaController();
            controller.run(bean);
        }
        finally {
            if (bean != null)
                bean.removeTempFiles();
        }
    }

    public static void main(String[] args) throws Exception {

        var boundary = new WekaAnalysisBoundary("/home/luca/Scrivania/ISW2/secondo_tentativo/file_output_falessi/bookkeeperProportion.csv",
                "/home/luca/Scrivania/ISW2/secondo_tentativo/file_output_falessi/bookkeeperWeka.csv",
                "");
        boundary.runAnalysis();

    }
}
