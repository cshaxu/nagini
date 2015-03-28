package nagini.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import nagini.config.NaginiConfig;
import nagini.protocol.RequestType;
import nagini.protocol.ResponseType;
import nagini.protocol.SocketAndStreams;
import nagini.server.NaginiServerStatus;
import nagini.utils.NaginiFileUtils;
import nagini.utils.NaginiZipUtils;
import nagini.utils.process.NaginiProcessUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

public class NaginiClient {

    public NaginiConfig config;
    public ControlOperations controlOps;
    public FileOperations fileOps;
    public ServiceOperations serviceOps;

    public NaginiClient(String configPath) throws IOException {
        config = new NaginiConfig(configPath);
        controlOps = new ControlOperations();
        fileOps = new FileOperations();
        serviceOps = new ServiceOperations();
    }

    public String getClientTempPath() {
        return config.client.tempPath + File.separator + "Neko_" + System.nanoTime();
    }

    public void loadConfig(String configPath) throws IOException {
        config.loadConfig(configPath);
    }

    public Integer receiveResponseMessage(SocketAndStreams sands) throws IOException {
        DataInputStream dis = sands.getInputStream();
        Integer responseType = dis.readInt();
        String responseMessage = null;
        switch(responseType) {
            case ResponseType.RESPONSE_SUCCESS:
                responseMessage = dis.readUTF();
                System.out.println(responseMessage);
                responseMessage = dis.readUTF();
                System.out.println(responseMessage);
                break;
            case ResponseType.RESPONSE_FAIL:
                responseMessage = dis.readUTF();
                System.out.println(responseMessage);
                responseMessage = dis.readUTF();
                System.out.println(responseMessage);
                break;
            case ResponseType.RESPONSE_FILE:
                break;
            case ResponseType.RESPONSE_NOOP:
                break;
            default:
                throw new RuntimeException("Client: unexpected server response type.");
        }
        return responseType;
    }

    public void receiveAndCheckResponseMessage(SocketAndStreams sands) throws IOException {
        Integer responseType = receiveResponseMessage(sands);
        switch(responseType) {
            case ResponseType.RESPONSE_SUCCESS:
                break;
            case ResponseType.RESPONSE_FAIL:
                throw new RuntimeException("Client: server operation failed");
            default:
                sands.close();
                throw new RuntimeException("Client: invalid server response type.");
        }
    }

    public class ControlOperations {

        /**
         * Inner function that pings one remote server.
         * 
         * @param hostName
         * @throws IOException
         */
        private void pingInner(String hostName) throws IOException {
            SocketAndStreams sands = new SocketAndStreams(hostName, config.server.portId);
            DataOutputStream dos = sands.getOutputStream();
            System.out.println("Client: ping " + hostName + " ...");
            // send request type
            dos.writeInt(RequestType.REQUEST_PING);
            // flush request header
            dos.flush();

            // receive server response
            DataInputStream dis = sands.getInputStream();
            int responseType = dis.readInt();
            if(responseType == ResponseType.RESPONSE_SUCCESS) {
                String responseMessage = dis.readUTF();
                System.out.println(responseMessage);
                responseMessage = dis.readUTF();
                NaginiServerStatus status = new Gson().fromJson(responseMessage,
                                                                NaginiServerStatus.class);
                status.print(System.out);
                sands.close();
            } else {
                sands.close();
                throw new RuntimeException("server operation failed");
            }

        }

        /**
         * Pings one remote server.
         * 
         * @param hostName
         * @throws IOException
         */
        public void pingOneHost(String hostName) throws IOException {
            pingInner(hostName);
        }

