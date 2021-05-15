package logic.proportion_algo;

import logic.dataset_manager.BugTicket;
import logic.dataset_manager.Dataset;
import logic.dataset_manager.Release;
import logic.exception.InvalidRangeException;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProportionIncrement {

    Integer proportionP;
    Dataset dataset;

    public ProportionIncrement() throws IOException, InvalidRangeException, GitAPIException {
        this.dataset = new Dataset();
        this.proportionP = 0;
    }


    private void computePvalue(List<BugTicket> bugs){
        /* bugs is a the list of bug whose fixed version index is less than or equal a certain value
        *  and AffectedVersion is not empty, info got by jira  */
        double numerator;
        double denominator;
        double fract;
        double sum = 0;
        for (BugTicket b : bugs){
            numerator = (double) b.getFixedVersion().getIndex() - b.getInjectedVersion().getIndex();
            denominator = (double) b.getFixedVersion().getIndex() - b.getOpeningVersion().getIndex();
            assert denominator != 0;
            fract = numerator / denominator;
            sum += fract;
        }
        assert !bugs.isEmpty();
        sum /= bugs.size();
        this.proportionP = (int) Math.ceil(sum);
    }

    private void computeProportion(Integer index){
        /*  this method compute the P value of the first index releases: as proportion increment wants,
        *   if i want to predict buggines of index-th release's classes, i should use a P value computed
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
        this.computePvalue(onlyRelativeBugs);

        /*  updating all bugs AV    */
        this.updateBugsAV(bugs);

    }

    private void updateBugsAV(List<BugTicket> bugs) {
        int injectedVersionIndex;
        int fixedVersionIndex;
        int openingVersionIndex;
        int i;
        List<Release> affectedVersions;
        for (BugTicket bug : bugs){
            if (bug.getAffectedVersions().isEmpty()){
                fixedVersionIndex = bug.getFixedVersion().getIndex();
                openingVersionIndex = bug.getOpeningVersion().getIndex();
                injectedVersionIndex = fixedVersionIndex - this.proportionP * (fixedVersionIndex - openingVersionIndex);
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
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDatasetBugginess() {
        for (BugTicket b : this.dataset.getFixedBugs()) {
            for (Release r : b.getAffectedVersions())
                r.setAllFileBuggines(b.getTouchedFiles());
        }
    }


    public static void main(String[] args) throws InvalidRangeException, IOException, GitAPIException {
        ProportionIncrement p = new ProportionIncrement();
        p.computeProportionIncrement();
        p.dataset.computeFeatures();
    }

}
