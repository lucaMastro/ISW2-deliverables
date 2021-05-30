package logic.boundary;

import logic.abstracts.AbstractBoundary;
import logic.bean.ProcessChartBean;
import logic.controller.ProcessControlChartController;
import logic.exception.InvalidRangeException;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public class ProcessControlChartBoundary extends AbstractBoundary {

    private Integer threshold;

    public ProcessControlChartBoundary(String file, String dir, String proj, Integer thr){
        this.outputFile = file;
        this.dirPath = dir;
        this.projectName = proj;
        this.threshold = thr;
    }

    @Override
    public void runUseCase() throws GitAPIException, InvalidRangeException, IOException {
        var bean = new ProcessChartBean(this.outputFile, this.dirPath, this.projectName, this.threshold);
        var controller = new ProcessControlChartController();
        controller.run(bean);
    }
}
