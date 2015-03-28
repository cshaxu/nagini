package nagini.utils.process;

import java.io.File;
import java.util.List;
import java.util.Queue;

import nagini.utils.NaginiFileUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

public class Service extends Thread {

    private String logfile;
    private Integer capacity;
    private Boolean watchBufferEnabled;

    private Boolean exitFlag;
    private Queue<ProcessThread> queue;

    public Service(String name, String logfile, Integer capacity, Boolean watchBufferEnabled) {
        this.setName(name);
        this.logfile = logfile;
        this.capacity = capacity;
        this.watchBufferEnabled = watchBufferEnabled;

        this.exitFlag = true;
        this.queue = Queues.newArrayBlockingQueue(capacity);
    }

    public String getLogFile() {
        return this.logfile;
    }

    public Integer getCapacity() {
        return this.capacity;
    }

    public Integer getJobCount() {
        return queue.size();
    }

    public Boolean isRunningJob() {
        return queue.size() > 0 && queue.peek().isAlive();
    }

    public ProcessThread getCurrentJob() {
        if(isRunningJob()) {
            return queue.peek();
        } else {
            return null;
        }
    }

    public synchronized void removeCurrentJob() {
        if(isRunningJob()) {
            getCurrentJob().terminate();
        }
    }

    public Queue<ProcessThread> getAllJobs() {
        return this.queue;
    }

    public synchronized void removeAllJobs() {
        for(ProcessThread thread: queue) {
            if(thread.isAlive()) {
                thread.terminate();
            }
        }
        queue.clear();
    }

    /**
     * Adds a new job to the queue
     * 
     * @param name
     * @param args
     * @param path
     * @return
     */
    public synchronized Boolean addJob(String name, List<String> args, String path) {
        if(queue != null && queue.size() < capacity) {
            queue.add(new ProcessThread(name, args, path, logfile, watchBufferEnabled));
            return true;
        } else {
            return false;
        }
    }

    public List<String> readOutput() {
        List<String> output = Lists.newArrayList();
        if(isRunningJob()) {
            List<String> stdout = getCurrentJob().readStdOut();
            List<String> stderr = getCurrentJob().readStdErr();
            output.addAll(stdout);
            output.addAll(stderr);
        }
        return output;
    }

    @Override
    public void run() {
        exitFlag = false;

        while(!exitFlag) {
            try {
                if(queue.size() > 0) {
                    ProcessThread current = queue.peek();
                    Long startTime = System.currentTimeMillis();
                    if(new File(logfile).exists()) {
                        NaginiFileUtils.delete(logfile);
                    }
                    if(!current.isAlive()) {
                        current.start();
                    }
                    current.join();
                    if(!queue.isEmpty()) {
                        queue.remove();
                    }
                    Long endTime = System.currentTimeMillis();
                    if(new File(logfile).exists()) {
                        NaginiFileUtils.move(logfile, logfile + "." + startTime + "." + endTime);
                    }
                }
                Thread.sleep(1000);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove all jobs and stops this service thread
     */
    public synchronized void terminate() {
        removeAllJobs();
        exitFlag = true;
    }

}
