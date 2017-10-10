package cz.hartrik.puzzle.net;

/**
 *
 * @author Patrik Harag
 * @version 2017-10-09
 */
public class ConnectionProvider {

    private static final int PORT = 8076;
    private static final String HOST = "localhost";

    public static Connection connect() throws Exception {
        Connection connection = new Connection(HOST, PORT);
        connection.connect();

        return connection;
    }

}
