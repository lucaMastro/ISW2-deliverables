package logic.dataset_manager;

import logic.bean.BugginessAndProcessChartBean;
import logic.bean.JiraBeanInformations;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang3.time.DateUtils;
import org.decimal4j.util.DoubleRounder;

public class ProcessControlChartDataset extends Dataset {

    private ArrayList<ProcessControlChartEntry> months;
    private Integer threshold;
    private Double mean;
    private Double variance;
    private Double upperControlLimit;
    private Double lowerControlLimit;

    public ProcessControlChartDataset(BugginessAndProcessChartBean bean) throws IOException {
        super(bean);
        this.initializeBugsList(bean.getProject());
        this.threshold = 0;
        this.mean = 0.0;
        this.upperControlLimit = 0.0;
        this.lowerControlLimit = 0.0;

        this.months = new ArrayList<>();
        var initialDate = Calendar.getInstance();
        var lastDate = Calendar.getInstance();

        var unroundedInitialDate = this.getCommit(0).getDate();
        var unroundedFinalDate = this.getCommit(this.getNumOfCommits()-1).getDate();

        //rounding the dates to their months
        initialDate.setTime(DateUtils.round(unroundedInitialDate, Calendar.MONTH));
        lastDate.setTime(DateUtils.round(unroundedFinalDate, Calendar.MONTH));

        while (initialDate.compareTo(lastDate) <= 0){
            this.months.add(new ProcessControlChartEntry(initialDate.getTime()));
            initialDate.add(Calendar.MONTH, 1);
        }
    }

    @Override
    protected void initializeBugsList(String projectName) throws IOException {
        var retrieveInformations = new RetrieveInformations(projectName);
        ArrayList<JiraBeanInformations> informations = (ArrayList<JiraBeanInformations>)
                retrieveInformations.getInformations();

        List<Commit> relatives;

        this.fixedBugs = new ArrayList<>();
        for (JiraBeanInformations info : informations) {
            relatives = this.findCommitsFromTicketId(info.getKey());
            /*  excluding defects that don't have a git relative fix commit */
            if (!relatives.isEmpty()) {
                var bug = new BugTicket(info);
                bug.setRelativeCommits(relatives);
                this.fixedBugs.add(bug);
            }
        }
    }

    //*****************************************************************************************************

    public List<Commit> retrieveCommitsBetweenDates(Date initialDate, Date finalDate){
        var list = new ArrayList<Commit>();

        Date currDate;
        for (Commit c : this.commits){
            currDate = c.getDate();
            if (finalDate.before(currDate))
                break;

            if (initialDate.before(currDate) && currDate.before(finalDate))
                list.add(c);
        }
        return list;
    }

    private void updateFixBugNum(ProcessControlChartEntry older,List<Commit> list){
        /*  given a list of commit, this method will remove the non-fixBug commits  */
        list.removeIf(commit -> commit.isFixCommit(this.fixedBugs) == null);
        older.setNumberOfFixCommits(list.size());
    }

    public void computeFeatures(){
        int i;
        int n;
        int size;
        Date olderDate;
        Date newerDate;

        ProcessControlChartEntry olderEntry;
        ProcessControlChartEntry newer;
        size = this.months.size();
        for (i = 1; i <= size; i++){
            olderEntry = this.months.get(i - 1);
            olderDate = olderEntry.getDate();

            if (i == size)
                newerDate = new Date(olderDate.getTime() * 2); //just make a bigger date then the older
            else {
                newer = this.months.get(i);
                newerDate = newer.getDate();
            }
            List<Commit> list = this.retrieveCommitsBetweenDates(olderDate, newerDate);
            n = list.size();

            olderEntry.setNumberOfCommits(n);
            this.updateFixBugNum(olderEntry, list);
        }
        this.computeMean();
        this.computeVariance();
        this.computeUpperControlLimit();
        this.computeLowerControlLimit();
    }

    private void computeMean() {
        int n = 0;
        Double m = 0.0;
        for (ProcessControlChartEntry entry : this.months){
            if (entry.getCommitsNum() >= this.threshold) {
                m += entry.getFixedBugs();
                n++;
            }
        }
        if (n != 0) { //if this is False, mean never changes its default value
            m /= n;
            this.mean = DoubleRounder.round(m, 3);
        }
    }

    private void computeVariance(){
        Integer n = 0;
        Double v = 0.0;
        for (ProcessControlChartEntry entry : this.months){
            if (entry.getCommitsNum() >= this.threshold) {
                v += Math.pow(this.mean - entry.getFixedBugs(), 2);
                n++;
            }
        }
        if (n != 0) {
            v /= n;
            this.variance = DoubleRounder.round(v, 3);
        }
    }

    private void computeUpperControlLimit(){
        Double ucl = this.mean + 3 * this.variance;
        this.upperControlLimit = DoubleRounder.round(ucl, 3);
    }

    private void computeLowerControlLimit(){
        /* it has not sense to put negative values in the "fixed bugs" */
        Double lcl = this.mean - 3 * this.variance;
        if (lcl > 0) //else it's default value 0 never changed
            this.lowerControlLimit = DoubleRounder.round(lcl, 3);
    }


    @Override
    public String toString(){
        StringBuilder bld = new StringBuilder("date,fixedCommits,totalCommits,mean,upperControlLimit,lowerControlLimit\n");
        for (ProcessControlChartEntry entry : this.months){
            if (entry.getCommitsNum() >= this.threshold) {
                DateFormat pattern = new SimpleDateFormat("yyyy-MM");
                bld.append(pattern.format(entry.date)).append(",")
                        .append(entry.fixedBugs.toString()).append(",")
                        .append(entry.totalCommits).append(",")
                        .append(this.mean.toString()).append(",")
                        .append(this.upperControlLimit.toString()).append(",")
                        .append(this.lowerControlLimit.toString()).append("\n");
            }
        }
        return bld.toString();
    }
}
