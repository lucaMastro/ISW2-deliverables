package logic.config_manager;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConfigurationManager {
    /*  Class with the aim to parse the config file. That's why it has an only static method
    *   and no public constructors. */

    private static ConfigurationManager instance = null;
    private String projectName;
    private String repositoryPath;
    private String outputFile;

    private ConfigurationManager(){}

    public static String getConfigEntry(String key){
        String c = "";
        try (InputStream inputStream = new FileInputStream("src/main/resources/config.properties")){
            Properties p = new Properties();
            p.load(inputStream);
            c = p.getProperty(key);
        } catch (IOException e) {
            Logger logger = Logger.getLogger(ConfigurationManager.class.getName());
            logger.log(Level.OFF, e.toString());
        }
        return c;
    }
}
