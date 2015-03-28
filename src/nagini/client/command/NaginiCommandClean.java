package nagini.client.command;

import java.io.IOException;
import java.io.PrintStream;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import nagini.client.NaginiClient;

/**
 * Implements all clean commands.
 */
public class NaginiCommandClean extends AbstractCommand {

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
            SubCommandCleanApp.executeCommand(args);
        } else if(subCmd.equals("config")) {
            SubCommandCleanConfig.executeCommand(args);
        } else {
            printHelp(System.out);
        }
    }

    /**
     * Prints command-line help menu.
     * */
    public static void printHelp(PrintStream stream) {
        stream.println();
        stream.println("Nagini Clean Commands");
        stream.println("---------------------");
        stream.println("app          Clean up applicaiton on all remote hosts.");
        stream.println("config       Clean up config on all remote hosts.");
        stream.println();
        stream.println("To get more information on each command,");
        stream.println("please try \'help clean <command-name>\'.");
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
            SubCommandCleanApp.printHelp(stream);
        } else if(subCmd.equals("config")) {
            SubCommandCleanConfig.printHelp(stream);
        } else {
            printHelp(stream);
        }
    }

    /**
     * clean application command
     */
    public static class SubCommandCleanApp extends AbstractCommand {

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
            stream.println("  clean app - Clean up application on all remote hosts");
            stream.println();
            stream.println("SYNOPSIS");
            stream.println("  clean app --config <config-path>");
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
            NaginiClient naginiClient = new NaginiClient(configPath);
            naginiClient.serviceOps.stopApplicationAllNodes();
            naginiClient.fileOps.deleteAllHosts(naginiClient.config.server.getApplicationPath());
        }
    }

    /**
     * clean config command
     */
    public static class SubCommandCleanConfig extends AbstractCommand {

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
            stream.println("  clean config - Clean up config on all remote servers");
            stream.println();
            stream.println("SYNOPSIS");
            stream.println("  clean config --config <config-path>");
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
            NaginiClient naginiClient = new NaginiClient(configPath);
            naginiClient.serviceOps.stopApplicationAllNodes();
            naginiClient.fileOps.deleteAllHosts(naginiClient.config.server.getConfigPath());
        }
    }
}
