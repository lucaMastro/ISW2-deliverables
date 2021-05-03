package logic.dataset_manager;

import logic.jira_informations.JiraBeanInformations;
import org.json.JSONObject;
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

        this.affectedVersions = info.getAffectedVersions();

        DateFormat pattern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        this.openingDate = pattern.parse(info.getOpeningVersionDate(), new ParsePosition(0));

        this.fixedVersion = info.getTrulyFixedVersion();
    }


    public BugTicket(String s){
        this.ticketId = s;
    }
    public BugTicket(JSONObject jo) {
        this.ticketId = (String) jo.get("key");
        JSONObject fields = (JSONObject) jo.get("fields");

        String date = (String) fields.get("created");
        DateFormat pattern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        this.openingDate = pattern.parse(date, new ParsePosition(0));



        //this.openingDate =
    }

    public static void main(String[] args){
        String s = "2017-08-09T18:25:48.000+0000";
    }
}
