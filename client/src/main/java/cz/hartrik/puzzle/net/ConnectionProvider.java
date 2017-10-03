package cz.hartrik.puzzle.net;


import cz.hartrik.common.Exceptions;

/**
 *
 * @author Patrik Harag
 * @version 2017-10-03
 */
public class ConnectionProvider {

    private static final int PORT = 8076;
    private static final String HOST = "localhost";

    public static Connection connect() {
        Connection connection = new Connection(HOST, PORT);
        Exceptions.unchecked(connection::connect);

        return connection;
    }

}
