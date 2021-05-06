package logic.dataset_manager;
import logic.config_manager.ConfigurationManager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JgitManager {

    private static JgitManager instance = null;
    private Repository repository;

    public static JgitManager getInstance() throws IOException {
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



    public List<DiffEntry> listDifferencesBetweenTwoCommits(RevCommit older, RevCommit newer) throws IOException {

        ObjectId olderId = older.getTree().getId();
        ObjectId newerId = newer.getTree().getId();
        List<DiffEntry> diffs = null;

        try (ObjectReader reader = repository.newObjectReader();
             Git git = new Git(repository)) {

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, olderId);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, newerId);

            diffs = git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();

            diffs.removeIf((DiffEntry diff) -> !diff.getNewPath().endsWith(".java"));

        } catch (GitAPIException e) {
            Logger logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
        return diffs;
    }

    public Integer[] countLinesAddedAndDeleted(DiffEntry diffEntry) throws IOException {

        Integer linesAdded;
        Integer linesDeleted;
        linesAdded = 0;
        linesDeleted = 0;

        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);

        for (Edit edit : df.toFileHeader(diffEntry).toEditList()) {
            linesDeleted += edit.getEndA() - edit.getBeginA();
            linesAdded += edit.getEndB() - edit.getBeginB();
        }
        return new Integer[]{linesAdded, linesDeleted};
    }

    public Integer getLocFileInGivenRelease(String fileName, RevCommit release) throws IOException {
        /*  This function may not count commits line */

        String fileContent;
        try (TreeWalk treeWalk = TreeWalk.forPath(this.getRepository(), fileName,
                release.getTree())) {
            ObjectId blobId = treeWalk.getObjectId(0);
            try (ObjectReader objectReader = this.getRepository().newObjectReader()) {
                ObjectLoader objectLoader = objectReader.open(blobId);
                byte[] bytes = objectLoader.getBytes();
                fileContent = new String(bytes, StandardCharsets.UTF_8);
                String[] lines = fileContent.split("\n");
                return lines.length;
            }
        }

    }

}
