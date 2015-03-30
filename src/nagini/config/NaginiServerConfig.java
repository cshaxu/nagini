package nagini.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class NaginiServerConfig {

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
        userName = props.getProperty(PARAM_USER_NAME);
        basePath = props.getProperty(PARAM_BASE_PATH);
        tempPath = props.getProperty(PARAM_TEMP_PATH).replace("$", this.basePath);
        javaExec = props.getProperty(PARAM_JAVA_EXEC, "java").replace("$", this.basePath);

        portId = Integer.parseInt(props.getProperty(PARAM_PORT_ID));
        watchEnabled = Boolean.parseBoolean(props.getProperty(PARAM_WATCH_ENABLED,
                                                              Boolean.toString(true)));

        if(props.containsKey(PARAM_APP_START_COMMAND)) {
            appStartCommand = props.getProperty(PARAM_APP_START_COMMAND)
                                   .replace("$", this.basePath);
            appJavaClassSubPaths = null;
            appJavaMainClass = null;
            appJavaClassOpts = null;
            appJvmOpts = null;
        } else {
            appStartCommand = null;
            appJavaClassSubPaths = Arrays.asList(props.getProperty(PARAM_APP_JAVA_CLASS_REL_PATHS)
                                                      .split("\\s*,\\s*"));
            appJavaMainClass = props.getProperty(PARAM_APP_JAVA_MAIN_CLASS);
            appJavaClassOpts = props.getProperty(PARAM_APP_JAVA_CLASS_OPTIONS, "")
                                    .replace("$", this.basePath);
            appJvmOpts = props.getProperty(PARAM_APP_JVM_OPTIONS, "").replace("$", this.basePath);
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
        return this.basePath + File.separator + NaginiServerConfig.NAGINI_FOLDER;
    }

    public String getConfigPath() {
        return this.basePath + File.separator + NaginiServerConfig.CONFIG_FOLDER;
    }

    public String getConfigApplicationPath() {
        return getConfigPath() + File.separator + NaginiServerConfig.CONFIG_APPLICATION_FOLDER;
    }

    public String getApplicationName() {
        return NaginiServerConfig.APPLICATION_FOLDER;
    }

    public String getApplicationPath() {
        return this.basePath + File.separator + NaginiServerConfig.APPLICATION_FOLDER;
    }

    public String getNodePath(Integer nodeId) {
        return this.basePath + File.separator + NaginiServerConfig.NODE_PREFIX + nodeId.toString();
    }

    public String getNodeConfigPath(Integer nodeId) {
        return getNodePath(nodeId) + File.separator + NaginiServerConfig.NODE_CONFIG_FOLDER;
    }

    public String getNodeApplicationLogFilePath(Integer nodeId) {
        return getNodePath(nodeId) + File.separator + NaginiServerConfig.NODE_APPLICATION_LOG_FILE;
    }

    public String expandNodePath(String path, Integer nodeId) {
        return path.replace("#", getNodePath(nodeId));
    }
}
