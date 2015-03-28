package nagini;

import nagini.client.command.NaginiCommand;

/**
 * Provides a command line interface to the {@link nagini.client.NaginiClient}
 * 
 * @author Xu Ha
 */
public class NaginiClientCli {

    public static void main(String[] args) {
        try {
            NaginiCommand.executeCommand(args);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
