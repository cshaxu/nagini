package nagini;

import nagini.server.NaginiServer;

/**
 * Provides a command line interface to the {@link nagini.server.NaginiServer}
 * 
 * @author Xu Ha
 */
public class NaginiServerCli {

    public static void main(String[] args) throws Exception {
        if(args.length < 2) {
            System.out.println("Server: missing config path and/or node id.");
            System.exit(1);
        } else {
            String configPath = args[0];
            String hostName = args[1];
            configPath = configPath.replace("~", System.getProperty("user.home"));
            NaginiServer server = new NaginiServer(configPath, hostName);
            server.start();
        }
    }
}
