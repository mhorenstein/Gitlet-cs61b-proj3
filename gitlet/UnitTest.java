package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static gitlet.Gitlet.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
    }

    /** test the method of getting the length of a commit. */
    @Test
    public void lengthTest() {
        Commit commit1 = new Commit(null, null,
                null, new HashMap<String, String>());
        assertEquals(0, commit1.length());
    }
    /** test the method of getting the split point of 2 commits. */
    @Test
    public void splitPointTest() {
        Commit commit1 = new Commit(null, null,
                null, new HashMap<String, String>());
        Commit splitPoint = Gitlet.splitPoint(commit1, commit1);
        assertEquals(true, commit1.equals(splitPoint));
    }
    /** test the equals method of the commit class. */
    @Test
    public void commitEquals() {
        Commit commit1 = new Commit(null, null,
                null, new HashMap<String, String>());
        Commit commit2 = new Commit(null, null,
                null, new HashMap<String, String>());
        assertEquals(true, commit1.equals(commit2));
    }

}


