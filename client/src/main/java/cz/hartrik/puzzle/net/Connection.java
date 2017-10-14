package cz.hartrik.puzzle.net;

import cz.hartrik.common.Exceptions;
import cz.hartrik.puzzle.net.protocol.LogInResponse;
import cz.hartrik.puzzle.net.protocol.LogOutResponse;
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author Patrik Harag
 * @version 2017-10-14
 */
public class Connection implements AutoCloseable {

    private static final Charset CHARSET = StandardCharsets.US_ASCII;


    private final String host;
    private final int port;
    private boolean connected;

    private Socket socket;

    private ReaderThread reader;
    private WriterThread writer;

    private volatile Exception exception;

    Connection(String host, int port) {
        this.host = host;
        this.port = port;
        this.reader = new ReaderThread(this::onException);
        this.writer = new WriterThread(this::onException);
    }

    synchronized void connect() throws IOException {
        if (connected) return;  // already connected
        this.connected = true;

        this.exception = null;
        this.socket = new Socket(host, port);

        this.reader.initStream(socket.getInputStream(), CHARSET);
        this.writer.initStream(socket.getOutputStream(), CHARSET);

        reader.start();
        writer.start();
    }

    public void addConsumer(String type, MessageConsumer consumer) {
        this.reader.addConsumer(type, consumer);
    }

    private void onException(Exception e) {
        if (exception != null) return;  // exception from second thread...

        this.exception = e;
        e.printStackTrace();
        Exceptions.silent(this::closeNow);
    }

    private void send(String content) throws Exception {
        if (exception != null)
            throw exception;

        // TODO: escaping
        writer.send(content);
    }

    void sendMessage(String type, String content) throws Exception {
        send(String.format("|%s%s|", type, content));
    }

    void sendRaw(String data) throws Exception {
        send(String.format("|%s|", data));
    }

    public void sendPing() throws Exception {
        connect();

        sendMessage("PIN", "");
    }

    public Future<LogInResponse> sendLogIn(String nick) throws Exception {
        connect();

        CompletableFuture<LogInResponse> future = new CompletableFuture<>();
        reader.addConsumer("LIN", MessageConsumer.temporary(response -> {
            future.complete(LogInResponse.parse(response));
        }));

        sendMessage("LIN", nick);

        return future;
    }

    public Future<LogOutResponse> sendLogOut() throws Exception {
        connect();

        CompletableFuture<LogOutResponse> future = new CompletableFuture<>();
        reader.addConsumer("LOF", MessageConsumer.temporary(response -> {
            future.complete(LogOutResponse.parse(response));
        }));

        sendMessage("LOF", "");

        return future;
    }

    public Future<String> sendNewGame() throws Exception {
        connect();

        CompletableFuture<String> future = new CompletableFuture<>();
        reader.addConsumer("GAM", MessageConsumer.temporary(future::complete));

        sendMessage("NEW", "");

        return future;
    }

    /**
     * Closes connection, blocks.
     */
    @Override
    public synchronized void close() throws Exception {
        if (exception != null || !connected) {
            // closed already
        } else {
            sendMessage("BYE", "");
            closeNow();

            if (exception != null) {
                // exception occurred
                throw exception;
            }
        }
    }

    private void closeNow() throws Exception {
        writer.close();
        reader.close();

        Exceptions.silent(() -> writer.join(500));
        Exceptions.silent(() -> reader.join(500));

        socket.close();
    }
}
