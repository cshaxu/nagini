package nagini.utils.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import com.google.common.collect.Lists;

public class StreamWatchThread extends Thread {

    private InputStream is;
    private PrintStream ps;
    private List<String> buffer;
    private final Object lock = new Object();

    public StreamWatchThread(InputStream is, PrintStream ps, Boolean watchBufferEnabled) {
        this.is = is;
        this.ps = ps;
        if(watchBufferEnabled) {
            this.buffer = Lists.newArrayList();
        } else {
            this.buffer = null;
        }
    }

    public List<String> readOutput() {
        List<String> list = null;
        if(buffer != null) {
            synchronized(lock) {
                list = Lists.newArrayList(buffer);
                buffer.clear();
            }
        }
        return list;
    }

    private void writeNewLine(String newLine) {
        if(buffer != null) {
            synchronized(lock) {
                buffer.add(newLine);
            }
        }
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while((line = br.readLine()) != null) {
                if(ps != null) {
                    ps.println(line);
                }
                writeNewLine(line);
            }
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
