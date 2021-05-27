package logic.controller;

import logic.bean.CloneRepositoryBean;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class CloneRepositoryController {

    public void clone(CloneRepositoryBean bean) throws GitAPIException {
        var outputDir = bean.getOutputDir();
        Git.cloneRepository()
                .setURI(bean.getUrl() + ".git")
                .setDirectory(outputDir)
                .call();
    }
}
