package logic.boundary;

import logic.bean.CloneRepositoryBean;
import logic.controller.CloneRepositoryController;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class CloneRepositoryBoundary {
    String url;
    File outputDirectory;

    public CloneRepositoryBoundary(String u, String file){
        this.url = u;
        this.outputDirectory = new File(file);
    }

    public void cloneRepository() throws GitAPIException {
        var bean = new CloneRepositoryBean(this.url, this.outputDirectory);
        var controller = new CloneRepositoryController();
        controller.clone(bean);

    }
}
