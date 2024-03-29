package logic.boundary;

import logic.abstracts.AbstractBoundary;
import logic.bean.ProportionBean;
import logic.controller.ProportionController;
import logic.exception.InvalidRangeException;
import logic.exception.NotAvaiableAlgorithm;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public class ProportionAnalisysBoundary extends AbstractBoundary {

    private String proportion;

    public ProportionAnalisysBoundary(String file, String dir, String proj, String proportionAlgo){
        this.outputFile = file;
        this.dirPath = dir;
        this.projectName = proj;
        this.proportion = proportionAlgo;
    }

    @Override
    public void runUseCase() throws GitAPIException, InvalidRangeException, IOException, NotAvaiableAlgorithm {
        var bean = new ProportionBean(this.outputFile,
                this.dirPath,
                this.projectName,
                this.proportion);
        var controller = new ProportionController();
        controller.run(bean);
    }

    public static void main(String[] args) throws GitAPIException, NotAvaiableAlgorithm, InvalidRangeException, IOException {
        var boundary = new ProportionAnalisysBoundary("/home/luca/Scrivania/openjpa3.csv",
                "/home/luca/Scrivania/ISW2/deliverables/deliverable2/openjpa",
                "openjpa",
                "Increment");
        boundary.runUseCase();
    }
}
