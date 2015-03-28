package nagini.client.command;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import nagini.client.NaginiClient;

/**
 * Implements all file commands.
 */
public class NaginiCommandFile extends AbstractCommand {

    /**
     * Parses command-line and directs to sub-commands.
     * 
     * @param args Command-line input
     * @throws Exception
     */
    public static void executeCommand(String[] args) throws Exception {
        String subCmd = (args.length > 0) ? args[0] : "";
        args = CommandUtils.copyArrayCutFirst(args);
        if(subCmd.equals("put")) {
            SubCommandFilePut.executeCommand(args);
        } else if(subCmd.equals("get")) {
            SubCommandFileGet.executeCommand(args);
        } else if(subCmd.equals("delete")) {
            SubCommandFileDelete.executeCommand(args);
        } else {
            printHelp(System.out);
        }
    }

    /**
     * Prints command-line help menu.
     * */
    public static void printHelp(PrintStream stream) {
        stream.println();
        stream.println("Nagini File Commands");
        stream.println("--------------------");
        stream.println("put         Put files to remote Nagini hosts.");
        stream.println("get         Get files from remote Nagini hosts.");
        stream.println("delete      Delete files on remote Nagini hosts.");
        stream.println();
        stream.println("To get more information on each command,");
        stream.println("please try \'help file <command-name>\'.");
        stream.println();
    }

    /**
     * Parses command-line input and prints help menu.
     * 
     * @throws Exception
     */
    public static void executeHelp(String[] args, PrintStream stream) throws Exception {
        String subCmd = (args.length > 0) ? args[0] : "";
        if(subCmd.equals("put")) {
            SubCommandFilePut.printHelp(stream);
        } else if(subCmd.equals("get")) {
            SubCommandFileGet.printHelp(stream);
        } else if(subCmd.equals("delete")) {
            SubCommandFileDelete.printHelp(stream);
        } else {
            printHelp(stream);
        }
    }

    /**
     * file put command
     */
    public static class SubCommandFilePut extends AbstractCommand {

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
            ParserUtils.acceptsLocalPath(parser);
            ParserUtils.acceptsRemotePath(parser);
            ParserUtils.acceptsRemoteNodePath(parser);
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
            stream.println("  file put - Put local file to all remote servers");
            stream.println();
            stream.println("SYNOPSIS");
            stream.println("  file put --config <config-path> --local-path <local-path>");
            stream.println("           (--remote-path <remote-path> | --remote-node-path <node-rel-path>)");
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
            String localPath = null;
            String remotePath = null;
            String remoteNodePath = null;

            // parse command-line input
            OptionSet options = parser.parse(args);
            if(options.has(ParserUtils.OPT_HELP)) {
                printHelp(System.out);
                return;
            }

            // check required options and/or conflicting options
            ParserUtils.checkRequired(options, ParserUtils.OPT_CONFIG);
            ParserUtils.checkRequired(options, ParserUtils.OPT_LOCAL_PATH);
            ParserUtils.checkRequired(options,
                                      ParserUtils.OPT_REMOTE_PATH,
                                      ParserUtils.OPT_REMOTE_NODE_PATH);

            // load parameters
            configPath = (String) options.valueOf(ParserUtils.OPT_CONFIG);
            localPath = (String) options.valueOf(ParserUtils.OPT_LOCAL_PATH);
            if(options.has(ParserUtils.OPT_REMOTE_PATH)) {
                remotePath = (String) options.valueOf(ParserUtils.OPT_REMOTE_PATH);
            }
            if(options.has(ParserUtils.OPT_REMOTE_NODE_PATH)) {
                remoteNodePath = (String) options.valueOf(ParserUtils.OPT_REMOTE_NODE_PATH);
            }

