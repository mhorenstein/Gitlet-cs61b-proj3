package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import static gitlet.Gitlet.*;
import static gitlet.Utils.*;

/** the stage area of the gitlet.
 *  @author Henry Xu
 */
public class Stage implements Serializable {

    /** staged files. i.e. files to be added.*/
    private HashMap<String, byte[]> _stagedFiles;
    /** marked files. i.e. files marked to be removed.*/
    private ArrayList<String> _markedFiles;
    /** the head of the commit tree.*/
    private Commit _head;

    /** generate an empty stage.*/
    public Stage() {
        _stagedFiles = new HashMap<String, byte[]>();
        _markedFiles = new ArrayList<String>();
    }

    /** clear the stage. clear staged files and marked files.*/
    public void clear() {
        _stagedFiles.clear();
        _markedFiles.clear();
    }

    /** set head to the given commit.
     * @param commit the given commit*/
    public void setHead(Commit commit) {
        _head = commit;
    }

    /** execute the add command. staged the given file.
     * @param fileName the name of the file to be staged.*/
    public void add(String fileName) {

        File fcurrent = new File(fileName);
        if (!fcurrent.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        byte[] current = Utils.readContents(fcurrent);
        String currentSHA = Utils.sha1(current);

        String previousSHA = _head.getFileSHA(fileName);

        if (previousSHA == null) {
            _stagedFiles.put(fileName, current);
        } else {
            if (!currentSHA.equals(previousSHA)) {
                _stagedFiles.put(fileName, current);
            } else {
                _stagedFiles.remove(fileName);
            }
        }
        _markedFiles.remove(fileName);
    }

    /** execute the commit command.
     *  make a new commit with the staged files and marked files.
     *  @param message the commit message
     *  @return the new commit.*/
    public Commit commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return null;
        }
        if (_stagedFiles.isEmpty() && _markedFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return null;
        }
        HashMap<String, String> blobMap = new HashMap<String, String>();
        for (String key: _head.getBlobs().keySet()) {
            blobMap.put(key, _head.getFileSHA(key));
        }
        for (String key : _stagedFiles.keySet()) {
            byte[] blob = _stagedFiles.get(key);
            String sha = Utils.sha1(blob);
            blobMap.put(key, sha);
            saveBlob(blob, sha);
        }
        for (String key: _markedFiles) {
            blobMap.remove(key);
        }
        String sha = Utils.sha1(Utils.serialize(_head));
        clear();

        Commit newCommit = new Commit(message, new Date(), sha, blobMap);
        _head = newCommit;
        return newCommit;
    }

    /** get the merged version commit.
     *  make a new commit with the staged files and marked files.
     *  @param message the commit message
     *  @param parent1 parent1.
     *  @param parent2 parent2.
     *  @return the new commit.*/
    public Commit mergeCommit(String message, String parent1, String parent2) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return null;
        }
        if (_stagedFiles.isEmpty() && _markedFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return null;
        }
        HashMap<String, String> blobMap = new HashMap<String, String>();
        for (String key: _head.getBlobs().keySet()) {
            blobMap.put(key, _head.getFileSHA(key));
        }
        for (String key : _stagedFiles.keySet()) {
            byte[] blob = _stagedFiles.get(key);
            String sha = Utils.sha1(blob);
            blobMap.put(key, sha);
            saveBlob(blob, sha);
        }
        for (String key: _markedFiles) {
            blobMap.remove(key);
        }
        String sha = Utils.sha1(Utils.serialize(_head));
        clear();

        Commit newCommit = new Commit(message, new Date(), sha, blobMap);
        newCommit.setMergedParent1(parent1);
        newCommit.setMergedParent2(parent2);
        _head = newCommit;
        return newCommit;
    }

    /** execute the rm command.
     * remove the given file.
     * @param fileName the name of the file to be removed.*/
    public void rm(String fileName) {
        if (_stagedFiles.remove(fileName) == null
                && _head.getFileSHA(fileName) == null) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (_head.getFileSHA(fileName) != null) {
            _markedFiles.add(fileName);
            Utils.restrictedDelete(fileName);
        }
    }

    /** save the given blob.
     * @param blob the contents of this blob
     * @param sha the sha of this blob*/
    public void saveBlob(byte[] blob, String sha) {
        File f = new File(BLOBS_DIR + sha);
        Utils.writeContents(f, blob);
    }

    /** @return if the stage is clear.*/
    public boolean isClear() {
        return _markedFiles.isEmpty() && _stagedFiles.isEmpty();
    }
    /** @return staged files.*/
    public HashMap<String, byte[]> getStagedFiles() {
        return _stagedFiles;
    }

    /** @return marked files.*/
    public ArrayList<String> getMarkedFiles() {
        return _markedFiles;
    }
}
