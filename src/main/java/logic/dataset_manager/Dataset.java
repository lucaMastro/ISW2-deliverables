package logic.dataset_manager;

import logic.bean.BugginessAndProcessChartBean;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Dataset {

    protected ArrayList<Commit> commits;
    protected ArrayList<BugTicket> fixedBugs;

    //***********************************************************************************************************

    protected Dataset(BugginessAndProcessChartBean bean) throws IOException {
        var jgitManager = new JgitManager(bean.getDirectory().getPath());
        this.initializeCommitList(jgitManager);
    }

    private void initializeCommitList(JgitManager manager) {
        this.commits = new ArrayList<>();
        try (var walk = new RevWalk(manager.getRepository())) {
            walk.sort(RevSort.REVERSE);
            Iterable<RevCommit> l = new Git(manager.getRepository()).log().call();
            for (RevCommit r : l){
                var curr = new Commit(r, manager);
                this.commits.add(curr);
            }
            Collections.sort(this.commits, (Commit o1, Commit o2) ->{
                Date  d1 = o1.date;
                Date d2 = o2.date;
                return d1.compareTo(d2);
            });
        } catch (GitAPIException e) {
            var logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

    protected abstract void initializeBugsList(String projectName) throws IOException;

    protected List<Commit> findCommitsFromTicketId(String ticketId){
        /*  This method returns the Commit list which are relative to a given TicketId  */
        ArrayList<Commit> relativeCommits = new ArrayList<>();
        for (Commit c : this.commits)
            if (c.message.contains(ticketId))
                relativeCommits.add(c);
        return relativeCommits;
    }



    //***********************************************************************************************************
    // getter and setter

    public Commit getCommit(Integer index){
        return this.commits.get(index);
    }

    public List<BugTicket> getFixedBugs() {
        return fixedBugs;
    }

    //***********************************************************************************************************
    // utility

    protected Commit findGitCommit(String id){
        /*  returns the commit which the given SHA-1    */
        Commit commit = null;
        for (Commit c : this.commits) {
            if (c.revCommit.getName().equals(id)) {
                commit = c;
                break;
            }
        }
        return commit;
    }

    protected Commit findSvnCommit(String id){
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

    public int getNumOfCommits(){
        return this.commits.size();
    }
}