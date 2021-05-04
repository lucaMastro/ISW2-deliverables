package logic.dataset_manager;

import logic.jira_informations.JiraBeanInformations;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BugTicket  {
    String ticketId;
    ArrayList<String> affectedVersions;
    Date openingDate;
    Commit fixedVersion;

    public BugTicket(JiraBeanInformations info){
        this.ticketId = info.getKey();

        this.affectedVersions = (ArrayList<String>) info.getAffectedVersions();

        DateFormat pattern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        this.openingDate = pattern.parse(info.getOpeningVersionDate(), new ParsePosition(0));

        this.fixedVersion = info.getTrulyFixedVersion();
    }

}
