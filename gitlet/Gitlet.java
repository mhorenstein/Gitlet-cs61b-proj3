package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.List;

/** Manager of the whole project.
 *  @author Henry Xu
 */
public class Gitlet implements Serializable {

    /** key: message, val: SHA of commit.*/
    private ArrayList<String> _commitMap;
    /** key: branch name, val: SHA of commit.*/
    private HashMap<String, String> _branchMap;
    /** stage.*/
    private Stage _stage;
    /** the SHA of current head commit.*/
    private String _head;
    /** the name of current branch.*/
    private String _branch;
    /** .gitlet path.*/
    static final String GITLET_DIR = ".gitlet/";
    /** the path of gitlet.*/
    static final String GITLET_PATH = ".gitlet/gitlet";
    /** the path of all commits.*/
    static final String COMMITS_DIR = ".gitlet/commits/";
    /** the path of all blobs.*/
    static final String BLOBS_DIR = ".gitlet/blobs/";

    /** make a initial gitlet.*/
    public Gitlet() {
        _commitMap = new ArrayList<String>();
        _branchMap = new HashMap<String, String>();
        _stage = new Stage();
        _branch = "master";

        Commit init = new Commit("initial commit", new Date(0),
                null, new HashMap<String, String>());
        String sha = Utils.sha1(Utils.serialize(init));
        saveCommit(init, sha);

        _head = sha;
        _stage.setHead(init);
        _commitMap.add(sha);
        _branchMap.put("master", sha);
    }

    /** save a commit.
     * @param commit the commit to be save.
     * @param sha the sha1 code of this commit.*/
    public void saveCommit(Commit commit, String sha) {
        File f = new File(COMMITS_DIR + sha);
        Utils.writeObject(f, commit);
    }

    /** add a file.
     * @param fileName name of this file.*/
    public void add(String fileName) {
        _stage.add(fileName);
    }

    /** make a commit.
     * @param message commit message.*/
    public void commit(String message) {
        Commit commit = _stage.commit(message);
        if (commit == null) {
            return;
        }
        String sha = Utils.sha1(Utils.serialize(commit));
        _commitMap.add(sha);
        saveCommit(commit, sha);
        _head = sha;
        _branchMap.put(_branch, _head);
    }

    /** make a commit.
     * @param message commit message.
     * @param parent1 parent1
     * @param parent2 parent2*/
    public void mergeCommit(String message, String parent1, String parent2) {
        Commit commit = _stage.mergeCommit(message, parent1, parent2);
        if (commit == null) {
            return;
        }
        String sha = Utils.sha1(Utils.serialize(commit));
        _commitMap.add(sha);
        saveCommit(commit, sha);
        _head = sha;
        _branchMap.put(_branch, _head);
    }

    /** remove a file.
     * @param fileName the name of this file*/
    public void rm(String fileName) {
        _stage.rm(fileName);
    }

    /** print the log of head.*/
    public void log() {
        String sha = _head;
        while (sha != null) {
            Commit commit = getCommit(sha);
            System.out.println("===");
            System.out.println("commit " + sha);
            if (commit.getMergedParent1() != null) {
                System.out.println("Merge: " + commit.getMergedParent1()
                        + " " + commit.getMergedParent2());
            }
            Date time = commit.getTime();
            System.out.println(String.format("Date: %1$ta %1$tb"
                    + " %1$te %1$tT %1$tY %1$tz", time));
            System.out.println(commit.getMessage());
            System.out.println();
            sha = commit.getParent();
        }
    }

