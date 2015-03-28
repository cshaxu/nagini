package nagini.client.command;

import java.io.PrintStream;

/** Implements all nagini commands. */
public class NaginiCommand extends AbstractCommand {

    /**
     * Parses command-line and directs to command groups or non-grouped
     * sub-commands.
     * 
     * @param args Command-line input
     * @throws Exception
     */
    public static void executeCommand(String[] args) throws Exception {
        String subCmd = (args.length > 0) ? args[0] : "";
        args = CommandUtils.copyArrayCutFirst(args);
        if(subCmd.equals("control")) {
            NaginiCommandControl.executeCommand(args);
        } else if(subCmd.equals("file")) {
            NaginiCommandFile.executeCommand(args);
        } else if(subCmd.equals("deploy")) {
            NaginiCommandDeploy.executeCommand(args);
        } else if(subCmd.equals("clean")) {
            NaginiCommandClean.executeCommand(args);
        } else if(subCmd.equals("start")) {
            NaginiCommandStart.executeCommand(args);
        } else if(subCmd.equals("stop")) {
            NaginiCommandStop.executeCommand(args);
        } else if(subCmd.equals("watch")) {
            NaginiCommandWatch.executeCommand(args);
        } else if(subCmd.equals("help") || subCmd.equals("--help") || subCmd.equals("-h")) {
            executeHelp(args, System.out);
        } else {
            args = CommandUtils.copyArrayAddFirst(args, subCmd);
            NaginiCommandOther.executeCommand(args);
        }
    }

    /**
     * Prints command-line help menu.
     */
    public static void printHelp(PrintStream stream) {
        stream.println();
        stream.println("Nagini Commands");
        stream.println("---------------");
        stream.println("control                Print server status or stop remote servers.");
        stream.println("file                   Put, get or delete remote files.");
        stream.println("deploy                 Deploy application or config.");
        stream.println("clean                  Clean application or config.");
        stream.println("start                  Start application on remote hosts.");
        stream.println("stop                   Stop application on remote hosts.");
        stream.println("watch                  Watch application on remote hosts.");
        stream.println("help                   Show help menu or information for each command.");
        stream.println();
        stream.println("To get more information on each command, please try \'help <command-name>\'.");
        stream.println();
    }

    /**
     * Parses command-line input and prints help menu.
     * 
     * @throws Exception
     */
    public static void executeHelp(String[] args, PrintStream stream) throws Exception {
        String subCmd = (args.length > 0) ? args[0] : "";
        args = CommandUtils.copyArrayCutFirst(args);
        if(subCmd.equals("control")) {
            NaginiCommandControl.executeHelp(args, stream);
        } else if(subCmd.equals("file")) {
            NaginiCommandFile.executeHelp(args, stream);
        } else if(subCmd.equals("deploy")) {
            NaginiCommandDeploy.executeHelp(args, stream);
        } else if(subCmd.equals("clean")) {
            NaginiCommandClean.executeHelp(args, stream);
        } else if(subCmd.equals("start")) {
            NaginiCommandStart.executeHelp(args, stream);
        } else if(subCmd.equals("stop")) {
            NaginiCommandStop.executeHelp(args, stream);
        } else if(subCmd.equals("watch")) {
            NaginiCommandWatch.executeHelp(args, stream);
        } else {
            args = CommandUtils.copyArrayAddFirst(args, subCmd);
            NaginiCommandOther.executeHelp(args, stream);
        }
    }
}
