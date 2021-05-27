package logic.dataset_manager;

import logic.bean.JiraBeanInformations;
import org.eclipse.jgit.diff.DiffEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BugTicket  {
    String ticketId;
    Date openingDate;

    ArrayList<Release> affectedVersions;
    //Release ingectedVersion is the first element of the previous array
    Release fixedVersion;
    Release openingVersion;

    ArrayList<Commit> relativeCommits;
    ArrayList<String> touchedFiles; //files modified by relativeCommit. just need the name

    public BugTicket(JiraBeanInformations info){
        this.ticketId = info.getKey();
        this.touchedFiles = new ArrayList<>();

        this.affectedVersions = (ArrayList<Release>) info.getAffectedVersions();
        this.openingDate = info.getOpeningDate();
        this.fixedVersion = (Release) info.getTrulyFixedVersion();
        this.openingVersion = info.getOpeningVersion();
    }

    public void setRelativeCommits(List<Commit> relativeCommits) {
        this.relativeCommits = (ArrayList<Commit>) relativeCommits;
    }

    public void addFileTouched(DiffEntry diff) {

        String fileName = diff.getNewPath();
        //adding the file if not present and not added
        if (!diff.getChangeType().equals(DiffEntry.ChangeType.ADD) &&
                !this.touchedFiles.contains(fileName)) {
            this.touchedFiles.add(fileName);
        }
    }

    public Release getInjectedVersion(){
        return this.affectedVersions.isEmpty() ? null : this.affectedVersions.get(0);
    }

    public Release getFixedVersion(){
        return  this.fixedVersion;
    }

    public List<Release> getAffectedVersions() {
        return affectedVersions;
    }

    public void setAffectedVersions(List<Release> affectedVersions) {
        this.affectedVersions = (ArrayList<Release>) affectedVersions;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public List<String> getTouchedFiles() {
        return touchedFiles;
    }


}
