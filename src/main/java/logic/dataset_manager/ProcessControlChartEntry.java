package logic.dataset_manager;

import java.util.Date;

public class ProcessControlChartEntry {
    protected Date date;
    protected Integer fixedBugs;
    protected Integer totalCommits;

    public ProcessControlChartEntry(Date d){
        this.date = d;
        this.fixedBugs = 0;
        this.totalCommits = 0;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        return sb.append(this.date).append(",")
                .append(this.fixedBugs).append(",")
                .append(this.totalCommits).toString();
    }

    public Date getDate() {
        return date;
    }

    public void setNumberOfCommits(int n) {
        this.totalCommits += n;
    }

    public void setNumberOfFixCommits(int n) {
        this.fixedBugs += n;
    }

    public int getCommitsNum() {
        return this.totalCommits;
    }

    public int getFixedBugs() {
        return this.fixedBugs;
    }
}
