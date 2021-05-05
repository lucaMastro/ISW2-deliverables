package logic.dataset_manager;

import logic.config_manager.ConfigurationManager;
import logic.exception.InvalidRangeException;
import logic.jira_informations.JiraBeanInformations;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatasetConstructor {

    private ArrayList<Commit> commits;
    private ArrayList<Release> releases;
    private ArrayList<BugTicket> fixedBugs;

    public DatasetConstructor() throws GitAPIException, IOException, InvalidRangeException {
        this.initializeCommitList();
        this.initializeReleaseList();
        Integer i;
        for (i = 0; i < this.releases.size(); i++)
            this.releases.get(i).setIndex(i + 1);

        this.initializeBugsList();
    }

    private void initializeReleaseList() throws GitAPIException, IOException, InvalidRangeException {
        Repository repository = JgitManager.getInstance().getRepository();
        List<Ref> tagList = new Git(repository).tagList().call();
        this.releases = new ArrayList<>();
        Integer i;
        for (i = 0; i < tagList.size(); i++){
            Release cur = new Release(tagList.get(i));
            this.releases.add(cur);
            cur.commits = (ArrayList<Commit>) this.retrieveCommitsBeetwenReleases(i + 1);
        }
        Collections.sort(this.releases, (Commit o1, Commit o2) ->{
            Date d1 = o1.date;
            Date d2 = o2.date;
            return d1.compareTo(d2);
        });
    }


    private void initializeCommitList() {
        this.commits = new ArrayList<>();
        try (RevWalk walk = new RevWalk(JgitManager.getInstance().getRepository())) {
            walk.sort(RevSort.REVERSE);
            Iterable<RevCommit> l = new Git(JgitManager.getInstance().getRepository()).log().call();
            for (RevCommit r : l){
                Commit curr = new Commit(r);
                this.commits.add(curr);
            }
            Collections.sort(this.commits, (Commit o1, Commit o2) ->{
                Date  d1 = o1.date;
                Date d2 = o2.date;
                return d1.compareTo(d2);
            });
        } catch (GitAPIException | IOException e) {
            Logger logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

    private Release getTag(Integer index){
        return this.releases.get(index);
    }

    private Commit getCommit(Integer index){
        return this.commits.get(index);
    }

    public List<Commit> retrieveCommitsBeetwenReleases(Integer endIndexRelease)
            throws InvalidRangeException {

        /* this.retrieveCommits(2) should return a list of all commits performed between release1 and release 2
         * That's why, if startIndexRelease is less than 1 an exception is thrown */

        if (endIndexRelease < 1)
            throw new InvalidRangeException("endIndexRelease should be greater than 0");

        ArrayList<Commit> comList = new ArrayList<>();
        // The release 1 is stored in ArrayList.get(0)
        endIndexRelease--;
        Integer startIndexRelease = endIndexRelease - 1;
        Date startDate;
        if (startIndexRelease >= 0)
            startDate = this.getTag(startIndexRelease).date;
        else // looking for commits of first release
            startDate = this.getCommit(0).date;
        Date endDate = this.getTag(endIndexRelease).date;

        for (Commit currCommit : this.commits){
            Date currDate = currCommit.date;
            // checking if currDate is in a correct interval:
            if (startDate.compareTo(currDate) <= 0
                    && currDate.compareTo(endDate) < 0)
                comList.add(currCommit);
        }
        return comList;
    }


    public List<Commit> findCommitsFromTicketId(String ticketId){
        /*  This method returns the Commit list which are relative to a given TicketId  */
        ArrayList<Commit> relativeCommits = new ArrayList<>();
        for (Commit c : this.commits)
            if (c.message.contains(ticketId))
                relativeCommits.add(c);
        return relativeCommits;
    }


    public Commit findLastCommitsFromTicketId(String ticketId){
        /*  This method returns the Commit which is the last (in time)
        *   relative to a given TicketId  */
        Commit last = null;
        for (Commit c : this.findCommitsFromTicketId(ticketId)){
            if (last == null || last.date.compareTo(c.date) < 0)
                last = c;
        }
        return last;
    }

    public Release findReleaseFromName(String s){
        /*  this method returns the release's Commit which has the version name in its message */
        Release ret = null;
        for (Release r : this.releases){
            if (r.versionName.contains(s)){
                ret = r;
                break;
            }
        }
        return ret;
    }

    public Commit findFixedVersion(List<String> fixedVersionNames) {
        /*  Jira ticket may return a list of fixedVersions. Assuming the truly fixed version is the last (in time)
         *  of this list, this method find that release's commit.
         *  Note that Jira returns a list of String, that are the numerical versioning of release.  */
        Commit last = null;
        for (String name : fixedVersionNames){
            Commit c = this.findReleaseFromName(name);
            if ( c == null)
                /*  this may happen when in jira ticket is listed a release which has not been tagged   */
                continue;

            if (last == null || last.date.compareTo(c.date) < 0)
                last = c;
        }
        return last;
    }

    private void initializeBugsList() throws IOException {

        RetrieveInformations retrieveInformations = new RetrieveInformations(
                ConfigurationManager.getConfigEntry("projectName"));
        ArrayList<JiraBeanInformations> informations = (ArrayList<JiraBeanInformations>) retrieveInformations.getInformations();

        this.fixedBugs = new ArrayList<>();

        for (JiraBeanInformations info : informations) {

            info.setTrulyFixedVersion(this.findFixedVersion(info.getFixedVersions()));
            BugTicket bug = new BugTicket(info);
            this.fixedBugs.add(bug);
        }
    }




    public static void main(String[] args) throws IOException, InvalidRangeException, GitAPIException {
        DatasetConstructor manager = new DatasetConstructor();
        Commit c1 = manager.releases.get(1).commits.get(0);
        Commit c2 = manager.releases.get(1).commits.get(1);

        List<DiffEntry> diffs = JgitManager.getInstance().listDifferencesBetweenTwoCommits(c1.revCommit, c2.revCommit);
        for (DiffEntry entry : diffs) {
            System.out.println("old: " + entry.getOldPath() +
                    ", new: " + entry.getNewPath() +
                    ", entry: " + entry);
        }

        for (DiffEntry entry : diffs) {
            Integer[] lines = JgitManager.getInstance().countLinesAddedAndDeleted(entry);
            System.out.println("added: "+ lines[0]+", deleted: "+ lines[1]);
        }


        /*Repository repository = JgitManager.getInstance().getRepository();

        ObjectId id1 = c1.revCommit.getTree().getId();
        ObjectId id2 = c2.revCommit.getTree().getId();

        System.out.println("Printing diff between tree: " + id1 + " and " + id2);
        List<DiffEntry> diffs;
        // prepare the two iterators to compute the diff between
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, id1);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, id2);

            // finally get the list of changed files
            try (Git git = new Git(repository)) {
                diffs= git.diff()
                        .setNewTree(newTreeIter)
                        .setOldTree(oldTreeIter)
                        .call();
                for (DiffEntry entry : diffs) {
                    System.out.println("old: " + entry.getOldPath() +
                            ", new: " + entry.getNewPath() +
                            ", entry: " + entry);
                }
            }
        }

        int linesDeleted = 0;
        int linesAdded = 0;
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        for (DiffEntry diff : diffs) {
            for (Edit edit : df.toFileHeader(diff).toEditList()) {
                linesDeleted += edit.getEndA() - edit.getBeginA();
                linesAdded += edit.getEndB() - edit.getBeginB();
            }
        }
        System.out.println("added: "+ linesAdded+", deleted: "+ linesDeleted);*/
    }

}