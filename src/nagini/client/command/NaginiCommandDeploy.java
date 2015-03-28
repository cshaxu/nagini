package nagini.client.command;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import nagini.client.NaginiClient;
import nagini.utils.NaginiFileUtils;
import nagini.utils.NaginiGitUtils;
import nagini.utils.process.NaginiProcessUtils;

/**
 * Implements all deploy commands.
 */
public class NaginiCommandDeploy extends AbstractCommand {

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
            SubCommandDeployApp.executeCommand(args);
        } else if(subCmd.equals("config")) {
            SubCommandDeployConfig.executeCommand(args);
        } else {
            printHelp(System.out);
        }
    }

    /**
     * Prints command-line help menu.
     * */
    public static void printHelp(PrintStream stream) {
        stream.println();
        stream.println("Nagini Deploy Commands");
        stream.println("----------------------");
        stream.println("app           Download, compile and deploy application.");
        stream.println("config        Deploy configuration files for Nagini and application.");
        stream.println();
        stream.println("To get more information on each command,");
        stream.println("please try \'help deploy <command-name>\'.");
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
            SubCommandDeployApp.printHelp(stream);
        } else if(subCmd.equals("config")) {
            SubCommandDeployConfig.printHelp(stream);
        } else {
            printHelp(stream);
        }
    }

    /**
     * deploy application command
     */
    public static class SubCommandDeployApp extends AbstractCommand {

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
            stream.println("  deploy app - Download from git repo, compile and deploy application");
            stream.println();
            stream.println("SYNOPSIS");
            stream.println("  deploy app --config <config-path>");
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
         * @throws Exception
         * 
         */
        public static void executeCommand(String[] args) throws Exception {

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

            String tempPath = naginiClient.getClientTempPath();
            File tempFolder = new File(tempPath);
            if(tempFolder.exists()) {
                NaginiFileUtils.delete(tempFolder);
            }

            // download application from git repository
            NaginiGitUtils.clone(naginiClient.config.client.appGitRepoUri,
                                 naginiClient.config.client.appGitRepoBranch,
                                 tempPath);

            // compile application jars
            NaginiProcessUtils.command(Arrays.asList(naginiClient.config.client.appBuildCommand.split("\\s* \\s*")),
                                       tempFolder,
                                       System.out);

            // create application distributable
            String tempApplicationPath = System.getProperty("java.io.tmpdir") + File.separator
                                         + naginiClient.config.server.getApplicationName();
            File tempApplication = new File(tempApplicationPath);
            for(String subPath: naginiClient.config.client.appBuildOutputSubPaths) {
                NaginiFileUtils.copy(tempPath + File.separator + subPath, tempApplicationPath
                                                                          + File.separator
                                                                          + subPath);
            }

            // send application to remote servers
            naginiClient.fileOps.putAllHosts(tempApplicationPath,
                                             naginiClient.config.server.basePath);

            // clean up temp folders
            NaginiFileUtils.delete(tempApplication);
            NaginiFileUtils.delete(tempFolder);
        }
    }

    /**
     * deploy config command
     */
    public static class SubCommandDeployConfig extends AbstractCommand {

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
            stream.println("  deploy config - Deploy Nagini configuration folder");
            stream.println();
            stream.println("SYNOPSIS");
            stream.println("  deploy config --config <config-path>");
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
         * @throws Exception
         * 
         */
        public static void executeCommand(String[] args) throws Exception {

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
            naginiClient.fileOps.deleteAllHosts(naginiClient.config.server.getConfigPath());
            naginiClient.fileOps.putAllHosts(configPath, naginiClient.config.server.basePath);
            naginiClient.controlOps.reconfig(naginiClient.config.server.getConfigPath());
        }
    }
}
