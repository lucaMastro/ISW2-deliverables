package logic.bean;

import java.io.File;

public class CloneRepositoryBean {
    private String url;
    private File outputDir;

    public CloneRepositoryBean(String url, File f){
        this.url = url;
        this.outputDir = f;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }
}