            // execute command
            NaginiClient naginiClient = new NaginiClient(configPath);
            if(remotePath != null) {
                naginiClient.fileOps.putAllHosts(localPath, remotePath);
            } else {
                for(String hostName: naginiClient.config.server.mapHostToNodes.keySet()) {
                    for(Integer nodeId: naginiClient.config.server.mapHostToNodes.get(hostName)) {
                        naginiClient.fileOps.putOneHost(hostName,
                                                     localPath,
                                                     naginiClient.config.server.getNodePath(nodeId)
                                                             + File.separator + remoteNodePath);
                    }
                }
            }
        }
    }

    /**
     * file get command
     */
    public static class SubCommandFileGet extends AbstractCommand {

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
            ParserUtils.acceptsLocalPath(parser);
            ParserUtils.acceptsRemotePath(parser);
            ParserUtils.acceptsRemoteNodePath(parser);
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
            stream.println("  file get - Get file from all remote servers");
            stream.println();
            stream.println("SYNOPSIS");
            stream.println("  file get --config <config-path> --local-path <local-path>");
            stream.println("           (--remote-path <remote-path> | --remote-node-path <node-rel-path>)");
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
            String localPath = null;
            String remotePath = null;
            String remoteNodePath = null;

            // parse command-line input
            OptionSet options = parser.parse(args);
            if(options.has(ParserUtils.OPT_HELP)) {
                printHelp(System.out);
                return;
            }

            // check required options and/or conflicting options
            ParserUtils.checkRequired(options, ParserUtils.OPT_CONFIG);
            ParserUtils.checkRequired(options, ParserUtils.OPT_LOCAL_PATH);
            ParserUtils.checkRequired(options,
                                      ParserUtils.OPT_REMOTE_PATH,
                                      ParserUtils.OPT_REMOTE_NODE_PATH);

            // load parameters
            configPath = (String) options.valueOf(ParserUtils.OPT_CONFIG);
            localPath = (String) options.valueOf(ParserUtils.OPT_LOCAL_PATH);
            if(options.has(ParserUtils.OPT_REMOTE_PATH)) {
                remotePath = (String) options.valueOf(ParserUtils.OPT_REMOTE_PATH);
            }
            if(options.has(ParserUtils.OPT_REMOTE_NODE_PATH)) {
                remoteNodePath = (String) options.valueOf(ParserUtils.OPT_REMOTE_NODE_PATH);
            }

            // execute command
            NaginiClient naginiClient = new NaginiClient(configPath);
            if(remotePath != null) {
                naginiClient.fileOps.getAllHosts(remotePath, localPath);
            } else {
                for(String hostName: naginiClient.config.server.mapHostToNodes.keySet()) {
                    for(Integer nodeId: naginiClient.config.server.mapHostToNodes.get(hostName)) {
                        naginiClient.fileOps.getOneHost(hostName,
                                                     naginiClient.config.server.getNodePath(nodeId)
                                                             + File.separator + remoteNodePath,
                                                     localPath + File.separator + hostName
                                                             + File.separator + "node_" + nodeId);
                    }
                }
            }
        }
    }

    /**
     * file delete command
     */
    public static class SubCommandFileDelete extends AbstractCommand {

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
            ParserUtils.acceptsRemotePath(parser);
            ParserUtils.acceptsRemoteNodePath(parser);
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
            stream.println("  file delete - Delete file from all remote servers");
            stream.println();
            stream.println("SYNOPSIS");
            stream.println("  file delete --config <config-path>");
            stream.println("              (--remote-path <remote-path> | --remote-node-path <node-rel-path>)");
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
            String remotePath = null;
            String remoteNodePath = null;

            // parse command-line input
            OptionSet options = parser.parse(args);
            if(options.has(ParserUtils.OPT_HELP)) {
                printHelp(System.out);
                return;
            }

            // check required options and/or conflicting options
            ParserUtils.checkRequired(options, ParserUtils.OPT_CONFIG);
            if(options.has(ParserUtils.OPT_REMOTE_PATH)) {
                remotePath = (String) options.valueOf(ParserUtils.OPT_REMOTE_PATH);
            }
            if(options.has(ParserUtils.OPT_REMOTE_NODE_PATH)) {
                remoteNodePath = (String) options.valueOf(ParserUtils.OPT_REMOTE_NODE_PATH);
            }

            // load parameters
            configPath = (String) options.valueOf(ParserUtils.OPT_CONFIG);
            if(options.has(ParserUtils.OPT_REMOTE_PATH)) {
                remotePath = (String) options.valueOf(ParserUtils.OPT_REMOTE_PATH);
            }
            if(options.has(ParserUtils.OPT_REMOTE_NODE_PATH)) {
                remoteNodePath = (String) options.valueOf(ParserUtils.OPT_REMOTE_NODE_PATH);
            }

            // execute command
            NaginiClient naginiClient = new NaginiClient(configPath);
            if(remotePath != null) {
                naginiClient.fileOps.deleteAllHosts(remotePath);
            } else {
                for(String hostName: naginiClient.config.server.mapHostToNodes.keySet()) {
                    for(Integer nodeId: naginiClient.config.server.mapHostToNodes.get(hostName)) {
                        naginiClient.fileOps.deleteOneHost(hostName,
                                                        naginiClient.config.server.getNodePath(nodeId)
                                                                + File.separator + remoteNodePath);
                    }
                }
            }
        }
    }
}
