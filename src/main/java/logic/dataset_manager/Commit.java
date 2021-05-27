package logic.dataset_manager;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Commit {
    protected RevCommit revCommit;
    protected Date date;
    protected String message;
    protected JgitManager jgitManager;

    public Commit(RevCommit c, JgitManager manager) {
        this.revCommit = c;
        this.jgitManager = manager;

        this.date = c.getAuthorIdent().getWhen();
        this.message = c.getFullMessage();
    }

    public Commit(Ref r, JgitManager manager) {
        this.jgitManager = manager;
        try (var walk = new RevWalk(this.jgitManager.getRepository())) {
            RevCommit c = walk.parseCommit(r.getObjectId());
            this.revCommit = c;
            this.date = c.getAuthorIdent().getWhen();
            this.message = c.getFullMessage();
        } catch (Exception e) {
            var logger = Logger.getLogger(Commit.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

    public BugTicket isFixCommit(List<BugTicket> fixedBugs){
        /*  this method return null if "this" is not a fix commit or the BugTicket which "this" is relative to
        *   otherwise.   */
        BugTicket b = null;
        for (BugTicket bug : fixedBugs){
            if (bug.relativeCommits.contains(this)){
                b = bug;
                break;
            }
        }
        return b;
    }
}
