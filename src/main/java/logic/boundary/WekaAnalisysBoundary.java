package logic.boundary;

import logic.bean.WekaBean;
import logic.controller.WekaController;
import logic.exception.UnexistingFileException;

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

    public void runAnalisys() throws UnexistingFileException, Exception {
        var bean = new WekaBean(this.csvInputFile, this.csvOutputFile, this.arfOutputFile);
        var controller = new WekaController();
        controller.run(bean);
    }

    public static void main(String[] args) throws Exception, UnexistingFileException {

        var boundary = new WekaAnalisysBoundary("/home/luca/Scrivania/bookkeeperGUI.csv",
                "/home/luca/Scrivania/wekaOutput.csv",
                "/home/luca/Scrivania/bookkeeperARFF.arff");
        boundary.runAnalisys();

    }
}
