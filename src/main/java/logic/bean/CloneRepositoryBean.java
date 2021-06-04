package logic.bean;

import logic.exception.InvalidInputException;
import java.io.File;

public class CloneRepositoryBean {
    private String url;
    private File outputDir;

    public CloneRepositoryBean(String url, String f) throws InvalidInputException {
        if (url == null || url.isEmpty() ||
                f == null || f.isEmpty())
            throw new InvalidInputException("Empty field detected: all field are mandatory.");
        this.url = url;
        this.outputDir = new File(f);
    }

    public String getUrl() {
        return url;
    }

    public File getOutputDir() {
        return outputDir;
    }
}
