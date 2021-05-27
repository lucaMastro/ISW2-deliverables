package logic.dataset_manager;

import logic.bean.BugginessAndProcessChartBean;
import logic.exception.InvalidRangeException;
import logic.bean.JiraBeanInformations;
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

public class Dataset {

    private ArrayList<Commit> commits;
    private ArrayList<Release> releases;
    private ArrayList<BugTicket> fixedBugs;
    private Map<String, Date> nameToAdditionDate;

    //***********************************************************************************************************
    // Constructor and relative methods

    public Dataset(BugginessAndProcessChartBean bean) throws GitAPIException, IOException, InvalidRangeException {
        JgitManager jgitManager = new JgitManager(bean.getDirectory().getPath());

        this.initializeCommitList(jgitManager);
        this.removeRevertCommits();

        this.initializeReleaseList(jgitManager);

        this.initializeBugsList(bean.getProject());

        this.reduceDataset();
        this.nameToAdditionDate = new TreeMap<>();
    }

    private void reduceDataset() {
        /*  removing releases   */
        Integer half = this.releases.size() / 2;
        this.releases.removeIf(r -> r.index > half);
        Date lastDate = this.releases.get(this.releases.size() - 1).date;

        /*  removing commits done after the last release    */
        this.commits.removeIf(c -> lastDate.before(c.date));

        /*  removing defects whose fixedVersionDate is before than opening date and the fixedVersionIndex
        *   is bigger than the older release we want to track   */
        this.fixedBugs.removeIf(b -> lastDate.before(b.fixedVersion.date) ||
                b.fixedVersion.date.before(b.openingDate));


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
                        id = id.substring(0, id.length() - 1);
                        commitsToRemove.add(this.findCommitFromId(id));
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


    private void initializeReleaseList(JgitManager manager)
            throws GitAPIException, InvalidRangeException {
        List<Ref> tagList = new Git(manager.getRepository()).tagList().call();
        this.releases = new ArrayList<>();
        Integer i;

        for (i = 0; i < tagList.size(); i++){
            Release cur = new Release(tagList.get(i), manager);
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

    private List<Commit> retrieveCommitsBeetwenReleases(Integer endIndexRelease)
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
            startDate = this.getRelease(startIndexRelease).date;
        else // looking for commits of first release
            startDate = this.getCommit(0).date;
        Date endDate = this.getRelease(endIndexRelease).date;

        for (Commit currCommit : this.commits){
            Date currDate = currCommit.date;
            // checking if currDate is in a correct interval:
            if (startDate.compareTo(currDate) <= 0
                    && currDate.compareTo(endDate) < 0)
                comList.add(currCommit);
        }
        return comList;
    }



    private void initializeCommitList(JgitManager manager) {
        this.commits = new ArrayList<>();
        try (RevWalk walk = new RevWalk(manager.getRepository())) {
            walk.sort(RevSort.REVERSE);
            Iterable<RevCommit> l = new Git(manager.getRepository()).log().call();
            for (RevCommit r : l){
                Commit curr = new Commit(r, manager);
                this.commits.add(curr);
            }
            Collections.sort(this.commits, (Commit o1, Commit o2) ->{
                Date  d1 = o1.date;
                Date d2 = o2.date;
                return d1.compareTo(d2);
            });
        } catch (GitAPIException e) {
            Logger logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

    private void initializeBugsList(String projectName) throws IOException {

        RetrieveInformations retrieveInformations = new RetrieveInformations(projectName);
        ArrayList<JiraBeanInformations> informations = (ArrayList<JiraBeanInformations>) retrieveInformations.getInformations();

        List<Commit> relatives;

        this.fixedBugs = new ArrayList<>();
        for (JiraBeanInformations info : informations) {
            relatives = this.findCommitsFromTicketId(info.getKey());
            /*  excluding defects that don't have a git relative fix commit */
            if (!relatives.isEmpty()) {
                Release fixV = this.findFixedVersion(relatives);
                info.setTrulyFixedVersion(fixV);
                info.setOpeningVersion(this.findOpeningVersion(info.getOpeningDate()));

                assert fixV != null;
                info.setAffectedVersions(this.findAffectedVersions(info.getAffectedVersionsName(), fixV));
                BugTicket bug = new BugTicket(info);
                bug.setRelativeCommits(relatives);
                this.fixedBugs.add(bug);
            }
        }
    }

    private List<Commit> findCommitsFromTicketId(String ticketId){
        /*  This method returns the Commit list which are relative to a given TicketId  */
        ArrayList<Commit> relativeCommits = new ArrayList<>();
        for (Commit c : this.commits)
            if (c.message.contains(ticketId))
                relativeCommits.add(c);
        return relativeCommits;
    }


    private List<Release> findAffectedVersions(List<String> affectedVersionsName, Release fixV) {
        List<Release> affectedVersions = new ArrayList<>();
        for (String name : affectedVersionsName){
            Release r = this.findReleaseFromName(name);
            if (r != null)
                affectedVersions.add(r);
        }
        if (!affectedVersions.isEmpty()){
            /*  should add all releases between the minimum and fixV  */
            Integer startIndex = affectedVersions.get(0).getIndex();
            Integer i;
            for (i = startIndex; i < fixV.getIndex(); i++) {
                Release toAdd = this.getRelease(i - 1);
                if (!affectedVersions.contains(toAdd))
                    affectedVersions.add(toAdd);

            }
        }
        return affectedVersions;
    }

    private Release findOpeningVersion(Date openingDate) {
        Integer i;
        Release curr = null;
        Date previousReleaseDate = new Date(0);
        for (i = 0; i < this.releases.size(); i++){
            curr = this.releases.get(i);
            if (i != 0)
                previousReleaseDate = this.releases.get(i - 1).date;

            if (previousReleaseDate.before(openingDate) && openingDate.before(curr.date))
                break;
        }
        return curr;
    }

    private Release findFixedVersion(List<Commit> relatives) {
        /*  this method take the list of commit that are relative to a given bug and find-out the
            last (in time) one's membership release     */

        Integer i;
        //  finding the last one, assuming relavise len > 0
        Commit last = null;
        for (Commit c : relatives){
            if (last == null || last.date.before(c.date))
                last = c;
        }
        //  finding membership release:
        /*  to check if membership release is the firstOne i need to check only if the
         *   commit's date is before than the release.get(0) date    */
        Date initialDate = new Date(0);
        Date endingDate;
        Release membershipRelease = null;

        assert last != null;
        for (i = 0; i < this.releases.size(); i++){
            membershipRelease =this.releases.get(i);
            endingDate = membershipRelease.date;
            if ( i != 0)
                initialDate = this.releases.get(i - 1).date;

            if (initialDate.before(last.date) && last.date.before(endingDate))
                break;
        }
        return membershipRelease;
    }


    //***********************************************************************************************************
    // getter and setter

    public Release getRelease(Integer index){
        return this.releases.get(index);
    }

    public Release getReleaseFromItsIndex(Integer releaseIndex){
        return this.releases.get(releaseIndex - 1);
    }

    public Commit getCommit(Integer index){
        return this.commits.get(index);
    }

    public List<BugTicket> getFixedBugs() {
        return fixedBugs;
    }

    public List<Release> getReleases() {
        return releases;
    }

    //***********************************************************************************************************
    // utility

    public Release findReleaseFromName(String s){
        /*  this method returns the release's Commit which has the version name in its name */
        Release ret = null;
        for (Release r : this.releases){
            if (r.versionName.contains(s)){
                ret = r;
                break;
            }
        }
        return ret;
    }


    public void computeFeatures() throws IOException {
        Release prev = null;
        for (Release r : this.releases) {
            this.nameToAdditionDate = r.computeMetrics(prev, this.fixedBugs, this.nameToAdditionDate);
            prev = r;
        }
    }

    private Commit findGitCommit(String id){
        Commit commit = null;
        for (Commit c : this.commits) {
            if (c.revCommit.getName().equals(id)) {
                commit = c;
                break;
            }
        }
        return commit;
    }

    private Commit findSvnCommit(String id){
        Commit commit = null;
        for (Commit c : this.commits) {
            if (c.message.contains("trunk@" + id)) {
                commit = c;
                break;
            }
        }
        return commit;
    }

    public Commit findCommitFromId(String id) {
        if (id.length() == 40) //it's a Git commit hash
            return this.findGitCommit(id);
        else
            return this.findSvnCommit(id);

    }

    public int getNumOfReleases(){
        return this.releases.size();
    }
}