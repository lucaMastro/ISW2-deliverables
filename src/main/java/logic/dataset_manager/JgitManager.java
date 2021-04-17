package logic.dataset_manager;
import logic.config_manager.ConfigurationManager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/* This class may be develop as a singleton */
public class JgitManager {

    public Repository repository;
    private List<Ref> tagList;
    public ArrayList<RevCommit> commitList;

    public JgitManager() throws IOException{
        String path = ConfigurationManager.getConfigEntry("repositoryPath") + "/.git";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.repository = builder.setGitDir(new File(path)).readEnvironment().findGitDir().build();
        this.initializeReleaseList();
    }

    public static Date tagDate(Ref tag, RevWalk w) throws IOException {
        RevCommit t = w.parseCommit(tag.getObjectId());
        w.parseBody(t);
        return t.getAuthorIdent().getWhen();
    }

    private void initializeReleaseList() {
        try (RevWalk walk = new RevWalk(this.repository)) {
            this.tagList = new Git(repository).tagList().call();

            Collections.sort(this.tagList, (Ref o1, Ref o2) ->{
                    Date d1 = null;
                    Date d2 = null;
                    try {
                        d1 = tagDate(o1, walk);
                        d2 = tagDate(o2, walk);
                    } catch (IOException e) {
                        Logger logger = Logger.getLogger(JgitManager.class.getName());
                        logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
                    }
                    return d1.compareTo(d2);
                });
        }catch (GitAPIException e){
            Logger logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }


    public ArrayList<ObjectId> retrieveCommits(Integer releaseIndex){
        /* this.retrieveCommits(1) should return a list of all commits performed between release1 and release 2 */

        ArrayList<ObjectId> commits = new ArrayList<>();
        return commits;
    }


    public static void main(String[] args) throws IOException, GitAPIException {
        JgitManager manager = new JgitManager();
        Repository repository = manager.repository;
    }


}
