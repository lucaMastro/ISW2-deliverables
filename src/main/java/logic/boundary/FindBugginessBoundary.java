package logic.boundary;

import logic.abstracts.AbstractBoundary;
import logic.bean.ProportionBean;
import logic.controller.ProportionIncrementController;
import logic.exception.InvalidRangeException;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public class FindBugginessBoundary extends AbstractBoundary {

    public FindBugginessBoundary(String file, String dir, String proj){
        this.outputFile = file;
        this.dirPath = dir;
        this.projectName = proj;
    }

    @Override
    public void runUseCase() throws GitAPIException, InvalidRangeException, IOException {
        var bean = new ProportionBean(this.outputFile, this.dirPath, this.projectName);
        var controller = new ProportionIncrementController();
        controller.run(bean);
    }
}
