package logic.boundary;

import logic.abstracts.AbstractBoundary;
import logic.bean.BugginessAndProcessChartBean;
import logic.controller.ProcessControlChartController;
import logic.exception.InvalidRangeException;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public class ProcessControlChartBoundary extends AbstractBoundary {

    public ProcessControlChartBoundary(String file, String dir, String proj){
        this.outputFile = file;
        this.dirPath = dir;
        this.projectName = proj;
    }

    @Override
    public void runUseCase() throws GitAPIException, InvalidRangeException, IOException {
        var bean = new BugginessAndProcessChartBean(this.outputFile, this.dirPath, this.projectName);
        var controller = new ProcessControlChartController();
        controller.run(bean);
    }
}
