package logic.dataset_manager;

import logic.exception.InvalidRangeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.ArrayList;

public class Release extends Commit {

    /*  Incremental index for csv file  */
    Integer index;

    /* List of all commits between Release(index) and Release(index + 1)    */
    ArrayList<Commit> commits;

    /*  List of all files in this release commit    */
    ArrayList<String> files;

    //--------------------------------------------------------------------------------

    public Release(RevCommit c, Integer index){
        super(c);
        this.index = index;
        /* looking for files */
        this.files = new ArrayList<>();
        try (TreeWalk tw = new TreeWalk(JgitManager.getInstance().getRepository()) ){
            tw.setRecursive(Boolean.TRUE);
            tw.reset(c.getTree().getId());
            while (tw.next()){
                String fileName = tw.getPathString();
                this.files.add(fileName);
            }

            /*  catching commits done beetween release index-1 and realease index
             *   in range [index - 1, index)
             * */
            ArrayList<RevCommit> list =  JgitManager.getInstance().retrieveCommitsBeetwenReleases(index);
            this.commits = new ArrayList<>();
            for (RevCommit rc : list)
                this.commits.add(new Commit(rc));
        } catch (IOException | InvalidRangeException e) {
            Logger logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }



}
