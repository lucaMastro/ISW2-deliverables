/*
 *
 *   Size                  Lines of code(LOC)
 *   LOC                   Touched Sum over revisions of LOC added + deleted
 *   NR                    Number of revisions
 *   Nfix                  Number of bug fixes
 *   Nauth                 Number of authors who committed the file
 *   LOC Added             Sum over revisions of LOC added
 *   MAX LOC Added         Maximum over revisions of LOC added
 *   AVG LOC Added         Average LOC added per revision
 *   Churn                 Sum over revisions of added - deleted LOC
 *   Max Churn             Maximum churn over revisions
 *   Average Churn         Average churn over revisions
 *   Change Set Size       Number of files committed together
 *   Max Change Set        Maximum change set size over revisions
 *   Average Change Set    Average change set size over revisions
 *   Age                   Age of Release
 *   Weighted Age          Age of Release weighted by LOC touched
 * */


package logic.dataset_manager;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import java.io.IOException;

public class ReleaseFile {

    String name;
    //metrics
    private long loc; //lines of code
    private long nr; //number of revisions that modifies this file
    private long nFix; //number of bug fixes on this file
    private long nAuth; //number of authors which commits this file
    private long locAdded; //sum over revision of LOC added
    private long maxLocAdded; //maximum over revisions of LOC added
    private long avgLocAdded; //average LOC added per revision
    private long churn; //sum over revisions of added - deleted LOC
    private long maxChurn; //maximum churn over revisions
    private long averageChurn; //average churn over revisions
    private long age; //age of Release
    private long weightedAge; //age of Release weighted by LOC touched

    public ReleaseFile(String name){
        this.name = name;
        this.nr = 0;
    }

    public String getPath(){
        return this.name;
    }

    public void computeLoc(RevCommit release) throws IOException, GitAPIException {
        Integer loc =  JgitManager.getInstance().getLocFileInGivenRelease(this.name, release);
        this.loc = loc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name).append(",")
                .append(this.loc).append(",")
                .append(this.nr).append(",")
                .append(this.nFix).append(",")
                .append(this.nAuth).append(",")
                .append(this.locAdded).append(",")
                .append(this.maxLocAdded).append(",")
                .append(this.avgLocAdded).append(",")
                .append(this.churn).append(",")
                .append(this.maxChurn).append(",")
                .append(this.averageChurn).append(",")
                .append(this.age).append(",")
                .append(this.weightedAge);
        return sb.toString();
    }

    public void updateNumberOfRevision(){
        this.nr++;
    }
}
