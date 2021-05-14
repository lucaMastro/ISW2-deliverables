package logic.jira_informations;

import logic.dataset_manager.Commit;
import logic.dataset_manager.Release;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JiraBeanInformations {

    private String key;
    private Date openingDate;
    private Release openingVersion;

    private ArrayList<String> affectedVersionsName;
    private ArrayList<Release> affectedVersions;

    private Release trulyFixedVersion;

    public JiraBeanInformations(JSONObject jo){
        Integer i;

        this.key = (String) jo.get("key");

        JSONObject obj = jo.getJSONObject("fields");
        String openingVersionDate = (String) obj.get("created");
        DateFormat pattern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        this.openingDate = pattern.parse(openingVersionDate, new ParsePosition(0));

        this.affectedVersionsName = new ArrayList<>();
        JSONArray affected = obj.getJSONArray("versions");
        for (i = 0; i < affected.length(); i++){
            String curr = (String) affected.getJSONObject(i).get("name");
            this.affectedVersionsName.add(curr);
        }
    }

    public String getKey() {
        return key;
    }


    public List<String> getAffectedVersionsName() {
        return affectedVersionsName;
    }


    public Commit getTrulyFixedVersion() {
        return trulyFixedVersion;
    }

    public void setTrulyFixedVersion(Release trulyFixedVersion) {
        this.trulyFixedVersion = trulyFixedVersion;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public void setOpeningVersion(Release openingVersion) {
        this.openingVersion = openingVersion;
    }

    public Date getOpeningDate() {
        return openingDate;
    }

    public List<Release> getAffectedVersions(){
        return this.affectedVersions;
    }

    public void setAffectedVersions(List<Release> affectedVersions) {
        this.affectedVersions = (ArrayList) affectedVersions;
    }
}
