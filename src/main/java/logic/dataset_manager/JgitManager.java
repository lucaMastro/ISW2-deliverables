package logic.dataset_manager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JgitManager {

    private Repository repository;

    public JgitManager(String path) throws IOException{
        path += "/.git";
        var builder = new FileRepositoryBuilder();
        this.repository = builder.setGitDir(new File(path)).readEnvironment().findGitDir().build();
    }

    public Repository getRepository() {
        return repository;
    }



    public List<DiffEntry> listDifferencesBetweenTwoCommits(RevCommit older, RevCommit newer) throws IOException {

        ObjectId olderId = older.getTree().getId();
        ObjectId newerId = newer.getTree().getId();
        List<DiffEntry> diffs = null;

        try (var reader = repository.newObjectReader();
             var git = new Git(repository)) {

            var oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, olderId);
            var newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, newerId);

            diffs = git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();

            diffs.removeIf((DiffEntry diff) -> !diff.getNewPath().endsWith(".java"));

        } catch (GitAPIException e) {
            var logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
        return diffs;
    }

    public Integer[] countLinesAddedAndDeleted(DiffEntry diffEntry) throws IOException {

        Integer linesAdded;
        Integer linesDeleted;
        linesAdded = 0;
        linesDeleted = 0;

        var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);

        for (Edit edit : df.toFileHeader(diffEntry).toEditList()) {
            linesDeleted += edit.getEndA() - edit.getBeginA();
            linesAdded += edit.getEndB() - edit.getBeginB();
        }
        return new Integer[]{linesAdded, linesDeleted};
    }


    /* ************************************************************************************************
        LOC metric computing method and usefull method to prevent comment's inclusion in count
    ************************************************************************************************** */

    public Integer getLocFileInGivenRelease(String fileName, RevCommit release) throws IOException {

        /*  This function may not count commits line */
        Integer count = 0;
        String fileContent;
        try (var treeWalk = TreeWalk.forPath(this.getRepository(), fileName,
                release.getTree())) {
            var blobId = treeWalk.getObjectId(0);
            try (var objectReader = this.getRepository().newObjectReader()) {
                var objectLoader = objectReader.open(blobId);
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
                        Boolean[] returned = this.isLineValid(line, openedComment);
                        if (Boolean.TRUE.equals(returned[0]))
                            count++;
                        openedComment = returned[1];
                    }
                }
            }
        }
        return count;
    }

    private Boolean updateQuote(char current, Boolean quotesStillFound){
        if (current == '"' || current == '\'')
            return !quotesStillFound;
        else
            return quotesStillFound;
    }


    private Boolean[] isLineValid(String line, Boolean previouslyOpened){
        /*  looking for comments delimiter in the line  */
        char[] chars = line.trim().toCharArray(); //no initial spaces
        Integer i;
        Integer len = chars.length;

        // looking also for /* not in quotes
        Boolean quoteFound = Boolean.FALSE;

        Boolean nowOpened = previouslyOpened;
        char current;
        char next;

        /*  if line's length is == 1, the following loop won't be executed. Just having a check if this is a valid line*/
        Boolean isValidLine = (len == 1 && Boolean.FALSE.equals(nowOpened)) ;

        for (i = 0; i < len - 1; i++){
            current = chars[i];
            next = chars[i + 1];
            quoteFound = this.updateQuote(current, quoteFound);

            if (current == '/' && next == '*' && Boolean.FALSE.equals(quoteFound)) {
                // found start delimiter
                nowOpened = Boolean.TRUE;
                i = i + 1;
            }

            else if (current == '*' && next == '/' && Boolean.FALSE.equals(quoteFound)) {
                //found end delimiter
                nowOpened = Boolean.FALSE;
                i = i + 1;
            }
            /*  this is the case of a particular line:
             *   line = "/*comment* / //other comment   */
            else if (current == '/' && next == '/')
                break;
            else{
                /*  chars[i] is a general char. if not commented opened, the line is valid!!
                *   current != ' ' is because of possibility of line such as the following:
                *   line = "/*comment* / //other comment
                *  */
                if (Boolean.FALSE.equals(nowOpened) && current != ' ')
                    isValidLine = Boolean.TRUE;
            }
        }
        return new Boolean[]{isValidLine, nowOpened};
    }

    public List<String> filesInRelease(RevCommit revCommit){
        var files = new ArrayList<String>();
        try (var tw = new TreeWalk(this.getRepository())){
            tw.setRecursive(Boolean.TRUE);
            tw.reset(revCommit.getTree().getId());
            while (tw.next()){
                var fileName = tw.getPathString();
                if (fileName.endsWith(".java"))
                    files.add(fileName);
            }
        } catch (IOException e) {
            var logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
        return files;
    }
}