        /**
         * Pings all remote servers.
         * 
         * @throws IOException
         */
        public void pingAllHosts() throws IOException {
            for(String hostName: config.server.mapHostToNodes.keySet()) {
                try {
                    pingInner(hostName);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Inner function that stops one remote server.
         * 
         * @param hostName
         * @throws IOException
         */
        private void stopInner(String hostName) throws IOException {
            SocketAndStreams sands = new SocketAndStreams(hostName, config.server.portId);
            DataOutputStream dos = sands.getOutputStream();
            System.out.println("Client: stopping " + hostName + " ...");
            // send request type
            dos.writeInt(RequestType.REQUEST_STOP);
            // flush request header
            dos.flush();
            receiveAndCheckResponseMessage(sands);
            sands.close();
        }

        /**
         * Stops one remote server.
         * 
         * @param hostName
         * @throws IOException
         */
        public void stopOneHost(String hostName) throws IOException {
            stopInner(hostName);
        }

        /**
         * Stops all remote servers.
         * 
         * @throws IOException
         */
        public void stopAllHosts() throws IOException {
            for(String hostName: config.server.mapHostToNodes.keySet()) {
                try {
                    stopInner(hostName);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Inner function that starts one remote server.
         * 
         * @param hostName
         * @throws Exception
         */
        private void startInner(String hostName) throws Exception {
            List<String> args = Lists.newArrayList();
            System.out.println("Client: starting " + hostName + " ...");
            args.add("ssh");
            args.add(hostName);
            args.add("\"\"sudo -u " + config.server.userName + " -sn bash "
                     + config.server.basePath + "/nagini/bin/nagini-server.sh "
                     + config.server.getConfigPath() + " " + hostName + "\"\"");
            NaginiProcessUtils.command(args, new File(config.client.basePath), System.out);
        }

        /**
         * Starts one remote server.
         * 
         * @param hostName
         * @throws Exception
         */
        public void startOneHost(String hostName) throws Exception {
            startInner(hostName);
        }

        /**
         * Starts all remote servers.
         * 
         * @throws Exception
         */
        public void startAllHosts() throws Exception {
            for(String hostName: config.server.mapHostToNodes.keySet()) {
                try {
                    startInner(hostName);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Inner function that reloads config on one remote server.
         * 
         * @param hostName
         * @param configPath
         * @throws IOException
         */
        private void reconfigInner(String hostName, String configPath) throws IOException {
            SocketAndStreams sands = new SocketAndStreams(hostName, config.server.portId);
            DataOutputStream dos = sands.getOutputStream();
            System.out.println("Client: reloading config on " + hostName + " ...");
            // send request type
            dos.writeInt(RequestType.REQUEST_RECONFIG);
            // send config path
            dos.writeUTF(configPath);
            // flush request header
            dos.flush();
            receiveAndCheckResponseMessage(sands);
            sands.close();
        }

        /**
         * Reloads config on one remote server.
         * 
         * @param hostName
         * @param configPath
         * @throws IOException
         */
        public void reconfig(String hostName, String configPath) throws IOException {
            reconfigInner(hostName, configPath);
        }

        /**
         * Reloads config on all remote servers.
         * 
         * @param configPath
         * @throws IOException
         */
        public void reconfig(String configPath) throws IOException {
            for(String hostName: config.server.mapHostToNodes.keySet()) {
                reconfigInner(hostName, configPath);
            }
        }
    }

    public class FileOperations {

        /**
         * Inner function that puts file to one remote server.
         * 
         * @param hostName, remote server host name
         * @param localPath, absolute local path
         * @param remotePath, absolute remote path
         * @throws IOException
         */
        private void putInner(String hostName, String localPath, String remotePath)
                throws IOException {
            SocketAndStreams sands = new SocketAndStreams(hostName, config.server.portId);
            DataOutputStream dos = sands.getOutputStream();
            File localFile = new File(localPath);
            FileInputStream fis = new FileInputStream(localFile);

            System.out.println("Client: putting " + localPath + " to " + hostName + " ... ("
                               + localFile.length() + " bytes)");

            // send request type
            dos.writeInt(RequestType.REQUEST_FILE_PUT);
            // send remote file path
            dos.writeUTF(remotePath);
            // send file length
            dos.writeLong(localFile.length());
            // flush request header
            dos.flush();

            int bufferSize = 65536;
            byte[] buffer = new byte[bufferSize];
            int read;

            while((read = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
            }
            dos.flush();
            fis.close();

            receiveAndCheckResponseMessage(sands);

            sands.close();
        }

        /**
         * Puts file to one remote server.
         * 
         * @param hostName
         * @param localPath
         * @param remotePath
         * @throws IOException
         */
        public void putOneHost(String hostName, String localPath, String remotePath)
                throws IOException {
            String tempZipPath = getClientTempPath() + ".zip";
            localPath = localPath.replace("~", System.getProperty("user.home"));
            if(!new File(localPath).exists()) {
                throw new RuntimeException("Client: cannot find " + localPath);
            }
            System.out.println("Client: zipping " + localPath + " ...");
            NaginiZipUtils.zip(localPath, tempZipPath, null);
            putInner(hostName, tempZipPath, remotePath);
            NaginiFileUtils.delete(new File(tempZipPath));
        }

        /**
         * Puts file to all remote servers.
         * 
         * @param localPath
         * @param remotePath
         * @throws IOException
         */
        public void putAllHosts(String localPath, String remotePath) throws IOException {
            String tempZipPath = getClientTempPath() + ".zip";
            localPath = localPath.replace("~", System.getProperty("user.home"));
            if(!new File(localPath).exists()) {
                throw new RuntimeException("Client: cannot find " + localPath);
            }
            System.out.println("Client: zipping " + localPath + " ...");
            NaginiZipUtils.zip(localPath, tempZipPath, null);
            for(String hostName: config.server.mapHostToNodes.keySet()) {
                putInner(hostName, tempZipPath, remotePath);
            }
            NaginiFileUtils.delete(new File(tempZipPath));
        }

        /**
         * Inner function that gets file from one remote server.
         * 
         * @param hostName, remote server host name
         * @param remotePath, absolute remote path
         * @param localPath, absolute local path
         * @throws IOException
         */
        private void getInner(String hostName, String remotePath, String localPath)
                throws IOException {
            SocketAndStreams sands = new SocketAndStreams(hostName, config.server.portId);
            DataOutputStream dos = sands.getOutputStream();
            DataInputStream dis = sands.getInputStream();

            System.out.println("Client: getting " + localPath + " from " + hostName + " ...");

            // send request type
            dos.writeInt(RequestType.REQUEST_FILE_GET);
            // send remote file path
            dos.writeUTF(remotePath);
            // flush request header
            dos.flush();

            // receive file get response
            int responseType = dis.readInt();
            if(responseType != ResponseType.RESPONSE_FILE) {
                sands.close();
                throw new RuntimeException("Client: failed to get file from " + hostName
                                           + ". (unknown)");
            }

            Long fileLength = dis.readLong();
            File localFile = new File(localPath);
            FileOutputStream fos = new FileOutputStream(localFile);

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

            sands.close();

            if(localFile.exists() && done == fileLength) {
                System.out.println("Client: successfully received file. (" + done + " bytes)");
            } else {
                throw new RuntimeException("Client: failed to receive file. (" + done + " bytes)");
            }
        }

        /**
         * Gets file from one remote server.
         * 
         * @param hostName
         * @param remotePath
         * @param localPath
         * @throws IOException
         */
        public void getOneHost(String hostName, String remotePath, String localPath)
                throws IOException {
            localPath = localPath.replace("~", System.getProperty("user.home"));
            String tempZipPath = getClientTempPath() + ".zip";
            File tempZipFile = new File(tempZipPath);
            String destinationPath = localPath;
            getInner(hostName, remotePath, tempZipPath);
            System.out.println("Client: unzipping received file ...");
            NaginiZipUtils.unzip(tempZipPath, destinationPath, null);
            NaginiFileUtils.delete(tempZipFile);

        }

        /**
         * Gets file from all remote servers (automatically creates separate
         * folders for files from each server).
         * 
         * @param remotePath
         * @param localPath
         * @throws IOException
         */
        public void getAllHosts(String remotePath, String localPath) throws IOException {
            localPath = localPath.replace("~", System.getProperty("user.home"));
            for(String hostName: config.server.mapHostToNodes.keySet()) {
                String tempZipPath = getClientTempPath() + ".zip";
                File tempZipFile = new File(tempZipPath);
                String destinationPath = localPath + File.separator + hostName;
                getInner(hostName, remotePath, tempZipPath);
                System.out.println("Client: unzipping received file ...");
                NaginiZipUtils.unzip(tempZipPath, destinationPath, null);
                NaginiFileUtils.delete(tempZipFile);
            }
        }

        /**
         * Inner function that deletes file from one remote server.
         * 
         * @param hostName, remote server host name
         * @param remotePath, absolute remote path
         * @throws IOException
         */
        private void deleteInner(String hostName, String remotePath) throws IOException {
            SocketAndStreams sands = new SocketAndStreams(hostName, config.server.portId);
            DataOutputStream dos = sands.getOutputStream();
            System.out.println("Client: deleting file " + remotePath + " on " + hostName + " ...");

            // send request type
            dos.writeInt(RequestType.REQUEST_FILE_DELETE);
            // send remote file path
            dos.writeUTF(remotePath);
            // flush request header
            dos.flush();

            receiveAndCheckResponseMessage(sands);
            sands.close();
        }

        /**
         * Deletes file on one remote server.
         * 
         * @param remotePath
         * @throws IOException
         */
        public void deleteOneHost(String hostName, String remotePath) throws IOException {
            deleteInner(hostName, remotePath);
        }

        /**
         * Deletes file on all remote servers.
         * 
         * @param remotePath
         * @throws IOException
         */
        public void deleteAllHosts(String remotePath) throws IOException {
            for(String hostName: config.server.mapHostToNodes.keySet()) {
                deleteInner(hostName, remotePath);
            }
        }
    }

    public class ServiceOperations {

        /**
         * Inner function that starts one application node.
         * 
         * @param nodeId
         * @throws IOException
         */
        private void startApplicationInner(Integer nodeId) throws IOException {
            String hostName = config.server.mapNodeToHost.get(nodeId);

            SocketAndStreams sands = new SocketAndStreams(hostName, config.server.portId);
            DataOutputStream dos = sands.getOutputStream();

            System.out.println("Client: starting application node " + nodeId + " on " + hostName
                               + " ...");

            // send request type
            dos.writeInt(RequestType.REQUEST_SERVICE_START_APPLICATION);
            // send node id
            dos.writeInt(nodeId);
            // flush request header
            dos.flush();

            receiveAndCheckResponseMessage(sands);
            sands.close();
        }

        /**
         * Starts one application node.
         * 
         * @param nodeId
         * @throws IOException
         */
        public void startApplicationOneNode(Integer nodeId) throws IOException {
            startApplicationInner(nodeId);
        }

        /**
         * Starts all application nodes.
         * 
         * @throws IOException
         */
        public void startApplicationAllNodes() throws IOException {
            for(String hostName: config.server.mapHostToNodes.keySet()) {
                for(Integer nodeId: config.server.mapHostToNodes.get(hostName)) {
                    startApplicationInner(nodeId);
                }
            }
        }

        /**
         * Inner function that stops one application node.
         * 
         * @param nodeId
         * @throws IOException
         */
        private void stopApplicationInner(Integer nodeId) throws IOException {
            String hostName = config.server.mapNodeToHost.get(nodeId);
            SocketAndStreams sands = new SocketAndStreams(hostName, config.server.portId);
            DataOutputStream dos = sands.getOutputStream();

            System.out.println("Client: stopping application node " + nodeId + " on " + hostName
                               + " ...");

            // send request type
            dos.writeInt(RequestType.REQUEST_SERVICE_STOP_APPLICATION);
            // send node id
            dos.writeInt(nodeId);
            // flush request header
            dos.flush();

            receiveAndCheckResponseMessage(sands);
            sands.close();
        }

        /**
         * Stops one application node.
         * 
         * @param nodeId
         * @throws IOException
         */
        public void stopApplicationOneNode(Integer nodeId) throws IOException {
            stopApplicationInner(nodeId);
        }

        /**
         * Stops all application nodes.
         * 
         * @throws IOException
         */
        public void stopApplicationAllNodes() throws IOException {
            for(String hostName: config.server.mapHostToNodes.keySet()) {
                for(Integer nodeId: config.server.mapHostToNodes.get(hostName)) {
                    stopApplicationInner(nodeId);
                }
            }
        }

        /**
         * Inner function that watches one application node.
         * 
         * @param nodeId
         * @param tail
         * @return true if application is running on this node
         * @throws IOException
         */
        private Boolean watchApplicationInner(Integer nodeId, Integer tail) throws IOException {
            String hostName = config.server.mapNodeToHost.get(nodeId);
            SocketAndStreams sands = new SocketAndStreams(hostName, config.server.portId);
            DataOutputStream dos = sands.getOutputStream();

            // send request type
            dos.writeInt(RequestType.REQUEST_SERVICE_WATCH_APPLICATION);
            // send node id
            dos.writeInt(nodeId);
            // send tail number
            dos.writeInt(tail);
            // flush request header
            dos.flush();

            Integer responseType = receiveResponseMessage(sands);
            sands.close();
            return responseType == ResponseType.RESPONSE_SUCCESS;
        }

        /**
         * Watches one application node.
         * 
         * @param nodeId
         * @param interval
         * @param tail
         * @throws IOException
         * @throws
         */
        public void watchApplicationOneNode(Integer nodeId, Integer interval, Integer tail)
                throws IOException {
            while(watchApplicationInner(nodeId, tail)) {
                try {
                    Thread.sleep(interval * 1000);
                } catch(Exception e) {
                    throw new IOException(e);
                }
            }
        }

        /**
         * Watches all application nodes.
         * 
         * @param interval
         * @param tail
         * @throws IOException
         */
        public void watchApplicationAllNodes(Integer interval, Integer tail) throws IOException {
            Set<Integer> nodeIds = Sets.newHashSet();
            for(String hostName: config.server.mapHostToNodes.keySet()) {
                for(Integer nodeId: config.server.mapHostToNodes.get(hostName)) {
                    nodeIds.add(nodeId);
                }
            }
            while(nodeIds.size() > 0) {
                Set<Integer> removedNodeIds = Sets.newHashSet();
                for(Integer nodeId: nodeIds) {
                    if(!watchApplicationInner(nodeId, tail)) {
                        System.out.println("Node "
                                           + nodeId
                                           + " is not running application. Remove from watch node list.");
                        removedNodeIds.add(nodeId);
                    }
                }
                nodeIds.removeAll(removedNodeIds);
                if(nodeIds.size() > 0) {
                    try {
                        Thread.sleep(interval * 1000);
                    } catch(Exception e) {
                        throw new IOException(e);
                    }
                }
            }
        }
    }
}
