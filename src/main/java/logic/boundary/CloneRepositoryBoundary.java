package logic.boundary;

import logic.bean.CloneRepositoryBean;
import logic.controller.CloneRepositoryController;
import logic.exception.InvalidInputException;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class CloneRepositoryBoundary {
    String url;
    File outputDirectory;

    public CloneRepositoryBoundary(String u, String file) throws InvalidInputException {
        if (u.isEmpty() || u == null
                || file == null || file.isEmpty())
            throw new InvalidInputException("Empty field detected: all field are mandatory.");
        this.url = u;
        this.outputDirectory = new File(file);
    }

    public void cloneRepository() throws GitAPIException {
        var bean = new CloneRepositoryBean(this.url, this.outputDirectory);
        var controller = new CloneRepositoryController();
        controller.clone(bean);

    }
}
