package logic.dataset_manager;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

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


    private Commit[] findOlderAndNewerCommits(Release previousRelease, Integer index){
        Commit older;
        Commit newer;

        older = index < 0 ? previousRelease : this.commits.get(index);
        newer = index < this.commits.size() - 1 ? this.commits.get(index + 1) : this;

        if (older == null)
            return null;
        else
            return new Commit[]{older, newer};
    }


    public Map<String, Date> computeMetrics(Release previousRelease, List<BugTicket> fixedBugs,
                               Map<String, Date> nameToAdditionDate) throws IOException {
        // computing loc
        this.setEachFileLoc();
        Commit older;
        Commit newer;
        Integer[] lines;
        Integer i;
        Date additionDate;
        for (i = -1; i < this.commits.size(); i++){
            /*  index starts from -1 because this.commits.get(i) is assigned to the olderCommit.
            *   Starting from 0, i only can get the first commit of each new release as an older commit and i will
            *   lose the changes between previousReleaseCommit and the first commit of the newer release.
            *   When index is -1, i can understand that the older commit i should use is the previous release */

            Commit[] olderAndNewer = this.findOlderAndNewerCommits(previousRelease, i);
            if (olderAndNewer == null)
                continue;
            older = olderAndNewer[0];
            newer = olderAndNewer[1];

            List<DiffEntry> differences = JgitManager.getInstance().listDifferencesBetweenTwoCommits(older.revCommit,
                    newer.revCommit);
            for (DiffEntry diff : differences) {

                ReleaseFile rf = this.findFromName(diff.getNewPath());

                if (diff.getChangeType().equals(DiffEntry.ChangeType.ADD) &&
                        !nameToAdditionDate.containsKey(diff.getNewPath())) {
                    //newer.date is the addition Date of this file
                    nameToAdditionDate.put(diff.getNewPath(), newer.date);
                    additionDate = newer.date;
                }
                else
                    additionDate = nameToAdditionDate.get(diff.getNewPath());

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
                    if (newer.isFixCommit(fixedBugs).equals(Boolean.TRUE))
                        rf.updateNfix();
                    //age
                    rf.setAdditionDate(additionDate);
                    rf.computeAge(this.date);

                }
            }
        }
        return nameToAdditionDate;
    }

}
