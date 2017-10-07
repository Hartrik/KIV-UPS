package cz.hartrik.puzzle.net;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 *
 * @author Patrik Harag
 * @version 2017-10-07
 */
public class WriterThread extends Thread {

    private final Writer writer;
    private final Consumer<Exception> onException;
    private final BlockingQueue<String> queue;
    private volatile boolean close = false;

    public WriterThread(OutputStream outputStream, Charset charset,
                        Consumer<Exception> onException) {

        this.writer = new OutputStreamWriter(outputStream, charset);
        this.onException = onException;
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        try {
            while (!close || !queue.isEmpty()) {
                String data = queue.poll(500, TimeUnit.MILLISECONDS);
                if (data != null) {
                    writeData(data);
                }
            }
        } catch (Exception e) {
            onException.accept(e);
        }
    }

    private void writeData(String data) throws IOException {
        writer.write(data);
        writer.flush();

        System.out.println("<-- " + data);
    }

    public void send(String data) {
        if (!queue.add(data))
            throw new RuntimeException();
    }

    public void close() {
        this.close = true;
    }

}