    /** print the global log.*/
    public void globalLog() {
        for (String sha : _commitMap) {
            File f = new File(COMMITS_DIR + sha);
            Commit commit = Utils.readObject(f, Commit.class);
            System.out.println("===");
            System.out.println("commit " + sha);
            if (commit.getMergedParent1() != null) {
                System.out.println("Merge: " + commit.getMergedParent1()
                        + " " + commit.getMergedParent2());
            }
            Date time = commit.getTime();
            System.out.println(String.format("Date: %1$ta"
                    + " %1$tb %1$te %1$tT %1$tY %1$tz", time));
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    /** find commits with given message.
     * @param message message of the commit you want.*/
    public void find(String message) {
        boolean found = false;
        for (String sha : _commitMap) {
            File f = new File(COMMITS_DIR + sha);
            Commit commit = Utils.readObject(f, Commit.class);
            if (commit.getMessage().equals(message)) {
                System.out.println(sha);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** print branch status.*/
    public void statusBranch() {
        System.out.println("=== Branches ===");
        PriorityQueue<String> branches = new PriorityQueue<>();
        for (String key : _branchMap.keySet()) {
            branches.add(key);
        }
        while (!branches.isEmpty()) {
            String branch = branches.remove();
            if (branch.equals(this._branch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
    }

    /** print staged status.*/
    public void statusStaged() {
        System.out.println("=== Staged Files ===");
        PriorityQueue<String> stagedFiles = new PriorityQueue<>();
        for (String key : _stage.getStagedFiles().keySet()) {
            stagedFiles.add(key);
        }
        while (!stagedFiles.isEmpty()) {
            String fileName = stagedFiles.remove();
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** print removed status.*/
    public void statusRemoved() {
        System.out.println("=== Removed Files ===");
        Collections.sort(_stage.getMarkedFiles());
        for (String fileName : _stage.getMarkedFiles()) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** print modified status.*/
    public void statusModified() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        ArrayList<String> modified = new ArrayList<>();
        List<String> workingDirectoryFiles = Utils.plainFilenamesIn("./");
        Commit commit = getHead();
        HashMap<String, String> blobs = commit.getBlobs();
        for (String key: blobs.keySet()) {
            if (!workingDirectoryFiles.contains(key)
                    && !_stage.getMarkedFiles().contains(key)) {
                modified.add(key + " (deleted)");
            } else if (workingDirectoryFiles.contains(key)
                    && !_stage.getStagedFiles().containsKey(key)
                    && isModified(key, blobs.get(key))) {
                modified.add(key + " (modified)");
            }
        }
        Collections.sort(modified);
        for (String fileName : modified) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** get the commit with given sha1.
     * @param sha the sha of the commit you want.
     * @return the commit you want.*/
    public static Commit getCommit(String sha) {
        File f = new File(COMMITS_DIR + sha);
        return Utils.readObject(f, Commit.class);
    }

    /** get the head commit.
     * @return head commit.*/
    public Commit getHead() {
        return getCommit(_head);
    }

    /** get contents of the given file.
     * @param fileName name of the file.
     * @return contents of this file.*/
    public byte[] getContents(String fileName) {
        File f = new File(fileName);
        return Utils.readContents(f);
    }

    /** check if the file is modified.
     * @param fileName name of the file.
     * @param sha the sha of the file you want to compare.
     * @return ismodified.*/
    public boolean isModified(String fileName, String sha) {
        String sha1 = Utils.sha1(getContents(fileName));
        return !sha1.equals(sha);
    }

    /** print untracked status.*/
    public void statusUntracked() {
        System.out.println("=== Untracked Files ===");
        ArrayList<String> untracked = new ArrayList<>();
        List<String> workingDirectoryFiles = Utils.plainFilenamesIn("./");
        Commit commit = getHead();
        for (String fileName: workingDirectoryFiles) {
            if (!_stage.getStagedFiles().containsKey(fileName)
                    && !commit.getBlobs().containsKey(fileName)
                    && !_stage.getMarkedFiles().contains(fileName)) {
                untracked.add(fileName);
            }
        }
        Collections.sort(untracked);
        for (String fileName : untracked) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** print status.*/
    public void status() {
        statusBranch();
        statusStaged();
        statusRemoved();
        statusModified();
        statusUntracked();
    }

    /** fast checkout.
     * @param fileName the name of the file you want to checkout*/
    public void checkout(String fileName) {
        File fcommit = new File(COMMITS_DIR + _head);
        Commit commit = Utils.readObject(fcommit, Commit.class);
        String sha = commit.getFileSHA(fileName);
        if (sha == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File fblob = new File(BLOBS_DIR + sha);
        byte[] blob = Utils.readContents(fblob);
        File file = new File(fileName);
        Utils.writeContents(file, blob);
    }

    /** checkout a given commit.
     * @param commitID the id of the commit you want to checkout.
     * @param fileName the name of the file you want to checkout.*/
    public void checkout(String commitID, String fileName) {
        boolean isFind = false;
        for (String key: _commitMap) {
            if (key.substring(0, commitID.length()).equals(commitID)) {
                commitID = key;
                isFind = true;
                break;
            }
        }
        if (!isFind) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File fcommit = new File(COMMITS_DIR + commitID);
        Commit commit = Utils.readObject(fcommit, Commit.class);
        String sha = commit.getFileSHA(fileName);
        if (sha == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File fblob = new File(BLOBS_DIR + sha);
        byte[] blob = Utils.readContents(fblob);
        File file = new File(fileName);
        Utils.writeContents(file, blob);
    }

    /** checkout a branch.
     * @param branchName the name of the branch you want to checkout.*/
    public void checkoutBranch(String branchName) {
        String branch = _branchMap.get(branchName);
        if (branch == null) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branchName.equals(_branch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit commit = getCommit(branch);
        HashMap<String, String> branchBlobs = commit.getBlobs();

        Commit commitHead = getHead();
        HashMap<String, String> headBlobs = commitHead.getBlobs();
        List<String> workingDirectoryFiles = Utils.plainFilenamesIn("./");
        if (isUntracked(commit)) {
            return;
        }

        HashMap<String, String> blobs = commit.getBlobs();
        for (String key: blobs.keySet()) {
            String blobID = blobs.get(key);
            File fblob = new File(BLOBS_DIR + blobID);
            byte[] blob = Utils.readContents(fblob);
            File file = new File(key);
            Utils.writeContents(file, blob);
        }

        for (String fileName: workingDirectoryFiles) {
            if (!blobs.keySet().contains(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }
        _stage.clear();
        _stage.setHead(commit);
        _head = branch;
        _branch = branchName;
    }

    /** make a new branch.
     * @param branchName the name of the new branch*/
    public void branch(String branchName) {
        if (_branchMap.get(branchName) != null) {
            System.out.println("branch with that name already exists.");
            return;
        }
        _branchMap.put(branchName, _head);
    }

    /** remove a branch.
     * @param branchName the name of the branch to be remove*/
    public void rmBranch(String branchName) {
        if (branchName.equals(_branch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        if (_branchMap.remove(branchName) == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
    }

    /** reset to a commit.
     * @param commitID the id of the commit you want to checkout.*/
    public void reset(String commitID) {
        if (!_commitMap.contains(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit commit = getCommit(commitID);
        HashMap<String, String> branchBlobs = commit.getBlobs();

        Commit commitHead = getHead();
        HashMap<String, String> headBlobs = commitHead.getBlobs();
        List<String> workingDirectoryFiles = Utils.plainFilenamesIn("./");
        if (isUntracked(commit)) {
            return;
        }

        HashMap<String, String> blobs = commit.getBlobs();
        for (String key: blobs.keySet()) {
            String blobID = blobs.get(key);
            File fblob = new File(BLOBS_DIR + blobID);
            byte[] blob = Utils.readContents(fblob);
            File file = new File(key);
            Utils.writeContents(file, blob);
        }

        for (String fileName: workingDirectoryFiles) {
            if (branchBlobs.get(fileName) == null) {
                Utils.restrictedDelete(fileName);
            }
        }
        _stage.clear();
        _stage.setHead(commit);
        _head = commitID;
        _branchMap.put(_branch, commitID);
    }

    /** check if there is any file is untracked.
     * @param given the given commit.
     * @return if it is untracked.*/
    public boolean isUntracked(Commit given) {
        Commit current = getHead();
        HashMap headBlobs = current.getBlobs();
        HashMap givenBlobs = given.getBlobs();
        List<String> workingDirectoryFiles = Utils.plainFilenamesIn("./");
        for (String fileName: workingDirectoryFiles) {
            if (!headBlobs.containsKey(fileName)
                    && !_stage.getStagedFiles().containsKey(fileName)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                return true;
            }
        }
        return false;
    }

    /** collection of error message.
     * @param branchName the given branch.
     * @return if there is an error.*/
    public boolean isErrorMessage(String branchName) {
        if (!_stage.isClear()) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        if (!_branchMap.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return true;
        }
        if (branchName.equals(_branch)) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }
        Commit current = getHead();
        Commit given = getCommit(_branchMap.get(branchName));
        Commit splitPoint = splitPoint(current, given);

        if (given.equals(splitPoint)) {
            System.out.println("Given branch is an ancestor"
                    + " of the current branch.");
            return true;
        }
        if (current.equals(splitPoint)) {
            _head = given.getSHA();
            _branchMap.put(_branch, _head);
            System.out.println("Current branch fast-forwarded.");
            return true;
        }
        return false;
    }

    /** write the contents of given file into working directory.
     * @param fileName the file's name.
     * @param givenFileSHA the sha of the given file.*/
    public void write(String fileName, String givenFileSHA) {
        File file = new File(fileName);
        File givenFile = new File(BLOBS_DIR + givenFileSHA);
        Utils.writeContents(file, Utils.readContents(givenFile));
        add(fileName);
    }

    /** write the contents of conflict into working directory.
     * @param fileName the file's name.
     * @param givenFileSHA the sha of the given file.*/
    public void writeConflict1(String fileName, String givenFileSHA) {
        File workingFile = new File(fileName);
        File givenFile = new File(BLOBS_DIR + givenFileSHA);
        String givenContents = Utils.readContentsAsString(givenFile);
        Utils.writeContents(workingFile, "<<<<<<< HEAD"
                + System.lineSeparator()
                + "=======" + System.lineSeparator() + givenContents
                + ">>>>>>>" + System.lineSeparator());
        add(fileName);
    }

    /** write the contents of conflict into working directory.
     * @param fileName the file's name.
     * @param givenFileSHA the sha of the given file.
     * @param currFileSHA the sha of the current file.*/
    public void writeConflict2(String fileName,
                               String currFileSHA,
                               String givenFileSHA) {
        File workingFile = new File(fileName);
        File currFile = new File(BLOBS_DIR + currFileSHA);
        File givenFile = new File(BLOBS_DIR + givenFileSHA);
        String currContents = Utils.readContentsAsString(currFile);
        String givenContents = Utils.readContentsAsString(givenFile);
        Utils.writeContents(workingFile, "<<<<<<< HEAD\n"
                + currContents + "=======\n" + givenContents
                + ">>>>>>>\n");
        add(fileName);
    }

    /** write the contents of conflict into working directory.
     * @param fileName the file's name.
     * @param currFileSHA the sha of the given file.*/
    public void writeConflict3(String fileName, String currFileSHA) {
        File workingFile = new File(fileName);
        File currFile = new File(BLOBS_DIR + currFileSHA);
        String currContents = Utils.readContentsAsString(currFile);
        Utils.writeContents(workingFile, "<<<<<<< HEAD"
                + System.lineSeparator()
                + currContents + "=======" + System.lineSeparator()
                + ">>>>>>>\n");
        add(fileName);
    }

    /** just to make merge shorter....
     * @param givenFileSHA the sha of given branch.
     * @param splitFileSHA the sha of split point.
     * @param currFileSHA the sha of current branch.
     * @return if true.*/
    public boolean pred1(String givenFileSHA,
                         String splitFileSHA, String currFileSHA) {
        return !givenFileSHA.equals(splitFileSHA)
                && splitFileSHA.equals(currFileSHA);
    }

    /** just to make merge shorter....
     * @param givenFileSHA the sha of given branch.
     * @param splitFileSHA the sha of split point.
     * @param currFileSHA the sha of current branch.
     * @return if true.*/
    public boolean pred2(String givenFileSHA,
                         String splitFileSHA, String currFileSHA) {
        return !givenFileSHA.equals(splitFileSHA)
                && splitFileSHA.equals(currFileSHA);
    }

    /** just to make merge shorter....
     * @param givenFileSHA the sha of given branch.
     * @param splitFileSHA the sha of split point.
     * @param currFileSHA the sha of current branch.
     * @return if true.*/
    public boolean pred3(String givenFileSHA,
                         String splitFileSHA, String currFileSHA) {
        return !givenFileSHA.equals(splitFileSHA)
                && givenFileSHA.equals(currFileSHA);
    }

    /** just to make merge shorter....
     * @param givenFileSHA the sha of given branch.
     * @param splitFileSHA the sha of split point.
     * @param currFileSHA the sha of current branch.
     * @return if true.*/
    public boolean pred4(String givenFileSHA,
                         String splitFileSHA, String currFileSHA) {
        return !givenFileSHA.equals(currFileSHA)
                && !currFileSHA.equals(splitFileSHA)
                && !splitFileSHA.equals(givenFileSHA);
    }

    /** just to make merge shorter....
     * @param givenFileSHA the sha of given branch.
     * @param splitFileSHA the sha of split point.
     * @param currFileSHA the sha of current branch.
     * @return if true.*/
    public boolean pred5(String givenFileSHA,
                         String splitFileSHA, String currFileSHA) {
        return splitFileSHA != null && givenFileSHA == null
                && currFileSHA.equals(splitFileSHA);
    }
    /** merge the current branch with the given branch.
     * @param branchName the branch you want to merge.*/
    public void merge(String branchName) {
        if (isErrorMessage(branchName)) {
            return;
        }
        Commit given = getCommit(_branchMap.get(branchName));
        Commit splitPoint = splitPoint(getHead(), given);
        HashMap<String, String> currBlobs = getHead().getBlobs();
        HashMap<String, String> givenBlobs = given.getBlobs();
        HashMap<String, String> splitBlobs = splitPoint.getBlobs();
        if (isUntracked(given)) {
            return;
        }
        boolean isConflict = false;
        for (String fileName: givenBlobs.keySet()) {
            String currFileSHA = currBlobs.get(fileName);
            String givenFileSHA = givenBlobs.get(fileName);
            String splitFileSHA = splitBlobs.get(fileName);
            if (currFileSHA == null && splitFileSHA == null) {
                write(fileName, givenFileSHA);
                continue;
            }
            if (pred1(givenFileSHA, splitFileSHA, currFileSHA)) {
                writeConflict1(fileName, givenFileSHA);
                isConflict = true;
            }
            if (givenFileSHA.equals(splitFileSHA)) {
                continue;
            }
            if (pred2(givenFileSHA, splitFileSHA, currFileSHA)) {
                write(fileName, givenFileSHA);
                continue;
            }
            if (pred3(givenFileSHA, splitFileSHA, currFileSHA)) {
                continue;
            }
            if (pred4(givenFileSHA, splitFileSHA, currFileSHA)) {
                writeConflict2(fileName, currFileSHA, givenFileSHA);
                isConflict = true;
            }
        }
        for (String fileName: currBlobs.keySet()) {
            String currFileSHA = currBlobs.get(fileName);
            String givenFileSHA = givenBlobs.get(fileName);
            String splitFileSHA = splitBlobs.get(fileName);
            if (pred5(givenFileSHA, splitFileSHA, currFileSHA)) {
                rm(fileName);
            }
            if (splitFileSHA != null && givenFileSHA == null
                    && !currFileSHA.equals(splitFileSHA)) {
                writeConflict3(fileName, currFileSHA);
                isConflict = true;
            }
        }
        String parent1 = _branchMap.get(branchName).substring(0, 7);
        mergeCommit("Merged " + branchName + " into " + _branch + ".",
                parent1, _head.substring(0, 7));
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** get the split point of two commits.
     * @param current the current commit.
     * @param given the given commit.
     * @return the split point.*/
    public static Commit splitPoint(Commit current, Commit given) {
        /*if (given == null) {
            return current;
        }*/
        int len = current.length() - given.length();
        if (len < 0) {
            given = given.shorten(-len);
        } else {
            current = current.shorten(len);
        }
        while (!current.equals(given)) {
            current = getCommit(current.getParent());
            given = getCommit(given.getParent());
        }
        return given;
    }
}
