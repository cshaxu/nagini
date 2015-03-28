package nagini.server;

import java.io.PrintStream;
import java.util.List;

public class NaginiServerStatus {

    public String host_name;
    public List<NodeStatus> node_list;

    public void print(PrintStream stream) {
        for(NodeStatus ns: node_list) {
            stream.println("Node = " + ns.node_id);
            for(ServiceStatus ss: ns.service_list) {
                stream.println((ss.is_alive ? "OK" : "ERROR") + "\t" + ss.service_name);
                for(JobStatus js: ss.job_list) {
                    stream.println("  " + (js.is_active ? "RUN" : "WAIT") + "\t  " + js.job_name);
                }
            }
        }
    }
}
