package cz.hartrik.puzzle.net;

import cz.hartrik.common.Exceptions;
import java.util.function.Consumer;

/**
 * @author Patrik Harag
 * @version 2017-10-30
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

    public void setOnConnectionLost(Runnable onConnectionLost) {
        connection.setOnConnectionLost(onConnectionLost);
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

    public Thread asyncFinally(Command command, Consumer<Exception> after) {
        Thread thread = new Thread(() -> {
            Exception exception = null;
            try {
                command.apply(connection);
            } catch (Exception e) {
                exception = e;
            } finally {
                after.accept(exception);
            }
        }, "ConnectionHolder/Command/" + (++ID));
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    public void addConsumer(String type, MessageConsumer consumer) {
        connection.addConsumer(type, consumer);
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

}
