package logic.dataset_manager;

import logic.config_manager.ConfigurationManager;
import logic.exception.InvalidRangeException;
import logic.jira_informations.JiraBeanInformations;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatasetConstructor {

    private ArrayList<Commit> commits;
    private ArrayList<Release> releases;
    private ArrayList<BugTicket> fixedBugs;
    private Map<String, Date> nameToAdditionDate;

    public DatasetConstructor() throws GitAPIException, IOException, InvalidRangeException {
        this.initializeCommitList();
        this.removeRevertCommits();
        this.initializeReleaseList();
        this.initializeBugsList();
        this.nameToAdditionDate = new TreeMap<>();
    }

    private void removeRevertCommits() {
        ArrayList<Commit> commitsToRemove = new ArrayList<>();
        for (Commit c : this.commits){
            if (c.message.contains("This reverts commit")){
                // finding the id of commit reverted:
                String[] lines = c.message.split("\n");
                for (String line : lines){
                    if (line.contains("This reverts commit")){
                        String id = line.split(" ")[3];
                        id = id.substring(0, 40);
                        commitsToRemove.add(this.findCommitFromName(id));
                        // removing the commit which reverses another one
                        commitsToRemove.add(c);
                    }
                }
            }
        }

        for (Commit c : commitsToRemove){
            if (c != null) //may be null if findCommitFromName(id) returns null
                this.commits.remove(c);
        }
    }

    public Commit findCommitFromName(String id) {
        Commit commit = null;
        for (Commit c : this.commits) {
            if (c.revCommit.getName().equals(id)) {
                commit = c;
                break;
            }
        }
        return commit;
    }

    private void initializeReleaseList() throws GitAPIException, IOException, InvalidRangeException {
        Repository repository = JgitManager.getInstance().getRepository();
        List<Ref> tagList = new Git(repository).tagList().call();
        this.releases = new ArrayList<>();
        Integer i;
        for (i = 0; i < tagList.size(); i++){
            Release cur = new Release(tagList.get(i));
            this.releases.add(cur);
        }
        Collections.sort(this.releases, (Commit o1, Commit o2) ->{
            Date d1 = o1.date;
            Date d2 = o2.date;
            return d1.compareTo(d2);
        });
        for (i = 0; i < tagList.size(); i++) {
            Release cur = this.releases.get(i);
            cur.setIndex(i + 1);
            cur.commits = (ArrayList<Commit>) this.retrieveCommitsBeetwenReleases(i + 1);
        }
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

    public Commit findPreviously(Commit c){
        Integer i;
        Commit toReturn = null;
        for (i = 0; i < this.commits.size(); i++){
           if (c == this.commits.get(i)) {
               toReturn = this.commits.get(i - 1);
               break;
           }
        }
        return toReturn;
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
            bug.setRelativeCommits(this.findCommitsFromTicketId(info.getKey()));
            this.fixedBugs.add(bug);
        }
    }

    public static void main(String[] args) throws IOException, InvalidRangeException, GitAPIException {
        DatasetConstructor ds = new DatasetConstructor();


        Release prev = null;
        for (Release r : ds.releases) {
            ds.nameToAdditionDate = r.computeMetrics(prev, ds.fixedBugs, ds.nameToAdditionDate);
            prev = r;
        }
    }

}