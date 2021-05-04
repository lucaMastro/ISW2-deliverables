package logic.dataset_manager;
import logic.config_manager.ConfigurationManager;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import java.io.File;
import java.io.IOException;

public class JgitManager {

    private static JgitManager instance = null;
    private Repository repository;

    public static JgitManager getInstance() throws IOException{
        if (JgitManager.instance == null){
            JgitManager.instance = new JgitManager();
        }
        return JgitManager.instance;
    }

    public JgitManager() throws IOException{
        String path = ConfigurationManager.getConfigEntry("repositoryPath") + ".git";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.repository = builder.setGitDir(new File(path)).readEnvironment().findGitDir().build();
    }

    public Repository getRepository() {
        return repository;
    }

}
