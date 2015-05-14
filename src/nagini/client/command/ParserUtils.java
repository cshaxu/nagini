package nagini.client.command;

import java.util.Arrays;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import com.google.common.collect.Lists;

/**
 * Parser utility class for AdminCommand
 */
public class ParserUtils {

    public static void printArgs(String[] args) {
        System.out.println("Arguments Accepted");
        for(String arg: args) {
            System.out.println(arg);
        }
    }

    // options without argument
    public static final String OPT_ALL_NODES = "all-nodes";
    public static final String OPT_CONFIRM = "confirm";
    public static final String OPT_H = "h";
    public static final String OPT_HELP = "help";

    // options with one argument
    public static final String OPT_CONFIG = "config";
    public static final String OPT_LOCAL_PATH = "local-path";
    public static final String OPT_REMOTE_PATH = "remote-path";
    public static final String OPT_REMOTE_NODE_PATH = "remote-node-path";
    public static final String OPT_U = "u";
    public static final String OPT_URL = "url";

    // options with multiple arguments
    // none

    // options that have exactly one argument or multiple arguments
    public static final String OPT_N = "n";
    public static final String OPT_NODE = "node";

    // defined argument strings
    // none

    /**
     * Adds OPT_ALL_NODES option to OptionParser, without argument.
     * 
     * @param parser OptionParser to be modified
     * @param required Tells if this option is required or optional
     */
    public static void acceptsAllNodes(OptionParser parser) {
        parser.accepts(OPT_ALL_NODES, "select all nodes");
    }

    /**
     * Adds OPT_CONFIRM option to OptionParser, without argument.
     * 
     * @param parser OptionParser to be modified
     */
    public static void acceptsConfirm(OptionParser parser) {
        parser.accepts(OPT_CONFIRM, "confirm dangerous operation");
    }

    /**
     * Adds OPT_H | OPT_HELP option to OptionParser, without argument.
     * 
     * @param parser OptionParser to be modified
     */
    public static void acceptsHelp(OptionParser parser) {
        parser.acceptsAll(Arrays.asList(OPT_H, OPT_HELP), "show help menu");
    }

    /**
     * Adds OPT_CONFIG option to OptionParser, with one argument.
     * 
     * @param parser OptionParser to be modified
     */
    public static void acceptsConfig(OptionParser parser) {
        parser.accepts(OPT_CONFIG, "folder path that contains config files")
              .withRequiredArg()
              .describedAs("folder-path")
              .ofType(String.class);
    }

    /**
     * Adds OPT_LOCAL_PATH option to OptionParser, with one argument.
     * 
     * @param parser OptionParser to be modified
     */
    public static void acceptsLocalPath(OptionParser parser) {
        parser.accepts(OPT_LOCAL_PATH, "local file/folder path")
              .withRequiredArg()
              .describedAs("file-path")
              .ofType(String.class);
    }

    /**
     * Adds OPT_REMOTE_PATH option to OptionParser, with one argument.
     * 
     * @param parser OptionParser to be modified
     */
    public static void acceptsRemotePath(OptionParser parser) {
        parser.accepts(OPT_REMOTE_PATH, "remote file/folder path")
              .withRequiredArg()
              .describedAs("file-path")
              .ofType(String.class);
    }

    /**
     * Adds OPT_REMOTE_NODE_PATH option to OptionParser, with one argument.
     * 
     * @param parser OptionParser to be modified
     */
    public static void acceptsRemoteNodePath(OptionParser parser) {
        parser.accepts(OPT_REMOTE_NODE_PATH, "remote file/folder relative path to node base")
              .withRequiredArg()
              .describedAs("file-rel-path")
              .ofType(String.class);
    }

    /**
     * Adds OPT_U | OPT_URL option to OptionParser, with one argument.
     * 
     * @param parser OptionParser to be modified
     */
    public static void acceptsUrl(OptionParser parser) {
        parser.acceptsAll(Arrays.asList(OPT_U, OPT_URL), "bootstrap url")
              .withRequiredArg()
              .describedAs("url")
              .ofType(String.class);
    }

