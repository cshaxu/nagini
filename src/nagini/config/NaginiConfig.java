package nagini.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nagini.utils.NaginiFileUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class NaginiConfig {

    private static final String NAGINI_PROPERTIES_FILE = "nagini.properties";
    private static final String HOST_LIST_FILE = "host.list";

    public ServerConfig server;
    public ClientConfig client;

    public NaginiConfig() {
        server = new ServerConfig();
        client = new ClientConfig();
    }

    public NaginiConfig(String configPath) throws IOException {
        this();
        loadConfig(configPath);
    }

    public void loadConfig(String configPath) throws IOException {
        // load nagini.properties
        String propFilePath = configPath + File.separator + NAGINI_PROPERTIES_FILE;
        File propFile = new File(propFilePath);
        if(!propFile.exists() || propFile.isDirectory()) {
            throw new RuntimeException("Config: invalid property file " + propFilePath);
        }
        Properties props = new Properties();
        InputStream input = new BufferedInputStream(new FileInputStream(propFilePath));
        props.load(input);

        // load host.list
        String hostFilePath = configPath + File.separator + HOST_LIST_FILE;
        List<String> hostList = NaginiFileUtils.read(hostFilePath);

        // load config from the above config files
        server.loadConfig(props, hostList);
        client.loadConfig(props);

        System.out.println("Config: loaded config from " + configPath);
    }

    public class ServerConfig {

        // server folders
        private static final String NAGINI_FOLDER = "nagini";
        private static final String CONFIG_FOLDER = "config";
        private static final String APPLICATION_FOLDER = "application";

        // server/config files
        private static final String CONFIG_APPLICATION_FOLDER = "application";

        // server/node folders
        private static final String NODE_PREFIX = "node_";
        private static final String NODE_CONFIG_FOLDER = "config";

        // server/node log files
        private static final String NODE_APPLICATION_LOG_FILE = "application.log";

        // server properties
        private static final String PARAM_USER_NAME = "server.user.name";
        private static final String PARAM_BASE_PATH = "server.base.path";
        private static final String PARAM_TEMP_PATH = "server.temp.path";
        private static final String PARAM_PORT_ID = "server.port.id";
        private static final String PARAM_WATCH_ENABLED = "server.watch.enabled";
        private static final String PARAM_JAVA_EXEC = "server.java.exec";

        // server application properties
        private static final String PARAM_APP_START_COMMAND = "server.app.start.command";
        private static final String PARAM_APP_JAVA_CLASS_REL_PATHS = "server.app.java.class.rel.paths";
        private static final String PARAM_APP_JAVA_CLASS_OPTIONS = "server.app.java.class.options";
        private static final String PARAM_APP_JAVA_MAIN_CLASS = "server.app.java.main.class";
        private static final String PARAM_APP_JVM_OPTIONS = "server.app.jvm.options";

        public Map<String, List<Integer>> mapHostToNodes;
        public Map<Integer, String> mapNodeToHost;

        public String userName;
        public String basePath;
        public String tempPath;
        public Integer portId;
        public Boolean watchEnabled;
        public String javaExec;

        public String appStartCommand;
        public List<String> appJavaClassSubPaths;
        public String appJavaMainClass;
        public String appJavaClassOpts;
        public String appJvmOpts;

        public void loadConfig(Properties props, List<String> hosts) {
            // load config from properties
            // hostList = Arrays.asList(nodeListValue.split("\\s*,\\s*"));
            userName = props.getProperty(PARAM_USER_NAME);
            basePath = props.getProperty(PARAM_BASE_PATH);
            tempPath = props.getProperty(PARAM_TEMP_PATH);
            portId = Integer.parseInt(props.getProperty(PARAM_PORT_ID));
            watchEnabled = Boolean.parseBoolean(props.getProperty(PARAM_WATCH_ENABLED,
                                                                  Boolean.toString(true)));
            javaExec = props.getProperty(PARAM_JAVA_EXEC, "java");

            if(props.containsKey(PARAM_APP_START_COMMAND)) {
                appStartCommand = props.getProperty(PARAM_APP_START_COMMAND);
                appJavaClassSubPaths = null;
                appJavaMainClass = null;
                appJavaClassOpts = null;
                appJvmOpts = null;
            } else {
                appStartCommand = null;
                appJavaClassSubPaths = Arrays.asList(props.getProperty(PARAM_APP_JAVA_CLASS_REL_PATHS)
                                                          .split("\\s*,\\s*"));
                appJavaMainClass = props.getProperty(PARAM_APP_JAVA_MAIN_CLASS);
                appJavaClassOpts = props.getProperty(PARAM_APP_JAVA_CLASS_OPTIONS, "");
                appJvmOpts = props.getProperty(PARAM_APP_JVM_OPTIONS, "");
            }

            // load config from host.list
            mapHostToNodes = Maps.newHashMap();
            mapNodeToHost = Maps.newHashMap();
            for(String hostInfo: hosts) {
                String[] info = hostInfo.split("\\s*,\\s*");
                if(info.length > 0) {
                    String hostName = info[0];
                    List<Integer> nodeIds = Lists.newArrayList();
                    for(int i = 1; i < info.length; ++i) {
                        Integer nodeId = Integer.parseInt(info[i]);
                        nodeIds.add(nodeId);
                        mapNodeToHost.put(nodeId, hostName);
                    }
                    mapHostToNodes.put(hostName, nodeIds);
                }
            }
        }

        public String getBasePath() {
            return this.basePath;
        }

        public String getTempPath() {
            return this.tempPath;
        }

        public String getNaginiPath() {
            return this.basePath + File.separator + ServerConfig.NAGINI_FOLDER;
        }

        public String getConfigPath() {
            return this.basePath + File.separator + ServerConfig.CONFIG_FOLDER;
        }

        public String getConfigApplicationPath() {
            return getConfigPath() + File.separator + ServerConfig.CONFIG_APPLICATION_FOLDER;
        }

        public String getApplicationName() {
            return ServerConfig.APPLICATION_FOLDER;
        }

        public String getApplicationPath() {
            return this.basePath + File.separator + ServerConfig.APPLICATION_FOLDER;
        }

        public String getNodePath(Integer nodeId) {
            return this.basePath + File.separator + ServerConfig.NODE_PREFIX + nodeId.toString();
        }

        public String getNodeConfigPath(Integer nodeId) {
            return getNodePath(nodeId) + File.separator + ServerConfig.NODE_CONFIG_FOLDER;
        }

        public String getNodeApplicationLogFilePath(Integer nodeId) {
            return getNodePath(nodeId) + File.separator + ServerConfig.NODE_APPLICATION_LOG_FILE;
        }

        public String expandBasePath(String path) {
            return path.replace("$", this.basePath);
        }

        public String expandNodePath(String path, Integer nodeId) {
            return path.replace("#", getNodePath(nodeId));
        }
    }

    public class ClientConfig {

        // client properties
        private static final String PARAM_BASE_PATH = "client.base.path";
        private static final String PARAM_TEMP_PATH = "client.temp.path";

        // client application properties
        private static final String PARAM_JAVA_EXEC = "client.java.exec";
        private static final String PARAM_APP_PACKET_PATH = "client.app.packet.path";
        private static final String PARAM_APP_FETCH_COMMAND = "client.app.fetch.command";
        private static final String PARAM_APP_BUILD_COMMAND = "client.app.build.command";
        private static final String PARAM_APP_BUILD_OUTPUT_REL_PATHS = "client.app.build.output.rel.paths";

        public String basePath;
        public String tempPath;
        public String javaExec;
        public String appPacketPath;
        public String appFetchCommand;
        public String appBuildCommand;
        public List<String> appBuildOutputSubPaths;

        public void loadConfig(Properties props) {
            basePath = props.getProperty(PARAM_BASE_PATH);
            tempPath = props.getProperty(PARAM_TEMP_PATH);
            javaExec = props.getProperty(PARAM_JAVA_EXEC, "java");
            appPacketPath = props.getProperty(PARAM_APP_PACKET_PATH);
            appFetchCommand = props.getProperty(PARAM_APP_FETCH_COMMAND, "");
            appBuildCommand = props.getProperty(PARAM_APP_BUILD_COMMAND, "");
            appBuildOutputSubPaths = Arrays.asList(props.getProperty(PARAM_APP_BUILD_OUTPUT_REL_PATHS,
                                                                     ".")
                                                        .split("\\s*,\\s*"));
        }
    }
}
