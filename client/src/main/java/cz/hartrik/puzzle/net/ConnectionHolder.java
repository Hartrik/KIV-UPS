package cz.hartrik.puzzle.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Patrik Harag
 * @version 2017-10-10
 */
public class ConnectionHolder implements AutoCloseable {

    public interface Command {
        void apply(Connection c) throws Exception;
    }

    private static volatile int ID = 0;

    private final Connection connection;
    private final ExecutorService executor;

    public ConnectionHolder(Connection connection) {
        this.connection = connection;
        this.executor = Executors.newSingleThreadExecutor(
                r -> new Thread(r, ConnectionHolder.class.toString() + "/" + ID++));
    }

    public void async(Command command, Consumer<Exception> onError) {
        executor.execute(() -> {
            try {
                command.apply(connection);
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

    @Override
    public void close() throws Exception {
        executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
    }

}