    /**
     * Adds OPT_N | OPT_NODE option to OptionParser, with multiple arguments.
     * 
     * @param parser OptionParser to be modified
     * @param required Tells if this option is required or optional
     */
    public static void acceptsNodeMultiple(OptionParser parser) {
        parser.acceptsAll(Arrays.asList(OPT_N, OPT_NODE), "node id list")
              .withRequiredArg()
              .describedAs("node-id-list")
              .withValuesSeparatedBy(',')
              .ofType(Integer.class);
    }

    /**
     * Checks if the required option exists.
     * 
     * @param options OptionSet to checked
     * @param opt Required option to check
     * @throws RuntimeException
     */
    public static void checkRequired(OptionSet options, String opt) throws RuntimeException {
        List<String> opts = Lists.newArrayList();
        opts.add(opt);
        checkRequired(options, opts);
    }

    /**
     * Checks if there's exactly one option that exists among all possible opts.
     * 
     * @param options OptionSet to checked
     * @param opt1 Possible required option to check
     * @param opt2 Possible required option to check
     */
    public static void checkRequired(OptionSet options, String opt1, String opt2) {
        List<String> opts = Lists.newArrayList();
        opts.add(opt1);
        opts.add(opt2);
        checkRequired(options, opts);
    }

    /**
     * Checks if there's exactly one option that exists among all possible opts.
     * 
     * @param options OptionSet to checked
     * @param opt1 Possible required option to check
     * @param opt2 Possible required option to check
     * @param opt3 Possible required option to check
     */
    public static void checkRequired(OptionSet options, String opt1, String opt2, String opt3) {
        List<String> opts = Lists.newArrayList();
        opts.add(opt1);
        opts.add(opt2);
        opts.add(opt3);
        checkRequired(options, opts);
    }

    /**
     * Checks if there's exactly one option that exists among all opts.
     * 
     * @param options OptionSet to checked
     * @param opts List of options to be checked
     * @throws RuntimeException
     */
    public static void checkRequired(OptionSet options, List<String> opts) throws RuntimeException {
        List<String> optCopy = Lists.newArrayList();
        for(String opt: opts) {
            if(options.has(opt)) {
                optCopy.add(opt);
            }
        }
        if(optCopy.size() < 1) {
            System.err.println("Please specify one of the following options:");
            for(String opt: opts) {
                System.err.println("--" + opt);
            }
            throw new RuntimeException("Missing required option.");
        }
        if(optCopy.size() > 1) {
            System.err.println("Conflicting options:");
            for(String opt: optCopy) {
                System.err.println("--" + opt);
            }
            throw new RuntimeException("Conflicting options detected.");
        }
    }

    /**
     * Checks if there's at most one option that exists among all opts.
     * 
     * @param parser OptionParser to checked
     * @param opt1 Optional option to check
     * @param opt2 Optional option to check
     */
    public static void checkOptional(OptionSet options, String opt1, String opt2) {
        List<String> opts = Lists.newArrayList();
        opts.add(opt1);
        opts.add(opt2);
        checkOptional(options, opts);
    }

    /**
     * Checks if there's at most one option that exists among all opts.
     * 
     * @param parser OptionParser to checked
     * @param opt1 Optional option to check
     * @param opt2 Optional option to check
     * @param opt3 Optional option to check
     */
    public static void checkOptional(OptionSet options, String opt1, String opt2, String opt3) {
        List<String> opts = Lists.newArrayList();
        opts.add(opt1);
        opts.add(opt2);
        opts.add(opt3);
        checkOptional(options, opts);
    }

    /**
     * Checks if there's at most one option that exists among all opts.
     * 
     * @param parser OptionParser to checked
     * @param opts List of options to be checked
     * @throws RuntimeException
     */
    public static void checkOptional(OptionSet options, List<String> opts) throws RuntimeException {
        List<String> optCopy = Lists.newArrayList();
        for(String opt: opts) {
            if(options.has(opt)) {
                optCopy.add(opt);
            }
        }
        if(optCopy.size() > 1) {
            System.err.println("Conflicting options:");
            for(String opt: optCopy) {
                System.err.println("--" + opt);
            }
            throw new RuntimeException("Conflicting options detected.");
        }
    }
}
