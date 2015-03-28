package nagini.client.command;

import java.io.PrintStream;

import joptsimple.OptionParser;

/**
 * Abstract class that defines commands
 */
public abstract class AbstractCommand {

    /**
     * Initializes parser
     * 
     * @throws Exception
     */
    protected static OptionParser getParser() throws Exception {
        throw new Exception("Parser initializer not implemented.");
    }

    /**
     * Prints help menu for command. If not overwritten by inherited classes, it
     * throws exception by default.
     * 
     * @param args Array of arguments for this command
     * @throws Exception
     */
    @SuppressWarnings("unused")
    public static void printHelp(PrintStream stream) throws Exception {
        throw new RuntimeException("Help menu not implemented.");
    }

    /**
     * Parses command-line and decides what help menu to be printed out. If not
     * overwritten by inherited classes, it throws exception by default.
     * 
     * @param args Array of arguments for this command
     * @throws Exception
     */
    @SuppressWarnings("unused")
    public static void executeHelp(String[] args, PrintStream stream) throws Exception {
        throw new RuntimeException("Help menu not implemented.");
    }

    /**
     * Parses command-line and executes command with arguments. If not
     * overwritten by inherited classes, it throws exception by default.
     * 
     * @param args Array of arguments for this command
     * @throws Exception
     */
    public static void executeCommand(String[] args) throws Exception {
        throw new RuntimeException("Command not implemented.");
    }
}
