/*
 *
 *   Size                  Lines of code(LOC)
 *   LOC                   Touched Sum over revisions of LOC added + deleted + modified
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

import logic.config_manager.ConfigurationManager;

import java.io.File;

public class ReleaseFile extends File {

    //metrics
    private long loc; //lines of code
    private long nr; //number of revisions
    private long nFix; //number of bug fixes
    private long nAuth; //number of authors
    private long locAdded; //sum over revision of LOC added
    private long maxLocAdded; //maximum over revisions of LOC added
    private long avgLocAdded; //average LOC added per revision
    private long churn; //sum over revisions of added - deleted LOC
    private long maxChurn; //maximum churn over revisions
    private long averageChurn; //average churn over revisions
    private long age; //age of Release
    private long weightedAge; //age of Release weighted by LOC touched

    public ReleaseFile(String name){
        super(ConfigurationManager.getConfigEntry("repositoryPath") + name);
    }

}
