package logic.dataset_manager;

import logic.bean.JiraBeanInformations;
import logic.bean.ProportionBean;
import logic.exception.InvalidRangeException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import java.io.IOException;
import java.util.*;

public class ProportionDataset extends Dataset {
    private ArrayList<Release> releases;
    private ReleaseFileManager files;

    //***********************************************************************************************************
    // Constructor and relative methods
    public ProportionDataset(ProportionBean bean) throws GitAPIException, IOException, InvalidRangeException {
        super(bean);
        var jgitManager = new JgitManager(bean.getDirectory().getPath());

        this.removeRevertCommits();
        this.initializeReleaseList(jgitManager);

        this.initializeBugsList(bean.getProject());

        this.files = new ReleaseFileManager(jgitManager, this.releases);
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
        int len = tagList.size();

        for (i = 0; i < len; i++){
            var cur = new Release(tagList.get(i), manager);
            this.releases.add(cur);
        }
        Collections.sort(this.releases, (Commit o1, Commit o2) ->{
            Date d1 = o1.date;
            Date d2 = o2.date;
            return d1.compareTo(d2);
        });
        for (i = 0; i < len; i++) {
            Release cur = this.releases.get(i);
            cur.setIndex(i + 1);
            cur.commits = (ArrayList<Commit>) this.retrieveCommitsBetweenReleases(i + 1);
        }
    }

    public void removeHalfRelease(){
        this.releases.removeIf(r -> r.getIndex() > this.releases.size() / 2);
    }

    private List<Commit> retrieveCommitsBetweenReleases(Integer endIndexRelease)
            throws InvalidRangeException {

        /* this.retrieveCommits(2) should return a list of all commits performed between release1 and release 2
         * That's why, if endIndexRelease is less than 1 an exception is thrown */

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
        var endDate = this.getRelease(endIndexRelease).date;

        for (Commit currCommit : this.commits){
            var currDate = currCommit.date;
            // checking if currDate is in a correct interval:
            if (startDate.compareTo(currDate) <= 0
                    && currDate.compareTo(endDate) < 0)
                comList.add(currCommit);
        }
        return comList;
    }


    @Override
    protected void initializeBugsList(String projectName) throws IOException {

        var retrieveInformations = new RetrieveInformations(projectName);
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
                var bug = new BugTicket(info);
                bug.setRelativeCommits(relatives);
                this.fixedBugs.add(bug);
            }
        }
        Collections.sort(this.fixedBugs, (BugTicket b1, BugTicket b2) ->{
            Date d1 = b1.getFixedVersion().date;
            Date d2 = b2.getFixedVersion().date;
            return d1.compareTo(d2);
        });
    }


    private List<Release> findAffectedVersions(List<String> affectedVersionsName, Release fixV) {
        List<Release> affectedVersions = new ArrayList<>();
        for (String name : affectedVersionsName){
            var r = this.findReleaseFromName(name);
            if (r != null)
                affectedVersions.add(r);
        }
        if (!affectedVersions.isEmpty()){
            /*  should add all releases between the minimum and fixV  */
            Integer startIndex = affectedVersions.get(0).getIndex();
            Integer i;
            for (i = startIndex; i < fixV.getIndex(); i++) {
                var toAdd = this.getRelease(i - 1);
                if (!affectedVersions.contains(toAdd))
                    affectedVersions.add(toAdd);

            }
        }
        return affectedVersions;
    }

    private Release findOpeningVersion(Date openingDate) {
        Integer i;
        Release curr = null;
        var previousReleaseDate = new Date(0);
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
        var initialDate = new Date(0);
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
            this.files = r.computeMetrics(prev, this.fixedBugs, this.files);
            prev = r;
        }
    }

    public int getNumOfReleases(){
        return this.releases.size();
    }

    public List<ReleaseFile> getFiles() {
        return this.files.getFileList();
    }
}
