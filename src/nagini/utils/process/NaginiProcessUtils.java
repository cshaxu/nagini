package nagini.utils.process;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import com.google.common.base.Joiner;

public class NaginiProcessUtils {

    public static int command(List<String> args, File path, PrintStream stream) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(args).redirectErrorStream(true)
                                                                .directory(path);

        stream.println("Command to be executed: " + Joiner.on(" ").join(args));

        Process process = processBuilder.start();
        StreamWatchThread stdout = new StreamWatchThread(process.getInputStream(), stream, false);
        StreamWatchThread stderr = new StreamWatchThread(process.getErrorStream(), stream, false);

        // read stdout and stderr
        stdout.start();
        stderr.start();

        // wait for exit value
        return process.waitFor();
    }
}
