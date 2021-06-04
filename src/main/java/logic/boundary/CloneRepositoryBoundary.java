package logic.boundary;

import logic.bean.CloneRepositoryBean;
import logic.controller.CloneRepositoryController;
import logic.exception.InvalidInputException;
import org.eclipse.jgit.api.errors.GitAPIException;


public class CloneRepositoryBoundary {
    String url;
    String outputDirectory;

    public CloneRepositoryBoundary(String u, String file){
        this.url = u;
        this.outputDirectory = file;
    }

    public void cloneRepository() throws GitAPIException, InvalidInputException {
        var bean = new CloneRepositoryBean(this.url, this.outputDirectory);
        var controller = new CloneRepositoryController();
        controller.clone(bean);

    }
}
