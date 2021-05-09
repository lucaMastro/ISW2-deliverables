package logic.dataset_manager;

import logic.exception.InvalidRangeException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.ArrayList;

public class Release extends Commit {

    /*  Incremental index for csv file  */
    Integer index;
    /*  Numeric versioning  */
    String versionName;

    /* List of all commits between Release(index) and Release(index + 1)    */
    ArrayList<Commit> commits;

    /*  List of all files in this release commit    */
    ArrayList<ReleaseFile> files;

    //--------------------------------------------------------------------------------

    public Release(Ref ref){
        super(ref);
        this.versionName = ref.getName();
        /* looking for files */
        this.files = new ArrayList<>();
        try (TreeWalk tw = new TreeWalk(JgitManager.getInstance().getRepository()) ){
            tw.setRecursive(Boolean.TRUE);
            tw.reset(this.revCommit.getTree().getId());
            while (tw.next()){
                String fileName = tw.getPathString();
                if (fileName.endsWith(".java"))
                    this.files.add( new ReleaseFile(fileName) );
            }
        } catch (IOException e) {
            Logger logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setEachFileLoc() throws IOException {
        for (ReleaseFile f : this.files)
            f.computeLoc(this.revCommit);
    }


    private ReleaseFile findFromName(String name){
        ReleaseFile releaseFile = null;
        for (ReleaseFile r : this.files){
            if (r.getPath().equals(name))
                releaseFile = r;
        }
        return releaseFile;
    }

    public void setEachFileNr() throws IOException {
        Integer i;
        for (i = 0; i < this.commits.size() - 1; i++){
            /*  excluding last element because i need the couple
            *   commit_i commit_i+1 to found differences    */
            RevCommit older = this.commits.get(i).revCommit;
            RevCommit newer = this.commits.get(i + 1).revCommit;
            List<DiffEntry> differences = JgitManager.getInstance().listDifferencesBetweenTwoCommits(older, newer);
            for (DiffEntry diffEntry : differences){
                String name = diffEntry.getNewPath();

                ReleaseFile r = this.findFromName(name);
                if (r != null)
                    /*  it can be null if a file is added in a revision commit and deleted in another
                    *   revision commit before release commit. That's why this kind of file is not stored
                    *   in the list of files    */
                    r.updateNumberOfRevision();
                }
        }
    }

    public void setEachFileNfix(List<BugTicket> fixedBugs) throws InvalidRangeException, IOException, GitAPIException {
        for (Commit c : this.commits) {
            for (BugTicket bug : fixedBugs) {

                if (bug.relativeCommits.contains(c)) {
                    // c is a commit that fixes a bug. Find files changed
                    Commit previous = DatasetConstructor.getInstance().findPreviously(c);
                    List<DiffEntry> diffs = JgitManager.getInstance().
                            listDifferencesBetweenTwoCommits(previous.revCommit, c.revCommit);

                    for (DiffEntry diffEntry : diffs) {
                        String name = diffEntry.getNewPath();
                        ReleaseFile r = this.findFromName(name);
                        if (r != null)
                            /*  it can be null if a file is added in a revision commit and deleted in another
                             *   revision commit before release commit. That's why this kind of file is not stored
                             *   in the list of files    */
                            r.updateNfix();
                    }
                }
            }
        }
    }


    public void setEachFileNauth() throws IOException {
        Integer i;
        for (i = 0; i < this.commits.size() - 1; i++) {
            /*  excluding last element because i need the couple
             *   commit_i commit_i+1 to found differences    */
            RevCommit older = this.commits.get(i).revCommit;
            RevCommit newer = this.commits.get(i + 1).revCommit;
            List<DiffEntry> differences = JgitManager.getInstance().listDifferencesBetweenTwoCommits(older, newer);
            for (DiffEntry diff : differences){
                ReleaseFile f = this.findFromName(diff.getNewPath());
                if (f != null) {
                    f.addEditors(newer.getAuthorIdent());
                    f.updateNauth();
                }
            }
        }
    }

    public void setEachFileLocAdded() throws IOException {
        Integer i;
        Integer linesAdded;
        for (i = 0; i < this.commits.size() - 1; i++) {
            /*  excluding last element because i need the couple
             *   commit_i commit_i+1 to found differences    */
            RevCommit older = this.commits.get(i).revCommit;
            RevCommit newer = this.commits.get(i + 1).revCommit;
            List<DiffEntry> differences = JgitManager.getInstance().listDifferencesBetweenTwoCommits(older, newer);
            for (DiffEntry diff : differences) {
                ReleaseFile f = this.findFromName(diff.getNewPath());
                if (f != null) {
                    linesAdded = JgitManager.getInstance().countLinesAddedAndDeleted(diff)[0];
                    f.updateLocAdded(linesAdded);
                }
            }
        }
    }

    public void setEachFileChurn() throws IOException {
        /*  should find a way to exclude comment lines */
        Integer i;
        Integer linesAdded;
        Integer linesDeleted;
        for (i = 0; i < this.commits.size() - 1; i++) {
            /*  excluding last element because i need the couple
             *   commit_i commit_i+1 to found differences    */
            RevCommit older = this.commits.get(i).revCommit;
            RevCommit newer = this.commits.get(i + 1).revCommit;
            List<DiffEntry> differences = JgitManager.getInstance().listDifferencesBetweenTwoCommits(older, newer);
            for (DiffEntry diff : differences) {
                ReleaseFile f = this.findFromName(diff.getNewPath());
                if (f != null) {
                    Integer[] lines = JgitManager.getInstance().countLinesAddedAndDeleted(diff);
                    linesAdded = lines[0];
                    linesDeleted = lines[1];
                    f.updateChurn(linesAdded - linesDeleted);
                }
            }
        }
    }
}
