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
        Integer count = 0;
        String fileContent;
        try (TreeWalk treeWalk = TreeWalk.forPath(this.getRepository(), fileName,
                release.getTree())) {
            ObjectId blobId = treeWalk.getObjectId(0);
            try (ObjectReader objectReader = this.getRepository().newObjectReader()) {
                ObjectLoader objectLoader = objectReader.open(blobId);
                byte[] bytes = objectLoader.getBytes();
                fileContent = new String(bytes, StandardCharsets.UTF_8);
                String[] lines = fileContent.split("\n");

                /* The following code exclude commits line from count   */
                Integer i;
                Boolean openedComment = Boolean.FALSE;
                for (i = 0; i < lines.length; i++) {

                    String line = lines[i];
                    // skipping single line comments
                    if (!line.trim().startsWith("//") || !line.trim().isEmpty()) {
                        //continue;
                        //else{
                        Boolean[] returned = this.isLineValid(line, openedComment);
                        if (returned[0])
                            count++;
                        openedComment = returned[1];
                    }
                }
            }
        }
        return count;
    }

    private Boolean[] isLineValid(String line, Boolean previouslyOpened){
        /*  looking for comments delimiter in the line  */
        //  possible cases:
        //      /* any comment */ String s = "this is a valid line";
        //      startCommit = true, closeCommit = true, line.endsWith("*/") false, line.startsWith("/*") true
        //      /* this is not a valid line */
        //      /* this is not a valid line, but the comment is not closed here
        //      /*      <-- line that only has a starting comment block
        //      String s = "this is a valid line"; /* any comment here */
        //      String s = "this is a valid line"; /* any comment here, but block is not closed
        //      */ String s = "this is a valid line";

        char[] chars = line.trim().toCharArray(); //no initial spaces
        Integer i;
        Integer len = chars.length;
        // looking for /* not in quotes
        Boolean quoteFound = Boolean.FALSE;

        Boolean nowOpened = previouslyOpened;
        char current;
        char next;

        /*  if line's leng is == 1, the following loop won't be executed. Just having a check if this is a valid line*/
        Boolean isValidLine = (len == 1 && !nowOpened) ;

        for (i = 0; i < len - 1; i++){
            current = chars[i];
            next = chars[i + 1];
            if (current == '"' || current == '\'')
                quoteFound = !quoteFound;

            if (current == '/' && next == '*' && !quoteFound) {
                // found start delimiter
                nowOpened = Boolean.TRUE;
                i = i + 1;
            }

            else if (current == '*' && next == '/' && !quoteFound) {
                //found end delimiter
                nowOpened = Boolean.FALSE;
                i = i + 1;
            }
            else{
                // chars[i] is a general char. if not commented opened, the line is valid!!
                if (!nowOpened)
                    isValidLine = Boolean.TRUE;
            }
        }
        return new Boolean[]{isValidLine, nowOpened};
    }
}
