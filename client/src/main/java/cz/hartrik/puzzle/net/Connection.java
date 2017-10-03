package cz.hartrik.puzzle.net;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Patrik Harag
 * @version 2017-10-03
 */
public class Connection implements AutoCloseable {

    private static final Charset CHARSET = StandardCharsets.US_ASCII;


    private final String host;
    private final int port;

    private Socket socket;
    private Reader reader;
    private Writer writer;

    Connection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    void connect() throws IOException {
        this.socket = new Socket(host, port);

        // create streams for reading and writing
        this.reader = new InputStreamReader(socket.getInputStream(), CHARSET);
        this.writer = new OutputStreamWriter(socket.getOutputStream(), CHARSET);
    }

    private void msg(String type, String content) throws IOException {
        // TODO: escaping
        writer.write(String.format("|%s%s|", type, content));
        writer.flush();
    }

    public void login(String nick) throws IOException {
        msg("LOG", nick);
    }

    @Override
    public void close() throws IOException {
        msg("BYE", "");

        socket.close();
    }
}
