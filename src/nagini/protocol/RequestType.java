package nagini.protocol;

public class RequestType {

    // Server Control Requests
    public static final int REQUEST_PING = 0x00000000;
    public static final int REQUEST_STOP = 0x00000001;
    // [UTF:ServerConfigPath]
    public static final int REQUEST_RECONFIG = 0x00000002;

    // File Operation Requests
    // [UTF:DestiationPath][Long:Length][Byte[]:FileContent]
    public static final int REQUEST_FILE_PUT = 0x00000010;
    // [UTF:SourcePath]
    public static final int REQUEST_FILE_GET = 0x00000011;
    // [UTF:SourcePath]
    public static final int REQUEST_FILE_DELETE = 0x00000012;

    // Service Operation Requests
    // [Int:NodeId]
    public static final int REQUEST_SERVICE_START_APPLICATION = 0x00000020;
    // [Int:NodeId]
    public static final int REQUEST_SERVICE_STOP_APPLICATION = 0x00000021;
    // [Int:NodeId][Int:0/Tail]
    public static final int REQUEST_SERVICE_WATCH_APPLICATION = 0x00000022;
}
