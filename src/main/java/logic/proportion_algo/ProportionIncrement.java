package logic.proportion_algo;

import logic.dataset_manager.BugTicket;
import logic.dataset_manager.ProportionDataset;
import logic.dataset_manager.Release;
import java.util.ArrayList;
import java.util.List;

public class ProportionIncrement {

    Integer proportionP;
    ProportionDataset dataset;

    public ProportionIncrement(ProportionDataset d){
        this.dataset = d;
        this.proportionP = 0;
    }

    private void computeInitialPvalue(List<BugTicket> bugs){
        /* bugs is a the list of bug whose fixed version index is less than or equal a certain value
        *  and AffectedVersion is not empty, info got by jira. This method computes P using jira info.  */
        double numerator;
        double denominator;
        double fract;
        double sum = 0;
        for (BugTicket b : bugs){
            numerator = (double) b.getFixedVersion().getIndex() - b.getInjectedVersion().getIndex();
            denominator = (double) b.getFixedVersion().getIndex() - b.getOpeningVersion().getIndex();
            assert denominator != 0;
            try {
                fract = numerator / denominator;
                sum += fract;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        assert !bugs.isEmpty();
        sum /= bugs.size();
        this.proportionP = (int) Math.ceil(sum);
    }

    private void computeProportion(Integer index){
        /*  this method compute the P value of the first i releases: as proportion increment wants,
        *   if i want to predict buggines of index-th release's class, i should use a P value computed
        *   for fixed bugs in releases in [1, index].   */
        List<BugTicket> bugs = this.dataset.getFixedBugs();

        /* creating a list which contain only the bugs index whose FixedVersion is less than or equal to index  */
        List<BugTicket> onlyRelativeBugs = new ArrayList<>();
        for (BugTicket b : bugs){
            if (b.getFixedVersion().getIndex() <= index && !b.getAffectedVersions().isEmpty() &&
                    !b.getFixedVersion().getIndex().equals(b.getOpeningVersion().getIndex())) //because of proportion denominator
                onlyRelativeBugs.add(b);
        }
        /*  computing p */
        this.computeInitialPvalue(onlyRelativeBugs);

        /*  updating all bugs AV    */
        this.updateBugsAV(index);

    }

    private void updateBugsAV(int index) {
        int injectedVersionIndex;
        int fixedVersionIndex;
        int openingVersionIndex;
        int i;
        List<Release> affectedVersions;
        List<BugTicket> bugs = this.dataset.getFixedBugs();
        for (BugTicket bug : bugs){
            if (bug.getFixedVersion().getIndex() <= index && bug.getAffectedVersions().isEmpty()){
                fixedVersionIndex = bug.getFixedVersion().getIndex();
                openingVersionIndex = bug.getOpeningVersion().getIndex();
                injectedVersionIndex = fixedVersionIndex - this.proportionP * (fixedVersionIndex - openingVersionIndex);
                if (injectedVersionIndex <= 0) //proportion computation returns an older realease than the firstone: impossible
                    injectedVersionIndex = 1;
                for (i = injectedVersionIndex; i < fixedVersionIndex; i++) {
                    affectedVersions = bug.getAffectedVersions();
                    affectedVersions.add(this.dataset.getReleaseFromItsIndex(i));
                    bug.setAffectedVersions(affectedVersions);
                }
            }
        }
    }

    public void computeProportionIncrement(){
        int i;
        for (i = 1; i <= this.dataset.getNumOfReleases(); i++){
            /*  this method uses releases indexing number: that's why for loop is between [1, size] */
            this.computeProportion(i);
        }
        this.setDatasetBugginess();
    }

    private void setDatasetBugginess() {
        for (BugTicket b : this.dataset.getFixedBugs()) {
            for (Release r : b.getAffectedVersions())
                r.setAllFileBuggines(b.getTouchedFiles(), this.dataset.getFiles(), r.getIndex());
        }
    }
}
