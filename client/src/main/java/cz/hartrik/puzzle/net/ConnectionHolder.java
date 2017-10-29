package cz.hartrik.puzzle.net;

import cz.hartrik.common.Exceptions;
import java.util.function.Consumer;

/**
 * @author Patrik Harag
 * @version 2017-10-29
 */
public class ConnectionHolder implements AutoCloseable {

    public interface Command {
        void apply(Connection c) throws Exception;
    }

    private static volatile int ID = 0;

    private final Connection connection;

    public ConnectionHolder(Connection connection) {
        this.connection = connection;

        connection.addConsumer("PIN", MessageConsumer.persistant(s -> {
            Exceptions.silent(connection::sendPing);
        }));
    }

    public Thread async(Command command, Consumer<Exception> onError) {
        Thread thread = new Thread(() -> {
            try {
                command.apply(connection);
            } catch (Exception e) {
                onError.accept(e);
            }
        }, "ConnectionHolder/Command/" + (++ID));
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

}
