package logic.dataset_manager;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Commit {
    RevCommit revCommit;
    Date date;
    String message;

    public Commit(RevCommit c) {
        this.revCommit = c;
        this.date = c.getAuthorIdent().getWhen();
        this.message = c.getFullMessage();
    }

    public Commit(Ref r) {
        try (RevWalk walk = new RevWalk(JgitManager.getInstance().getRepository())) {
            RevCommit c = walk.parseCommit(r.getObjectId());
            this.revCommit = c;
            this.date = c.getAuthorIdent().getWhen();
            this.message = c.getFullMessage();
        } catch (Exception e) {
            Logger logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }
}
