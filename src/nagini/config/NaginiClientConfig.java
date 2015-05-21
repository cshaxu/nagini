package nagini.config;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class NaginiClientConfig {

    // client properties
    private static final String PARAM_BASE_PATH = "client.base.path";
    private static final String PARAM_TEMP_PATH = "client.temp.path";

    // client application properties
    private static final String PARAM_JAVA_EXEC = "client.java.exec";
    private static final String PARAM_APP_PACKET_PATH = "client.app.packet.path";
    private static final String PARAM_APP_FETCH_COMMAND = "client.app.fetch.command";
    private static final String PARAM_APP_REFRESH_COMMAND = "client.app.refresh.command";
    private static final String PARAM_APP_BUILD_COMMAND = "client.app.build.command";
    private static final String PARAM_APP_BUILD_OUTPUT_REL_PATHS = "client.app.build.output.rel.paths";

    public String basePath;
    public String tempPath;
    public String javaExec;
    public String appPacketPath;
    public String appFetchCommand;
    public String appRefreshCommand;
    public String appBuildCommand;
    public List<String> appBuildOutputSubPaths;

    public void loadConfig(Properties props) {
        basePath = props.getProperty(PARAM_BASE_PATH);
        tempPath = props.getProperty(PARAM_TEMP_PATH).replace("$", this.basePath);
        javaExec = props.getProperty(PARAM_JAVA_EXEC, "java").replace("$", this.basePath);
        appPacketPath = props.getProperty(PARAM_APP_PACKET_PATH).replace("$", this.basePath);
        appFetchCommand = props.getProperty(PARAM_APP_FETCH_COMMAND, "")
                               .replace("$", this.basePath);
        appRefreshCommand = props.getProperty(PARAM_APP_REFRESH_COMMAND)
                               .replace("$", this.basePath);
        appBuildCommand = props.getProperty(PARAM_APP_BUILD_COMMAND, "")
                               .replace("$", this.basePath);
        appBuildOutputSubPaths = Arrays.asList(props.getProperty(PARAM_APP_BUILD_OUTPUT_REL_PATHS,
                                                                 ".").split("\\s*,\\s*"));
    }
}
