package nagini.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nagini.config.NaginiConfig;
import nagini.protocol.RequestType;
import nagini.protocol.ResponseType;
import nagini.protocol.SocketAndStreams;
import nagini.utils.JavaCommandBuilder;
import nagini.utils.NaginiFileUtils;
import nagini.utils.NaginiZipUtils;
import nagini.utils.process.ProcessThread;
import nagini.utils.process.Service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

public class NaginiServer {

    public String hostName;
    public NaginiConfig config;
    public ServerSocket serverSocket;
    public List<Integer> nodeIds;
    public Map<Integer, Service> mapNodeIdToApplicationStarterService;

    public NaginiServer(String configPath, String hostName) throws IOException {
        this.hostName = hostName;
        this.config = null;
        this.serverSocket = null;
        this.nodeIds = null;
        this.mapNodeIdToApplicationStarterService = null;
        loadConfig(configPath);
    }

    private static boolean isInteger(String s) {
        if(s.isEmpty()) {
            return false;
        }
        for(int i = 0; i < s.length(); i++) {
            if(!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static void setupNodeConfig(File parent, Integer nodeId) {
        if(!parent.exists() || !parent.isDirectory()) {
            return;
        }
        for(File file: parent.listFiles()) {
            String path = parent.getAbsolutePath();
            String name = file.getName();
            String[] parsedName = name.split("\\.");
            String suffix = parsedName[parsedName.length - 1];
            if(parsedName.length > 1 && isInteger(suffix)) {
                if(!suffix.equals(nodeId.toString())) {
                    NaginiFileUtils.delete(file);
                } else {
                    String newName = new String();
                    for(int i = 0; i < parsedName.length - 1; ++i) {
                        newName = newName + parsedName[i];
                        if(i < parsedName.length - 2) {
                            newName = newName + ".";
                        }
                    }
                    NaginiFileUtils.move(path + File.separator + name, path + File.separator
                                                                       + newName);
                }
            }
        }
    }

    private void generateNodeConfig(Integer nodeId) throws IOException {
        // check and create node/config folder
        NaginiFileUtils.delete(config.server.getNodeConfigPath(nodeId));
        NaginiFileUtils.copy(config.server.getConfigApplicationPath(),
                             config.server.getNodeConfigPath(nodeId));
        setupNodeConfig(new File(config.server.getNodeConfigPath(nodeId)), nodeId);
    }

    private String getServerTempPath() {
        return config.server.tempPath + File.separator + "Neko_" + System.nanoTime();
    }

    public void loadConfig(String configPath) throws IOException {
        // load config from files
        Integer oldPortId = null;
        if(config != null) {
            oldPortId = config.server.portId;
        }

        config = new NaginiConfig(configPath);

        // load server socket
        if(serverSocket != null && oldPortId != config.server.portId) {
            serverSocket.close();
            serverSocket = null;
        }

        if(serverSocket == null) {
            serverSocket = new ServerSocket(config.server.portId);
        }

        // load node id list
        nodeIds = config.server.mapHostToNodes.get(hostName);

        if(nodeIds == null) {
            nodeIds = Lists.newArrayList();
        }

        // create map node to serviec
        mapNodeIdToApplicationStarterService = Maps.newHashMap();

        // generate application node config and create node services
        for(Integer nodeId: nodeIds) {
            generateNodeConfig(nodeId);
            mapNodeIdToApplicationStarterService.put(nodeId,
                                                     new Service("application-starter-" + nodeId,
                                                                 config.server.getNodeApplicationLogFilePath(nodeId),
                                                                 1,
                                                                 config.server.watchEnabled));
        }
    }

    private void startServices() {
        Service service = null;
        for(Integer nodeId: nodeIds) {
            service = mapNodeIdToApplicationStarterService.get(nodeId);
            if(service != null) {
                service.start();
            }
        }
    }

    private void stopServices() throws InterruptedException {
        Service service = null;
        for(Integer nodeId: nodeIds) {
            service = mapNodeIdToApplicationStarterService.get(nodeId);
            if(service != null) {
                service.terminate();
                service.join();
            }
        }
    }

    public void start() throws IOException {
        startServices();
        handleRequests();
    }

    private void handleRequests() throws IOException {
        while(true) {
            SocketAndStreams sands = null;
            try {
                sands = new SocketAndStreams(serverSocket.accept());
                int requestType = sands.getInputStream().readInt();
                switch(requestType) {
                    case RequestType.REQUEST_PING:
                        handleControlPing(sands);
                        break;
                    case RequestType.REQUEST_STOP:
                        handleControlStop(sands);
                        break;
                    case RequestType.REQUEST_RECONFIG:
                        handleControlReconfig(sands);
                        break;
                    case RequestType.REQUEST_FILE_PUT:
                        handleFilePutRequest(sands);
                        break;
                    case RequestType.REQUEST_FILE_GET:
                        handleFileGetRequest(sands);
                        break;
                    case RequestType.REQUEST_FILE_DELETE:
                        handleFileDeleteRequest(sands);
                        break;
                    case RequestType.REQUEST_SERVICE_START_APPLICATION:
                        handleStartApplicationRequest(sands);
                        break;
                    case RequestType.REQUEST_SERVICE_STOP_APPLICATION:
                        handleStopApplicationRequest(sands);
                        break;
                    case RequestType.REQUEST_SERVICE_WATCH_APPLICATION:
                        handleWatchApplicationRequest(sands);
                        break;
                    default:
                        sendFailResponse(sands,
                                         "invalid request. (0x" + Integer.toHexString(requestType)
                                                 + ")");
                        break;
                }
            } catch(Exception e) {
                System.out.println("NaginiServer Exception: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if(sands != null) {
                    sands.close();
                }
            }
        }
    }

    private void sendSuccessResponse(SocketAndStreams sands, String message) throws IOException {
        DataOutputStream dos = sands.getOutputStream();
        dos.writeInt(ResponseType.RESPONSE_SUCCESS);
        dos.writeUTF("Server: [host=" + hostName + "]: ");
        dos.writeUTF(message);
        dos.flush();
    }

    private void sendFailResponse(SocketAndStreams sands, String message) throws IOException {
        DataOutputStream dos = sands.getOutputStream();
        dos.writeInt(ResponseType.RESPONSE_FAIL);
        dos.writeUTF("Server: [host=" + hostName + "]: ");
        dos.writeUTF(message);
        dos.flush();
    }

    private void sendWatchResponse(SocketAndStreams sands,
                                   Service service,
                                   Integer nodeId,
                                   Integer tail) throws IOException {
        if(!service.isAlive()) {
            sendFailResponse(sands, "service " + service.getName() + " is corrupted.");
        } else if(service.getJobCount() == 0) {
            sendFailResponse(sands, "service " + service.getName()
                                    + " does not have any job to run.");
        } else if(!service.isRunningJob()) {
            sendSuccessResponse(sands, "service " + service.getName()
                                       + " is going to run the next job.");
        } else {
            List<String> output = Lists.newArrayList();
            output.add("        [node = " + nodeId + "]");
            output.addAll(service.readOutput());
            if(tail > 0) {
                while(output.size() > tail) {
                    output.remove(0);
                }
            }
            sendSuccessResponse(sands, Joiner.on("\n").join(output));
        }
    }

    private JobStatus getJobStatus(ProcessThread thread) {
        JobStatus status = new JobStatus();
        status.job_name = thread.getName();
        status.is_active = thread.isAlive();
        return status;
    }

    private ServiceStatus getServiceStatus(Service service) {
        ServiceStatus status = new ServiceStatus();
        status.service_name = service.getName();
        status.is_alive = service.isAlive();
        status.job_list = Lists.newArrayList();
        for(ProcessThread job: service.getAllJobs()) {
            status.job_list.add(getJobStatus(job));
        }
        return status;
    }

    private NodeStatus getNodeStatus(Integer nodeId) {
        NodeStatus status = new NodeStatus();
        status.node_id = nodeId;
        status.service_list = Lists.newArrayList();
        status.service_list.add(getServiceStatus(mapNodeIdToApplicationStarterService.get(nodeId)));
        return status;
    }

    private NaginiServerStatus getServerStatus() {
        NaginiServerStatus status = new NaginiServerStatus();
        status.host_name = hostName;
        status.node_list = Lists.newArrayList();
        for(Integer nodeId: nodeIds) {
            status.node_list.add(getNodeStatus(nodeId));
        }
        return status;
    }

    private void handleControlPing(SocketAndStreams sands) throws IOException {
        NaginiServerStatus status = getServerStatus();
        Gson gson = new Gson();
        String responseMessage = gson.toJson(status, NaginiServerStatus.class);
        sendSuccessResponse(sands, responseMessage);
    }

    private void handleControlStop(SocketAndStreams sands) throws IOException, InterruptedException {
        sendSuccessResponse(sands, "stopping Nagini server ...");
        stopServices();
        System.exit(0);
    }

    private void handleControlReconfig(SocketAndStreams sands) throws IOException,
            InterruptedException {
        DataInputStream dis = sands.getInputStream();
        String configPath = dis.readUTF();
        sendSuccessResponse(sands, "started reloading config file from " + configPath);
        stopServices();
        loadConfig(configPath);
        startServices();
    }

    private void handleFilePutRequest(SocketAndStreams sands) throws IOException {
        String tempZipPath = getServerTempPath() + ".zip";
        File tempZipFile = new File(tempZipPath);
        DataInputStream dis = sands.getInputStream();
        String destPath = dis.readUTF();
        Long fileLength = dis.readLong();
        FileOutputStream fos = new FileOutputStream(tempZipFile);
        int bufferSize = 65536;
        byte[] buffer = new byte[bufferSize];
        int done = 0;
        while(done < fileLength) {
            int read = dis.read(buffer);
            fos.write(buffer, 0, read);
            if(read == -1) {
                break;
            }
            done += read;
        }
        fos.close();
        if(tempZipFile.exists() && done == fileLength) {
            sendSuccessResponse(sands, "successfully received file + " + tempZipPath + ". (" + done
                                       + " bytes)");
            System.out.println("unzipping file " + tempZipPath + "...");
            NaginiZipUtils.unzip(tempZipPath, destPath, null);
            System.out.println("unzipped file" + tempZipPath + ".");
        } else {
            sendFailResponse(sands, "failed to receive file " + tempZipPath + ". (" + done
                                    + " out of " + fileLength + " bytes)");
        }
        NaginiFileUtils.delete(tempZipFile);
    }

    private void handleFileGetRequest(SocketAndStreams sands) throws IOException {
        String tempZipPath = getServerTempPath() + ".zip";
        File tempZipFile = new File(tempZipPath);
        DataOutputStream dos = sands.getOutputStream();
        String filePath = sands.getInputStream().readUTF();
        if(!new File(filePath).exists()) {
            sendFailResponse(sands, "failed to send " + filePath + ". (file does not exist)");
            return;
        }

        System.out.println("zipping " + filePath + " ...");
        NaginiZipUtils.zip(filePath, tempZipPath, null);

        FileInputStream fis = new FileInputStream(tempZipFile);
        Long fileLength = tempZipFile.length();

        int bufferSize = 65536;
        byte[] buffer = new byte[bufferSize];
        int read;

        System.out.println("sending " + tempZipPath + " ...");
        dos.writeInt(ResponseType.RESPONSE_FILE);
        dos.writeLong(fileLength);
        while((read = fis.read(buffer)) != -1) {
            dos.write(buffer, 0, read);
        }
        dos.flush();
        fis.close();
        NaginiFileUtils.delete(tempZipFile);

        System.out.println("" + filePath + " sent.");
    }

    private void handleFileDeleteRequest(SocketAndStreams sands) throws IOException {
        String filePath = sands.getInputStream().readUTF();
        Boolean succeed = false;
        try {
            succeed = NaginiFileUtils.delete(new File(filePath));
        } catch(Exception e) {
            System.out.println("Exception during file deletion: " + e.getMessage());
            e.printStackTrace();
        }
        if(succeed) {
            sendSuccessResponse(sands, "successfully deleted " + filePath + ".");
        } else {
            sendFailResponse(sands, "failed to delete " + filePath + ".");
        }
    }

    private void handleStartApplicationRequest(SocketAndStreams sands) throws Exception {
        Integer nodeId = sands.getInputStream().readInt();
        Service service = mapNodeIdToApplicationStarterService.get(nodeId);

        if(!service.isAlive()) {
            sendFailResponse(sands, "application starter service is corrupted.");
            return;
        }

        if(service.isRunningJob()) {
            sendSuccessResponse(sands, "application is already running.");
        } else {
            try {
                List<String> args = null;
                if(config.server.appStartCommand == null) {
                    JavaCommandBuilder jcb = new JavaCommandBuilder();
                    jcb.setClassName(config.server.appJavaMainClass)
                            .setJavaExec(config.server.expandNodePath(config.server.javaExec, nodeId))
                            .setJvmOption(config.server.expandNodePath(config.server.appJvmOpts, nodeId))
                            .addClassOption(config.server.expandNodePath(config.server.appJavaClassOpts,
                                    nodeId));
                    for(String subPath: config.server.appJavaClassSubPaths) {
                        jcb.addClassPathByFolder(config.server.getApplicationPath() + File.separator
                                + subPath);
                    }
                    args = jcb.getJavaCommand();
                } else {
                    args = Arrays.asList(config.server.expandNodePath(config.server.appStartCommand,
                            nodeId).split(" "));
                }
                sendSuccessResponse(sands, "starting application ...");
                service.addJob("application-" + nodeId, args, config.server.getNodePath(nodeId));
                System.out.println("starting application: (node = " + nodeId + ")");
                System.out.println(Joiner.on(" ").join(args));
            } catch (Exception e) {
                String failureMessage = "Failed to start application because of: " + e.getMessage();
                sendFailResponse(sands, failureMessage);
                System.out.println(failureMessage);
                throw e;
            }
        }
    }

    private void handleStopApplicationRequest(SocketAndStreams sands) throws IOException {
        Integer nodeId = sands.getInputStream().readInt();
        Service service = mapNodeIdToApplicationStarterService.get(nodeId);

        if(!service.isAlive()) {
            sendFailResponse(sands, "application starter service is corrupted.");
            return;
        }

        if(!service.isRunningJob()) {
            sendSuccessResponse(sands, "application is not running.");
        } else {
            service.removeAllJobs();
            sendSuccessResponse(sands, "stopping application ...");
            System.out.println("stopping application. (node = " + nodeId + ")");
        }
    }

    private void handleWatchApplicationRequest(SocketAndStreams sands) throws IOException {
        DataInputStream dis = sands.getInputStream();
        Integer nodeId = dis.readInt();
        Integer tail = dis.readInt();
        Service service = mapNodeIdToApplicationStarterService.get(nodeId);
        sendWatchResponse(sands, service, nodeId, tail);
    }
}
