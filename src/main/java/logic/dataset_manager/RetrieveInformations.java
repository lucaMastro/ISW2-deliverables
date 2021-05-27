package logic.dataset_manager;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import logic.bean.JiraBeanInformations;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class RetrieveInformations {

    private String projectName;
    private JSONArray fixedBugs;
    private Integer fixedBugTicketsNumber;

    public RetrieveInformations(String projName) throws IOException {
        this.projectName = projName;
        this.fixedBugs = new JSONArray();
        this.retrieveFixedBugTickets();
    }

    private void retrieveFixedBugTickets()throws IOException {
        Integer j = 0;
        Integer i = 0;
        //Get JSON API for closed bugs w/ AV in the project
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + this.projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,versions,created&startAt="
                    + i.toString() + "&maxResults=" + j.toString();

            JSONObject json = readJsonFromUrl(url);

            this.fixedBugs = concatenate(this.fixedBugs, json.getJSONArray("issues"));

            this.fixedBugTicketsNumber = json.getInt("total");
            //if total is >= jsonArrayLength * 1000, i need another iteration. just increment of 1000
            i += 1000;

        } while (i < this.fixedBugTicketsNumber);

    }

    private JSONArray concatenate(JSONArray a1, JSONArray a2) {
        int i;
        JSONArray result = new JSONArray();
        for (i = 0; i < a1.length(); i++)
            result.put(a1.get(i));

        for (i = 0; i < a2.length(); i++)
            result.put(a2.get(i));

        return result;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))){
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } finally {
            is.close();
        }
    }

    public List<JiraBeanInformations> getInformations(){
        Integer i;
        ArrayList<JiraBeanInformations> list = new ArrayList<>();

        for (i = 0; i < this.fixedBugs.length(); i++){
            JSONObject obj = this.fixedBugs.getJSONObject(i);
            JiraBeanInformations curr = new JiraBeanInformations(obj);
            list.add(curr);
        }
        return list;
    }

}
