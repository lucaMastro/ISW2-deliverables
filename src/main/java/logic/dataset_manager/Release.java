package logic.dataset_manager;

import org.eclipse.jgit.lib.ObjectId;

import java.util.ArrayList;
import java.util.Date;

public class Release {

    /* Date of release and version code (v3.0.1)*/
    Date releaseDate;
    String release;

    /*  Incremental index for csv file  */
    Integer index;

    /* List of all commits between Release(index) and Release(index + 1)    */
    ArrayList<ObjectId> commits;

    //--------------------------------------------------------------------------------

    public Release(Integer index){
        this.index = index;
    }



}
