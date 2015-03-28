package nagini.server;

import java.util.List;

public class ServiceStatus {

    public String service_name;
    public Boolean is_alive;
    public List<JobStatus> job_list;
}