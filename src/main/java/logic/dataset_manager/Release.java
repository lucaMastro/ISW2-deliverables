package logic.dataset_manager;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.treewalk.TreeWalk;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.ArrayList;

public class Release extends Commit {

    /*  Incremental index for csv file  */
    Integer index;
    /*  Numeric versioning  */
    String versionName;

    /* List of all commits between Release(index) and Release(index + 1)    */
    ArrayList<Commit> commits;

    /*  List of all files in this release commit    */
    ArrayList<ReleaseFile> files;

    //--------------------------------------------------------------------------------

    public Release(Ref ref){
        super(ref);
        this.versionName = ref.getName();
        /* looking for files */
        this.files = new ArrayList<>();
        try (TreeWalk tw = new TreeWalk(JgitManager.getInstance().getRepository()) ){
            tw.setRecursive(Boolean.TRUE);
            tw.reset(this.revCommit.getTree().getId());
            while (tw.next()){
                String fileName = tw.getPathString();
                if (fileName.endsWith(".java"))
                    this.files.add( new ReleaseFile(fileName) );
            }
        } catch (IOException | GitAPIException e) {
            Logger logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setEachFileLoc() throws IOException, GitAPIException {
        for (ReleaseFile f : this.files)
            f.computeLoc(this.revCommit);
    }
}
