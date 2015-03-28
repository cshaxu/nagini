package nagini.utils.process;

import java.io.File;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

public class ProcessThread extends Thread {

    private Process process;
    private StreamWatchThread stdout;
    private StreamWatchThread stderr;
    private Boolean watchBufferEnabled;

    private List<String> args;
    private String path;
    private String logfile;

    public ProcessThread(String name,
                         List<String> args,
                         String path,
                         String logfile,
                         Boolean watchBufferEnabled) {
        process = null;
        stdout = null;
        stderr = null;
        this.setName(name);
        this.args = args;
        this.path = path;
        this.logfile = logfile;
        this.watchBufferEnabled = watchBufferEnabled;
    }

    public List<String> getArguments() {
        return args;
    }

    public String getPath() {
        return path;
    }

    public Integer exitValue() {
        return process.exitValue();
    }

    public void terminate() {
        if(process != null) {
            process.destroy();
        }
    }

    public List<String> readStdOut() {
        if(stdout != null) {
            return stdout.readOutput();
        } else {
            return null;
        }
    }

    public List<String> readStdErr() {
        if(stderr != null) {
            return stderr.readOutput();
        } else {
            return null;
        }
    }

    @Override
    public void run() throws RuntimeException {
        ProcessBuilder processBuilder = new ProcessBuilder(args).redirectErrorStream(true)
                                                                .directory(new File(path));
        try {
            PrintStream stream = new PrintStream(logfile);
            stream.println("Starting process " + this.getName() + " at " + new Date().toString());
            process = processBuilder.start();
            stdout = new StreamWatchThread(process.getInputStream(), stream, watchBufferEnabled);
            stderr = new StreamWatchThread(process.getErrorStream(), stream, watchBufferEnabled);
            stdout.start();
            stderr.start();
            process.waitFor();
            stream.flush();
            stream.close();
            stdout = null;
            stderr = null;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
