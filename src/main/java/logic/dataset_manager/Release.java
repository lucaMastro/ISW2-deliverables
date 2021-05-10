package logic.dataset_manager;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
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

    private ReleaseFile findFromName(String name){
        ReleaseFile releaseFile = null;
        for (ReleaseFile r : this.files){
            if (r.getPath().equals(name))
                releaseFile = r;
        }
        return releaseFile;
    }


    public void setEachFileLoc() throws IOException {
        for (ReleaseFile f : this.files)
            f.computeLoc(this.revCommit);
    }

    public void computeMetrics(Release previousRelease, List<BugTicket> fixedBugs) throws IOException {
        // computing loc
        this.setEachFileLoc();
        Commit older;
        Commit newer;
        Integer[] lines;

        Integer i;
        for (i = 0; i < this.commits.size() -1; i++){
            /*  excluding last element because i need the couple
             *   commit_i commit_i+1 to found differences    */
            if ( i == 0 && previousRelease != null){
                older = previousRelease;
                newer = this.commits.get(i);
            }
            else {
                older = this.commits.get(i);
                newer = this.commits.get(i + 1);
            }

            List<DiffEntry> differences = JgitManager.getInstance().listDifferencesBetweenTwoCommits(older.revCommit,
                    newer.revCommit);
            for (DiffEntry diff : differences) {
                ReleaseFile rf = this.findFromName(diff.getNewPath());
                if (rf != null) {
                    /*  it can be null if a file is added in a revision commit and deleted in another
                     *  revision commit before release commit. That's why this kind of file is not stored
                     *  in the list of files, which store the files tracked by git when the new release is out.  */
                    //nr
                    rf.updateNumberOfRevision();
                    //nauth
                    rf.addEditors(newer.revCommit.getAuthorIdent());
                    rf.updateNauth();
                    //locAdded
                    lines = JgitManager.getInstance().countLinesAddedAndDeleted(diff);
                    rf.updateLocAdded(lines[0]);
                    //churn
                    rf.updateChurn(lines[0] - lines[1]);
                    //nfix
                    if (newer.isFixCommit(fixedBugs))
                        rf.updateNfix();
                }
            }
        }
    }

}
