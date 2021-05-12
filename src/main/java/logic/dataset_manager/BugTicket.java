package logic.dataset_manager;

import logic.jira_informations.JiraBeanInformations;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BugTicket  {
    String ticketId;
    ArrayList<String> affectedVersions;
    Date openingDate;
    Release fixedVersion;
    Release openingVersion;
    ArrayList<Commit> relativeCommits;

    public BugTicket(JiraBeanInformations info){
        this.ticketId = info.getKey();

        this.affectedVersions = (ArrayList<String>) info.getAffectedVersions();

        this.openingDate = info.getOpeningDate();
        this.fixedVersion = (Release) info.getTrulyFixedVersion();
        this.openingVersion = info.getOpeningVersion();
    }

    public void setRelativeCommits(List<Commit> relativeCommits) {
        this.relativeCommits = (ArrayList) relativeCommits;
    }
}
