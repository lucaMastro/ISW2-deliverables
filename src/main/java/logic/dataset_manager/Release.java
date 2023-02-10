package logic.dataset_manager;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;

import java.util.*;
import java.io.IOException;

public class Release extends Commit {

    /*  Incremental index for csv file  */
    Integer index;
    /*  Numeric versioning  */
    String versionName;

    /* List of all commits between Release(index) and Release(index + 1)    */
    ArrayList<Commit> commits;

    //--------------------------------------------------------------------------------

    public Release(Ref ref, JgitManager jgitManager){
        super(ref, jgitManager);
        this.versionName = ref.getName();
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    private ReleaseFile findFromName(String name, List<ReleaseFile> files){
        ReleaseFile releaseFile = null;
        for (ReleaseFile r : files){
            if (r.isThisFile(name).equals(Boolean.TRUE)){
                releaseFile = r;
                break;
            }
        }
        return releaseFile;
    }


    public void setEachFileLoc(ReleaseFileManager files) throws IOException {
        for (ReleaseFile f : files.getFileList())
            f.computeLoc(this.revCommit, this.getIndex());
    }


    private List<Commit> findOlderAndNewerCommits(Release previousRelease, Integer index){
        Commit older;
        Commit newer;
        List<Commit> olderAndNewer = new ArrayList<>();

        older = index < 0 ? previousRelease : this.commits.get(index);
        newer = index < this.commits.size() - 1 ? this.commits.get(index + 1) : this;

        // older is null only for the first absolute commit
        if (older != null){
            olderAndNewer.add(older);
            olderAndNewer.add(newer);
        }
        return  olderAndNewer;
    }


    private void updateFileMetrics(ReleaseFile rf, Commit newer, DiffEntry diff,
                                   List<BugTicket> fixedBugs) throws IOException {
        if (rf != null) {
            /*  it can be null if a file is added in a revision commit and deleted in another
             *  revision commit before release commit:
             *  R.i     add     del     R.2  |
             *   -   -   -   -   -   -   -   |
             *  That's why this kind of file is not stored
             *  in the list of files, which store the files tracked by git when the new release is out.  */
            var i = this.index;

            //nr
            rf.updateNumberOfRevision(i);
            //nauth
            rf.addEditors(i, newer.revCommit.getAuthorIdent());
            rf.updateNauth(i);
            //locAdded
            Integer[] lines = this.jgitManager.countLinesAddedAndDeleted(diff);
            rf.updateLocAdded(i, lines[0]);
            //churn
            rf.updateChurn(i, lines[0] - lines[1]);
            //nfix
            BugTicket bug = newer.isFixCommit(fixedBugs);
            if (bug != null) {
                rf.updateNfix(i);
                bug.addFileTouched(diff);
            }
            //age
            rf.computeAge(i, this.date);
        }
    }

    /* this method works in this way:
    * 1) retrieve all commits between for each couple of release i, i+1
    * 2) retrieve all differences between commits j, j+1 obtained in the (1)
    *       -note: if j+1 == 0 => j is the commit release
    * 3) for each difference update metrics of the newest file
    *
    *          R.1               R.2   | <-- releases
    *  -  -  -  -  -  -  -  -  -  -    | <-- commits
    *
    * in the updateMetrics method, it's checked if the given commit refers a bugTicket of "fix" type. */
    public ReleaseFileManager computeMetrics(Release previousRelease, List<BugTicket> fixedBugs,
                                             ReleaseFileManager files) throws IOException {
        Commit older;
        Commit newer;
        Integer i;
        String fileNameToSearch;


        // computing loc
        this.setEachFileLoc(files);
        for (i = -1; i < this.commits.size(); i++) {
            /*  index starts from -1 because this.commits.get(i) is assigned to the olderCommit.
             *   Starting from 0, i would miss differences between oldRelease commit and the first commit of the
             *   newer release */

            List<Commit> olderAndNewer = this.findOlderAndNewerCommits(previousRelease, i);
            if (olderAndNewer.isEmpty())
                continue;

            older = olderAndNewer.get(0);
            newer = olderAndNewer.get(1);

            List<DiffEntry> differences = this.jgitManager.listDifferencesBetweenTwoCommits(older.revCommit,
                    newer.revCommit);
            for (DiffEntry diff : differences) {
                ReleaseFile rf;
                // get the path file. If the file is added, get the newer path because the older doesn't exist
                fileNameToSearch = diff.getChangeType().equals(DiffEntry.ChangeType.ADD) ?
                        diff.getNewPath() : diff.getOldPath();

                rf = files.findFileFromName(fileNameToSearch);
                if (rf != null) {
                    if (diff.getChangeType().equals(DiffEntry.ChangeType.ADD))
                        rf.setAdditionDate(newer.date);
                    this.updateFileMetrics(rf, newer, diff, fixedBugs);
                }
            }
        }
        return files;
    }


    public void setAllFileBuggines(List<String> fileNames, List<ReleaseFile> files, Integer index){
        for (String s : fileNames){
            ReleaseFile file = this.findFromName(s, files);
            if (file != null)
                file.updateBugginess(index);
        }
    }

    public Integer getIndex() {
        return index;
    }

}
