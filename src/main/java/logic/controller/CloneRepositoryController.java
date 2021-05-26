package logic.controller;

import logic.bean.CloneRepositoryBean;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class CloneRepositoryController {

    public void clone(CloneRepositoryBean bean) throws GitAPIException {
        var outputDir = bean.getOutputDir();
        Git.cloneRepository()
                .setURI(bean.getUrl() + ".git")
                .setDirectory(outputDir)
                .call();
    }

    public static void main(String[] args) throws GitAPIException {
        String url = "https://github.com/lucaMastro/deliverable2.git";
        File dir = new File("/home/luca/Scrivania/prova2");
        Git.cloneRepository().setURI(url).setDirectory(dir).call();
    }
}
