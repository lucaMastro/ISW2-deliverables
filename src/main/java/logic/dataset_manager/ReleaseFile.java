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
import java.util.*;


public class ReleaseFile {

    private Date additionDate;
    private JgitManager jgitManager;

    private String[] names;
    private ArrayList<PersonIdent>[] editors;

    //metrics
    private Long[] loc; //lines of code
    private Long[] nr;    //number of revisions that modifies this file
    private Long[] nFix; //number of bug fixes on this file
    private Long[] nAuth; //number of authors which commits this file
    private Long[] locAdded; //sum over revision of LOC added
    private Long[] maxLocAdded; //maximum over revisions of LOC added
    private Long[] churn; //sum over revisions of added - deleted LOC
    private Long[] maxChurn; //maximum churn over revisions
    private Long[] age; //age of Release

    private Boolean[] buggy;

    public ReleaseFile(JgitManager manager, int numberOfRelease, int currRelease, String name) {
        this(manager,numberOfRelease,currRelease,null,name);
    }


    public ReleaseFile(JgitManager manager, int numberOfRelease, int currRelease, Date date, String name){
        this.jgitManager = manager;
        this.additionDate = date;

        this.editors = new ArrayList[numberOfRelease];

        this.loc = new Long[numberOfRelease];
        this.names = new String[numberOfRelease];
        this.nr = new Long[numberOfRelease];
        this.nFix = new Long[numberOfRelease];
        this.nAuth = new Long[numberOfRelease];
        this.locAdded = new Long[numberOfRelease];
        this.churn = new Long[numberOfRelease];
        this.maxChurn = new Long[numberOfRelease];
        this.maxLocAdded = new Long[numberOfRelease];
        this.age = new Long[numberOfRelease];
        this.buggy = new Boolean[numberOfRelease];

        int i;

        for (i = 0; i < numberOfRelease; i++) {
            this.editors[i] = new ArrayList<>();
            this.loc[i] = (long) 0;
            this.names[i] = i == currRelease - 1 ? name : "";
            this.nr[i] = (long) 0;
            this.nFix[i] = (long) 0;
            this.nAuth[i] = (long) 0;
            this.locAdded[i] = (long) 0;
            this.maxLocAdded[i] = (long) 0;
            this.churn[i] = (long) 0;
            this.maxChurn[i] = (long) 0;
            this.age[i] = (long) 0;
            this.buggy[i] = Boolean.FALSE;
        }
    }

    public Boolean isThisFile(String name){
        /*  checks if this file has/had the given name */
        Boolean b = Boolean.FALSE;
        for (String s : this.names)
            if (s.equals(name)){
                b = Boolean.TRUE;
                break;
            }
        return b;
    }

    public void addName(String name, int releaseIndex) {
        this.names[releaseIndex - 1] = name;
    }

    public void computeLoc(RevCommit release, Integer index) throws IOException {
        var currName = this.names[index - 1];
        if (!currName.isEmpty())
            this.loc[index - 1] = (long) this.jgitManager.getLocFileInGivenRelease(this.names[index - 1], release);
    }

    public void updateNumberOfRevision(Integer index) {
        var currName = this.names[index - 1];
        if (!currName.isEmpty())
            this.nr[index - 1]++;
    }

    public void addEditors(Integer index, PersonIdent authorIdent) {
        var currName = this.names[index - 1];
        if (!currName.isEmpty()) {
            var list = this.editors[index - 1];
            if (!list.contains(authorIdent))
                list.add(authorIdent);
        }
    }

    public void updateNauth(Integer index) {
        var currName = this.names[index - 1];
        if (!currName.isEmpty())
            this.nAuth[index - 1] = (long) this.editors[index - 1].size();
    }

    public void updateLocAdded(Integer index, Integer linesAdded) {
        var currName = this.names[index - 1];
        if (!currName.isEmpty()) {
            this.locAdded[index - 1] += linesAdded;
            if (this.maxLocAdded[index - 1] < linesAdded)
                this.maxLocAdded[index - 1] = (long) linesAdded;
        }
    }

    public void updateChurn(Integer index, int i) {
        var currName = this.names[index - 1];
        if (!currName.isEmpty()) {
            this.churn[index - 1] += i;
            if (this.maxChurn[index - 1] < i)
                this.maxChurn[index - 1] = (long) i;
        }
    }

    public void updateNfix(Integer index) {
        var currName = this.names[index - 1];
        if (!currName.isEmpty())
            this.nFix[index - 1]++;
    }

    public void computeAge(Integer index, Date releaseDate) {
        var currName = this.names[index - 1];
        if (!currName.isEmpty()) {
            Long fileTime = this.additionDate.getTime();
            Long releaseTime = releaseDate.getTime();
            Long diffTime = releaseTime - fileTime; //milliseconds

            /* milliseconds to weeks:
             *  n [ms] = n / 1000 [s] = n / (1000 * 60) [m] =
             *  n / (1000 * 60 * 60) [h] = n / (1000 * 60 * 60 * 24) [d] =
             *  n / (1000 * 60 * 60 * 24 * 7) [w]
             * */
            this.age[index - 1] = diffTime / (1000 * 60 * 60 * 24 * 7);
        }
    }

    public void updateBugginess(Integer index) {
        var currName = this.names[index - 1];
        if (!currName.isEmpty())
            this.buggy[index - 1] = Boolean.TRUE;
    }

    public String[] getNames() {
        return this.names;
    }

    public String getOutputLine(Integer index) {
        var sb = new StringBuilder();
        var currName = this.names[index - 1];
        if (!currName.isEmpty()) {
            sb.append(this.names[index - 1]).append(",")
                    .append(this.loc[index - 1]).append(",")
                    .append(this.nr[index - 1]).append(",")
                    .append(this.nFix[index - 1]).append(",")
                    .append(this.nAuth[index - 1]).append(",")
                    .append(this.locAdded[index - 1]).append(",")
                    .append(this.maxLocAdded[index - 1]).append(",")
                    .append(this.churn[index - 1]).append(",")
                    .append(this.maxChurn[index - 1]).append(",")
                    .append(this.age[index - 1]).append(",");
            sb.append(this.buggy[index - 1].equals(Boolean.TRUE) ? "Yes" : "No").append("\n");
        }
        return sb.toString();
    }

    public void setAdditiondate(Date date) {
        this.additionDate = date;
    }

    public Date getDate() {
        return this.additionDate;
    }
}
