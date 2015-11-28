package nagini.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import nagini.utils.NaginiFileUtils;

public class NaginiConfig {

    private static final String NAGINI_PROPERTIES_FILE = "nagini.properties";
    public static final String HOST_LIST_FILE = "host.list";

    public NaginiServerConfig server;
    public NaginiClientConfig client;

    public NaginiConfig() {
        server = new NaginiServerConfig();
        client = new NaginiClientConfig();
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

}
