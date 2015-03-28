package nagini.client.command;

import java.io.PrintStream;

/**
 * Implements all non-grouped commands.
 * 
 */
public class NaginiCommandOther extends AbstractCommand {

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
        if(subCmd.equals("ping")) {
            NaginiCommandControl.SubCommandControlPing.executeCommand(args);
        } else {
            NaginiCommand.printHelp(System.out);
        }
    }

    /**
     * Parses command-line input and prints help menu.
     * 
     * @throws Exception
     */
    public static void executeHelp(String[] args, PrintStream stream) throws Exception {
        String subCmd = (args.length > 0) ? args[0] : "";
        if(subCmd.equals("ping")) {
            NaginiCommandControl.SubCommandControlPing.printHelp(stream);
        } else {
            NaginiCommand.printHelp(stream);
        }
    }
}
