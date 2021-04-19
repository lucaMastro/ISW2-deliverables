package logic.dataset_manager;
import logic.config_manager.ConfigurationManager;
import logic.exception.InvalidRangeException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/* This class may be develop as a singleton */
public class JgitManager {

    private static JgitManager instance = null;
    private Repository repository;
    private ArrayList<RevCommit> commits;
    private ArrayList<RevCommit> tags;

    public static JgitManager getInstance() throws IOException {
        if (JgitManager.instance == null){
            JgitManager.instance = new JgitManager();
        }
        return JgitManager.instance;
    }

    private JgitManager() throws IOException{
        String path = ConfigurationManager.getConfigEntry("repositoryPath") + "/.git";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.repository = builder.setGitDir(new File(path)).readEnvironment().findGitDir().build();
        this.initializeReleaseList();
        this.initializeCommitList();

    }

    private void initializeReleaseList() {
        try (RevWalk walk = new RevWalk(this.repository)) {
            List<Ref> tagList = new Git(repository).tagList().call();

            this.tags = new ArrayList<RevCommit>();
            Integer i;
            for (i = 0; i < tagList.size(); i++){
                RevCommit cur = walk.parseCommit(tagList.get(i).getObjectId());
                this.tags.add(cur);
            }

            Collections.sort(this.tags, (RevCommit o1, RevCommit o2) ->{
                Date  d1 = o1.getAuthorIdent().getWhen();
                Date d2 = o2.getAuthorIdent().getWhen();
                return d1.compareTo(d2);
            });

        }catch (Exception e){
            Logger logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

    private void initializeCommitList() {
        this.commits = new ArrayList<RevCommit>();

        try (RevWalk walk = new RevWalk(this.repository)) {
            walk.sort(RevSort.REVERSE);
            Iterable<RevCommit> l = new Git(this.repository).log().call();
            l.forEach(this.commits::add);
            Collections.sort(this.commits, (RevCommit o1, RevCommit o2) -> {
                Date d1 = o1.getAuthorIdent().getWhen();
                Date d2 = o2.getAuthorIdent().getWhen();
                return d1.compareTo(d2);
            });
        } catch (GitAPIException e) {
            Logger logger = Logger.getLogger(JgitManager.class.getName());
            logger.log(Level.OFF, Arrays.toString(e.getStackTrace()));
        }
    }

    private RevCommit getTag(Integer index){
        return this.tags.get(index);
    }

    private RevCommit getCommit(Integer index){
        return this.commits.get(index);
    }

    public ArrayList<RevCommit> retrieveCommitsBeetwenReleases(Integer endIndexRelease)
            throws InvalidRangeException {

        /* this.retrieveCommits(2) should return a list of all commits performed between release1 and release 2
        * That's why, if startIndexRelease is less than 1 an exception is thrown */

        if (endIndexRelease < 1)
            throw new InvalidRangeException("endIndexRelease should be greater than 0");

        ArrayList<RevCommit> comList = new ArrayList<>();
        // The release 1 is stored in ArrayList.get(0)
        endIndexRelease--;
        Integer startIndexRelease = endIndexRelease - 1;
        Date startDate;
        if (startIndexRelease >= 0)
            startDate = this.getTag(startIndexRelease).getAuthorIdent().getWhen();
        else // looking for commits of first release
            startDate = this.getCommit(0).getAuthorIdent().getWhen();
        Date endDate = this.getTag(endIndexRelease).getAuthorIdent().getWhen();

        for (RevCommit currCommit : this.commits){
           Date currDate = currCommit.getAuthorIdent().getWhen();
           // checking if currDate is in a correct interval:
           if (startDate.compareTo(currDate) <= 0
                   && currDate.compareTo(endDate) < 0)
               comList.add(currCommit);
        }
        return comList;
    }

    public Repository getRepository() {
        return repository;
    }

    public ArrayList<RevCommit> getCommits() {
        return commits;
    }

    public ArrayList<RevCommit> getTags() {
        return tags;
    }

    public static void main(String[] args) throws IOException, InvalidRangeException {
        JgitManager manager = new JgitManager();
        Date d1 = manager.getTag(0).getAuthorIdent().getWhen();
        Date d2 = manager.getTag(1).getAuthorIdent().getWhen();
        Date d3 = manager.getCommit(0).getAuthorIdent().getWhen();
        Date d4 = manager.getCommit(1).getAuthorIdent().getWhen();
        ArrayList<RevCommit> list = manager.retrieveCommitsBeetwenReleases(1);
        System.out.println(manager.commits.get(0).getName());
        Git g = new Git(manager.repository);
        try (TreeWalk tw = new TreeWalk(manager.repository) ){
            tw.setRecursive(Boolean.TRUE);
            RevCommit c = manager.commits.get(1);
            System.out.println(c.getName() + "\n");
            tw.reset(c.getTree().getId());
            while (tw.next()){
                System.out.println(tw.getPathString());
            }
            System.out.println("\nsecond\n");
            c = manager.commits.get(2);
            System.out.println(c.getName() + "\n");
            tw.reset(c.getTree().getId());
            while (tw.next()){
                System.out.println(tw.getPathString());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
