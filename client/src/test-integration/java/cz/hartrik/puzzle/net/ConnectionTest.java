package cz.hartrik.puzzle.net;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author Patrik Harag
 * @version 2017-10-05
 */
public class ConnectionTest {

    @Test
    public void testLogin() throws Exception {
        Connection connection = ConnectionProvider.connect();

        connection.login("My nick");
        connection.close();
    }

    @Test(expected = Exception.class)
    public void testNoActivity() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Thread.sleep(1000);
        // server must recognize /dead/ client

        connection.login("Nick");
        connection.close();
    }

    @Test
    public void testNotClosed() throws Exception {
        ConnectionProvider.connect();
        // server must recognize /dead/ client
    }

}