package nagini.client.command;

import java.io.IOException;
import java.io.PrintStream;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import nagini.client.NaginiClient;

/**
 * Implements all stop commands.
 */
public class NaginiCommandStop extends AbstractCommand {

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
            SubCommandStopApp.executeCommand(args);
        } else {
            printHelp(System.out);
        }
    }

    /**
     * Prints command-line help menu.
     * */
    public static void printHelp(PrintStream stream) {
        stream.println();
        stream.println("Nagini Stop Commands");
        stream.println("--------------------");
        stream.println("app         Stop all application nodes on remote hosts.");
        stream.println();
        stream.println("To get more information on each command,");
        stream.println("please try \'help stop <command-name>\'.");
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
            SubCommandStopApp.printHelp(stream);
        } else {
            printHelp(stream);
        }
    }

    /**
     * stop application command
     */
    public static class SubCommandStopApp extends AbstractCommand {

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
            stream.println("  stop app - Stop all application nodes on remote hosts");
            stream.println();
            stream.println("SYNOPSIS");
            stream.println("  stop app --config <config-path>");
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
        public static void executeCommand(String[] args) throws IOException {

            OptionParser parser = getParser();

            // declare parameters
            String configPath = null;

            // parse command-line input
            OptionSet options = parser.parse(args);
            if(options.has(ParserUtils.OPT_HELP)) {
                printHelp(System.out);
                return;
            }

            // check required options and/or conflicting options
            ParserUtils.checkRequired(options, ParserUtils.OPT_CONFIG);

            // load parameters
            configPath = (String) options.valueOf(ParserUtils.OPT_CONFIG);

            // execute command
            new NaginiClient(configPath).serviceOps.stopApplicationAllNodes();
        }
    }
}
