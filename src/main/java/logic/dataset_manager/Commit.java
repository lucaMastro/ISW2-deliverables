package logic.dataset_manager;

import org.eclipse.jgit.revwalk.RevCommit;
import java.util.Date;


public class Commit {
    RevCommit commit;
    Date date;

    public Commit(RevCommit c){
        this.commit = c;
        this.date = c.getAuthorIdent().getWhen();
    }



}
