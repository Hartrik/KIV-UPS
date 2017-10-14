package cz.hartrik.puzzle.net;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 *
 * @author Patrik Harag
 * @version 2017-10-14
 */
public class ReaderThread extends Thread {

    private final Map<String, Queue<MessageConsumer>> consumers;
    private final Consumer<Exception> onException;
    private volatile Reader reader;
    private volatile boolean close = false;

    public ReaderThread(Consumer<Exception> onException) {
        this.onException = onException;
        this.consumers = new ConcurrentHashMap<>();
    }

    public synchronized void initStream(InputStream inputStream, Charset charset) {
        if (reader != null)
            throw new IllegalStateException("Stream already initialized");

        this.reader = new InputStreamReader(inputStream, charset);
    }

    @Override
    public void run() {
        if (reader == null)
            throw new IllegalStateException("Stream not initialized!");

        try {
            StringBuilder builder = new StringBuilder();
            while (true) {
                int c = reader.read();
                if (c == -1) {
                    return;  // stream closed
                }

                if (c == '|') {
                    if (builder.length() > 0) {
                        processMessage(builder.toString());
                        builder.setLength(0);
                    }
                } else {
                    builder.append((char) c);
                }
            }
        } catch (Exception e) {
            onException.accept(e);
        }
    }

    private void processMessage(String data) {
        System.out.println("<-- " + data);

        if (data.length() < 3) {
            System.err.println("Message too short: " + data);
        } else {
            String type = data.substring(0, 3);
            String content = data.substring(3);

            Queue<MessageConsumer> queue = this.consumers.get(type);
            if (queue != null && !queue.isEmpty()) {
                MessageConsumer consumer = queue.element();
                consumer.accept(content);

                if (consumer.isTemporary()) {
                    queue.poll();
                }
            }
        }
    }

    public void addConsumer(String type, MessageConsumer contentConsumer) {
        Queue<MessageConsumer> queue = consumers
                .getOrDefault(type, new ConcurrentLinkedQueue<>());

        queue.add(contentConsumer);
        consumers.put(type, queue);
    }

    public void close() {
        this.close = true;  // TODO
    }

}
