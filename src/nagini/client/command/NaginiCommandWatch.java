package nagini.client.command;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import nagini.client.NaginiClient;

import com.google.common.collect.Sets;

/**
 * Implements all watch commands.
 */
public class NaginiCommandWatch extends AbstractCommand {

    /**
     * Parses command-line and directs to sub-commands.
     * 
     * @param args Command-line input
     * @throws Exception
     */
    public static void executeCommand(String[] args) throws Exception {
        String subCmd = (args.length > 0) ? args[0] : "";
        args = CommandUtils.copyArrayCutFirst(args);
        if(subCmd.equals("app")) {
            SubCommandWatchApplication.executeCommand(args);
        } else {
            printHelp(System.out);
        }
    }

    /**
     * Prints command-line help menu.
     * */
    public static void printHelp(PrintStream stream) {
        stream.println();
        stream.println("Nagini Watch Commands");
        stream.println("---------------------");
        stream.println("app          Watch all application node output on remote hosts.");
        stream.println();
        stream.println("To get more information on each command,");
        stream.println("please try \'help watch <command-name>\'.");
        stream.println();
    }

    /**
     * Parses command-line input and prints help menu.
     * 
     * @throws Exception
     */
    public static void executeHelp(String[] args, PrintStream stream) throws Exception {
        String subCmd = (args.length > 0) ? args[0] : "";
        if(subCmd.equals("app")) {
            SubCommandWatchApplication.printHelp(stream);
        } else {
            printHelp(stream);
        }
    }

    private static final String OPT_I = "i";
    private static final String OPT_INTERVAL = "interval";
    private static final String OPT_T = "t";
    private static final String OPT_TAIL = "tail";

    /**
     * watch application command
     */
    public static class SubCommandWatchApplication extends AbstractCommand {

        /**
         * Initializes parser
         * 
         * @return OptionParser object with all available options
         */
        protected static OptionParser getParser() {
            OptionParser parser = new OptionParser();
            // help options
            ParserUtils.acceptsHelp(parser);
            // required options
            ParserUtils.acceptsConfig(parser);
            parser.acceptsAll(Arrays.asList(OPT_I, OPT_INTERVAL),
                              "interval seconds to print output")
                  .withRequiredArg()
                  .describedAs("second")
                  .ofType(Integer.class);
            // optional options
            ParserUtils.acceptsAllNodes(parser);
            ParserUtils.acceptsNodeMultiple(parser);
            parser.acceptsAll(Arrays.asList(OPT_T, OPT_TAIL),
                              "number of tail lines to print output")
                  .withRequiredArg()
                  .describedAs("line-number")
                  .ofType(Integer.class);
            return parser;
        }

        /**
         * Prints help menu for command.
         * 
         * @param stream PrintStream object for output
         * @throws IOException
         */
        public static void printHelp(PrintStream stream) throws IOException {
            stream.println();
            stream.println("NAME");
            stream.println("  watch app - Watch all application node output on remote hosts");
            stream.println();
            stream.println("SYNOPSIS");
            stream.println("  watch app --config <config-path> --interval <second>");
            stream.println("            [-n <node-id-list> | --all-nodes] [--tail <line-number>]");
            stream.println();
            getParser().printHelpOn(stream);
            stream.println();
        }

        /**
         * Parses command-line and executes command.
         * 
         * @param args Command-line input
         * @param printHelp Tells whether to print help only or execute command
         *        actually
         * @throws IOException
         * 
         */
        @SuppressWarnings("unchecked")
        public static void executeCommand(String[] args) throws IOException {

            OptionParser parser = getParser();

            // declare parameters
            String configPath = null;
            Boolean allNodes = true;
            List<Integer> nodeIds = null;
            Integer interval = 0;
            Integer tail = 0;

            // parse command-line input
            OptionSet options = parser.parse(args);
            if(options.has(ParserUtils.OPT_HELP)) {
                printHelp(System.out);
                return;
            }

            // check required options and/or conflicting options
            ParserUtils.checkRequired(options, ParserUtils.OPT_CONFIG);
            ParserUtils.checkOptional(options, ParserUtils.OPT_NODE, ParserUtils.OPT_ALL_NODES);
            ParserUtils.checkRequired(options, OPT_INTERVAL);

            // load parameters
            configPath = (String) options.valueOf(ParserUtils.OPT_CONFIG);
            if(options.has(ParserUtils.OPT_NODE)) {
                nodeIds = (List<Integer>) options.valuesOf(ParserUtils.OPT_NODE);
                allNodes = false;
            }
            interval = (Integer) options.valueOf(OPT_INTERVAL);
            if(options.has(OPT_TAIL)) {
                tail = (Integer) options.valueOf(OPT_TAIL);
            }

            // execute command
            NaginiClient naginiClient = new NaginiClient(configPath);
            if(allNodes) {
                naginiClient.serviceOps.watchApplicationAllNodes(interval, tail);
            } else {
                Set<Integer> nodeIdSet = Sets.newHashSet();
                for(Integer nodeId: nodeIds) {
                    nodeIdSet.add(nodeId);
                }
                naginiClient.serviceOps.watchApplicationMultipleNodes(nodeIdSet, interval, tail);
            }
        }
    }
}
