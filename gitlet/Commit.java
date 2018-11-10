package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import static gitlet.Gitlet.*;
import static gitlet.Utils.*;

/** Commit object.
 *  @author Henry Xu
 */
public class Commit implements Serializable {
    /** commit message.*/
    private String _message;
    /** commit time.*/
    private Date _time;
    /** SHA code of its parent commit.*/
    private String _parent;
    /** key: blob name, val: SHA of blob.*/
    private HashMap<String, String> _blobMap;
    /** parent1 merge.*/
    private String _mergedParent1 = null;
    /** parent2.*/
    private String _mergedParent2 = null;

    /** generate a new commit.
     * @param message commit message.
     * @param time commit time.
     * @param parent the sha of parent commit.
     * @param blobMap blobmap.*/
    public Commit(String message, Date time, String parent,
                  HashMap<String, String> blobMap) {
        this._message = message;
        this._time = time;
        this._parent = parent;
        this._blobMap = new HashMap<String, String>();
        for (String key : blobMap.keySet()) {
            this._blobMap.put(key, blobMap.get(key));
        }
    }

    /** get the distance between this commit and the init commit.
     * @return the distance.*/
    public int length() {
        if (_parent == null) {
            return 0;
        } else {
            return Gitlet.getCommit(_parent).length() + 1;
        }
    }

    /** get the LENth previous commit of this.
     * @param len the distance.
     * @return that previous commit.*/
    public Commit shorten(int len) {
        Commit result = this;
        for (int i = 0; i < len; i++) {
            result = Gitlet.getCommit(result.getParent());
        }
        return result;
    }

    /** get the sha of this commit.
     * @return sha.*/
    public String getSHA() {
        return Utils.sha1(Utils.serialize(this));
    }

    /** check if the sha of given commit is same as itself.
     * @param commit the commit you compare to.
     * @return is same.*/
    public boolean equals(Commit commit) {
        return this.getSHA().equals(commit.getSHA());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /** @return commit message.*/
    public String getMessage() {
        return _message;
    }

    /** @return the sha of parent commit.*/
    public String getParent() {
        return _parent;
    }

    /** @return the sha of merge parent1 commit.*/
    public String getMergedParent1() {
        return _mergedParent1;
    }

    /** @return the sha of merge parent2 commit.*/
    public String getMergedParent2() {
        return _mergedParent2;
    }

    /** set the sha of merge parent1 commit.
     * @param mergedParent1 parent1*/
    public void setMergedParent1(String mergedParent1) {
        this._mergedParent1 = mergedParent1;
    }

    /** set the sha of merge parent2 commit.
     * @param mergedParent2 parent2.*/
    public void setMergedParent2(String mergedParent2) {
        this._mergedParent2 = mergedParent2;
    }

    /** @return all blobs.*/
    public HashMap<String, String> getBlobs() {
        return _blobMap;
    }

    /** @return commit time.*/
    public Date getTime() {
        return _time;
    }

    /** get the sha of the given file.
     * @param fileName the name of given file.
     * @return the sha.*/
    public String getFileSHA(String fileName) {
        return _blobMap.get(fileName);
    }
}
