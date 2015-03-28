package nagini.protocol;

public class ResponseType {

    public static final int RESPONSE_NOOP = 0x00000000;
    // [UTF:Header][UTF:Message]
    public static final int RESPONSE_SUCCESS = 0x00000001;
    // [UTF:Header][UTF:Message]
    public static final int RESPONSE_FAIL = 0x00000002;
    // [Long:Length][Byte[]:FileContent]
    public static final int RESPONSE_FILE = 0x00000010;
}
