package logic.jira_informations;

import logic.dataset_manager.Commit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JiraBeanInformations {

    private String key;
    private String openingVersionDate;
    private ArrayList<String> affectedVersions;
    private ArrayList<String> fixedVersions;
    private Commit trulyFixedVersion;

    public JiraBeanInformations(JSONObject jo){
        Integer i;

        this.key = (String) jo.get("key");

        JSONObject obj = jo.getJSONObject("fields");
        this.openingVersionDate = (String) obj.get("created");

        this.affectedVersions = new ArrayList<>();
        JSONArray affected = obj.getJSONArray("versions");
        for (i = 0; i < affected.length(); i++){
            String curr = (String) affected.getJSONObject(i).get("name");
            this.affectedVersions.add(curr);
        }

        this.fixedVersions = new ArrayList<>();
        JSONArray fixed = obj.getJSONArray("fixVersions");
        for (i = 0; i < fixed.length(); i++){
            String curr = (String) fixed.getJSONObject(i).get("name");
            this.fixedVersions.add(curr);
        }
    }

    public String getKey() {
        return key;
    }

    public String getOpeningVersionDate() {
        return openingVersionDate;
    }

    public List<String> getAffectedVersions() {
        return affectedVersions;
    }

    public List<String> getFixedVersions() {
        return fixedVersions;
    }

    public Commit getTrulyFixedVersion() {
        return trulyFixedVersion;
    }

    public void setTrulyFixedVersion(Commit trulyFixedVersion) {
        this.trulyFixedVersion = trulyFixedVersion;
    }
}
