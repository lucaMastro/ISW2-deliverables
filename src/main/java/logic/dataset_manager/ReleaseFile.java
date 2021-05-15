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

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReleaseFile {

    private String name;
    private List<PersonIdent> editors;
    private Date additionDate;

    //metrics
    private long loc; //lines of code
    private long nr;    //number of revisions that modifies this file
    private long nFix; //number of bug fixes on this file
    private long nAuth; //number of authors which commits this file
    private long locAdded; //sum over revision of LOC added
    private long maxLocAdded; //maximum over revisions of LOC added
    private long churn; //sum over revisions of added - deleted LOC
    private long maxChurn; //maximum churn over revisions
    private long age; //age of Release

    private Boolean buggy;


    public ReleaseFile(String name){
        this.editors = new ArrayList<>();
        this.name = name;
        this.nr = 0;
        this.nAuth = 0;
        this.locAdded = 0;
        this.churn = 0;
        this.maxChurn = 0;
        this.maxLocAdded = 0;
        this.age = 0;
        this.buggy = Boolean.FALSE;
    }

    public String getPath(){
        return this.name;
    }

    public void computeLoc(RevCommit release) throws IOException {
        this.loc = JgitManager.getInstance().getLocFileInGivenRelease(this.name, release);
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
                .append(this.churn).append(",")
                .append(this.maxChurn).append(",")
                .append(this.age).append(",");
        String isBuggy = this.buggy.equals(Boolean.TRUE) ? "Yes" : "No";
        sb.append(isBuggy).append("\n");
        return sb.toString();
    }

    public void updateNumberOfRevision(){
        this.nr++;
    }

    public void updateNfix() {
        this.nFix ++;
    }

    public void addEditors(PersonIdent editor) {
        if (!this.editors.contains(editor))
            this.editors.add(editor);
    }

    public void updateNauth(){
        this.nAuth = this.editors.size();
    }

    public void updateLocAdded(Integer linesAdded) {
        this.locAdded += linesAdded;
        if (this.maxLocAdded < linesAdded)
            this.maxLocAdded = linesAdded;
    }

    public void updateChurn(int i) {
        this.churn += i;
        if (this.maxChurn < i)
            this.maxChurn = i;
    }

    public void setAdditionDate(Date date) {
        this.additionDate = date;
    }

    public void computeAge(Date releaseDate) {
        Long fileTime = this.additionDate.getTime();
        Long releaseTime = releaseDate.getTime();
        Long diffTime = releaseTime - fileTime; //milliseconds

        /* milliseconds to weeks:
        *  n [ms] = n / 1000 [s] = n / (1000 * 60) [m] =
        *  n / (1000 * 60 * 60) [h] = n / (1000 * 60 * 60 * 24) [d] =
        *  n / (1000 * 60 * 60 * 24 * 7) [w]
        * */
        this.age = diffTime / (1000 * 60 * 60 * 24 * 7);
    }

    public void updateBugginess() {
        this.buggy = Boolean.TRUE;
    }
}
