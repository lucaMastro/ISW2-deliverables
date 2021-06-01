package logic.dataset_manager;

import java.util.ArrayList;
import java.util.List;

public class ReleaseFileManager {

    private ArrayList<ReleaseFile> files;
    private Integer numOfRelease;

    public ReleaseFileManager(JgitManager jgitManager, List<Release> releases){
        this.files = new ArrayList<>();

        this.numOfRelease = releases.size();
        for (Release r : releases){
            var nameList = jgitManager.filesInRelease(r.revCommit);
            for (String name : nameList){
                var stillExist = this.findFileFromName(name);
                if (stillExist != null)
                    stillExist.addName(name, r.getIndex());
                else
                    this.files.add(new ReleaseFile(jgitManager, this.numOfRelease, r.getIndex(),name));
            }
        }
    }

    public ReleaseFile findFileFromName(String name){
        ReleaseFile releaseFile = null;
        /*  getting last token of name:
         *   /src/main/java/logic/dataset_manager/ProportionDataset.java -> /ProportionDataset.java   */
        var classNames = name.split("/");
        var className = "/" + classNames[classNames.length - 1];

        int i;
        for (ReleaseFile r : this.files) {
            var names = r.getNames();
            for (i = 0; i < this.numOfRelease && releaseFile == null; i++){
                var currName = names[i];
                if (currName.endsWith(className)){
                    releaseFile = r;
                    break;
                }
            }
        }
        return releaseFile;
    }

    public List<ReleaseFile> getFileList() {
        return this.files;
    }
}
