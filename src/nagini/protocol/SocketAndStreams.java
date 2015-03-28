package nagini.protocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A wrapper class that wraps a socket with its DataInputStream and
 * DataOutputStream
 */
public class SocketAndStreams {

    private static final int DEFAULT_BUFFER_SIZE = 65536;

    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public SocketAndStreams(String hostName, int portId) throws IOException {
        this(new Socket(hostName, portId));
    }

    public SocketAndStreams(Socket socket) throws IOException {
        this(socket, DEFAULT_BUFFER_SIZE);
    }

    public SocketAndStreams(Socket socket, int bufferSizeBytes) throws IOException {
        this.socket = socket;
        this.inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream(),
                                                                       bufferSizeBytes));
        this.outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(),
                                                                          bufferSizeBytes));
    }

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public void close() throws IOException {
        if(socket != null) {
            socket.close();
        }
    }
}
