package logic.dataset_manager;

import org.eclipse.jgit.revwalk.RevCommit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class DatasetConstructor {

    public ArrayList<Release> releases;

    public DatasetConstructor() throws IOException {
        JgitManager instance = JgitManager.getInstance();
        this.releases = new ArrayList<>();

        ArrayList<RevCommit> releases = instance.getTags();
        Integer index = 1;
        for (RevCommit rel : releases ) {
            this.releases.add(new Release(rel, index));
            index++;
        }
    }

    public static void main(String[] args) throws IOException {
        DatasetConstructor dsc = new DatasetConstructor();
        int count = 0;
        for (Release r : dsc.releases){
            count += r.commits.size();
        }
        System.out.println(count);
        System.out.println(JgitManager.getInstance().getCommits().size());
        JgitManager i = JgitManager.getInstance();
        Collections.reverse(i.getCommits());
        count = 0;
        int j = 0;
        Date d = dsc.releases.get(dsc.releases.size() - 1).date;
        for (RevCommit t: i.getCommits()){
            if (d.compareTo(t.getAuthorIdent().getWhen()) < 0)
                count++;
            j++;
            if (j >= 50)
                break;
        }
    }
}
