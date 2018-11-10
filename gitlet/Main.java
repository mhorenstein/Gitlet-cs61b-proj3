package gitlet;

import java.io.File;
import static gitlet.Gitlet.*;
import static gitlet.Utils.*;
/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Henry Xu
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    static final String GITLET_DIR = ".gitlet/";
    /** the path of gitlet.*/
    static final String GITLET_PATH = ".gitlet/gitlet";
    /** the path of all commits.*/
    static final String COMMITS_DIR = ".gitlet/commits/";
    /** the path of all blobs.*/
    static final String BLOBS_DIR = ".gitlet/blobs/";

    /** initialize the program and make directory.*/
    public static void initCommand() {
        File fgitlet = new File(GITLET_DIR);
        if (fgitlet.exists()) {
            System.out.println("A Gitlet version-control system already"
                    + " exists in the current directory.");
            return;
        }
        fgitlet.mkdirs();
        File fcommits = new File(COMMITS_DIR);
        fcommits.mkdirs();
        File fblobs = new File(BLOBS_DIR);
        fblobs.mkdirs();
        Gitlet gitlet = new Gitlet();
        saveGitlet(gitlet);
    }

    /** save the situation after running a command.
     * @param gitlet current gitlet to be save*/
    public static void saveGitlet(Gitlet gitlet) {
        File f = new File(GITLET_PATH);
        writeObject(f, gitlet);
    }

    /** main method.
     * @param args the command user put in*/
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String command = args[0];
        if (command.equals("init")) {
            initCommand();
        } else {
            File f = new File(GITLET_PATH);
            if (!f.exists()) {
                System.out.println("Not in an initialized"
                        + " Gitlet directory.");
                return;
            }
            Gitlet gitlet = Utils.readObject(f, Gitlet.class);
            switch (command) {
            case "add":
                addComand(gitlet, args); break;
            case "commit":
                commitComand(gitlet, args); break;
            case "rm":
                rmComand(gitlet, args); break;
            case "log":
                logCommand(gitlet, args); break;
            case "global-log":
                globalLogCommand(gitlet, args); break;
            case "find":
                findCommand(gitlet, args); break;
            case "status":
                statusCommand(gitlet, args); break;
            case "checkout":
                checkoutCommand(gitlet, args); break;
            case "branch":
                branchCommand(gitlet, args); break;
            case "rm-branch":
                rmBranchCommand(gitlet, args); break;
            case "reset":
                resetCommand(gitlet, args); break;
            case "merge":
                mergeCommand(gitlet, args); break;
            default:
                System.out.println("No command with that name exists.");
                break;
            }
            saveGitlet(gitlet);
        }
    }

    /** add command method.
     * @param args the command user put in
     * @param gitlet current gitlet*/
    static void addComand(Gitlet gitlet, String...args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        gitlet.add(args[1]);
    }

    /** commit command method.
     * @param args the command user put in
     * @param gitlet current gitlet*/
    static void commitComand(Gitlet gitlet, String...args) {
        if (args.length == 1) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (args.length > 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        gitlet.commit(args[1]);
    }
    /** rm command method.
     * @param args the command user put in
     * @param gitlet current gitlet*/
    static void rmComand(Gitlet gitlet, String...args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        gitlet.rm(args[1]);
    }
    /** log command method.
     * @param args the command user put in
     * @param gitlet current gitlet*/
    static void logCommand(Gitlet gitlet, String...args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        gitlet.log();
    }
    /** global-log command method.
     * @param args the command user put in
     * @param gitlet current gitlet*/
    static void globalLogCommand(Gitlet gitlet, String...args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        gitlet.globalLog();
    }
    /** find command method.
     * @param args the command user put in
     * @param gitlet current gitlet*/
    static void findCommand(Gitlet gitlet, String...args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        gitlet.find(args[1]);
    }
    /** status command method.
     * @param args the command user put in
     * @param gitlet current gitlet*/
    static void statusCommand(Gitlet gitlet, String...args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        gitlet.status();
    }

    /** branch command method.
     * @param args the command user put in
     * @param gitlet current gitlet*/
    static void branchCommand(Gitlet gitlet, String...args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        gitlet.branch(args[1]);
    }
    /** checkout command method.
     * @param args the command user put in.
     * @param gitlet current gitlet.*/
    static void checkoutCommand(Gitlet gitlet, String...args) {
        if (args.length == 2) {
            gitlet.checkoutBranch(args[1]);
        } else if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.checkout(args[2]);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.checkout(args[1], args[3]);
        } else {
            System.out.println("Incorrect operands.");
        }
    }
    /** rm-branch command method.
     * @param args the command user put in
     * @param gitlet current gitlet*/
    static void rmBranchCommand(Gitlet gitlet, String...args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        gitlet.rmBranch(args[1]);
    }
    /** reset command method.
     * @param args the command user put in
     * @param gitlet current gitlet*/
    static void resetCommand(Gitlet gitlet, String...args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        gitlet.reset(args[1]);
    }
    /** merge command method.
     * @param args the command user put in
     * @param gitlet current gitlet*/
    static void mergeCommand(Gitlet gitlet, String...args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        gitlet.merge(args[1]);
    }
}
