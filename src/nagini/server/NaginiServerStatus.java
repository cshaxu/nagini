package nagini.server;

import java.io.PrintStream;
import java.util.List;

public class NaginiServerStatus {

    public String host_name;
    public List<NodeStatus> node_list;

    public void print(PrintStream stream) {
        for(NodeStatus ns: node_list) {
            stream.println("|->\tNode [id " + ns.node_id + "] contains the following services:");
            for(ServiceStatus ss: ns.service_list) {
                stream.println("\t|->\t" + (ss.is_alive ? "OK" : "ERROR") + "\t" + ss.service_name + " handling the following jobs:");
                for(JobStatus js: ss.job_list) {
                    stream.println("\t\t|->\t" + (js.is_active ? "RUNNING" : "WAITING") + "\t  " + js.job_name);
                }
                if (ss.job_list.isEmpty()) {
                    stream.println("\t\t|->\tNo jobs.");
                }
            }
        }
    }
}
