package cz.hartrik.puzzle.net;

import java.util.concurrent.Future;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author Patrik Harag
 * @version 2017-10-07
 */
public class ConnectionTest {

    @Test
    public void testLogin() throws Exception {
        Connection connection = ConnectionProvider.connect();

        connection.sendLogin("My nick");
        connection.close();
    }

    @Test(expected = Exception.class)
    public void testNoActivity() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Thread.sleep(5_000);
        // server must recognize /dead/ client

        connection.sendLogin("Nick");
        connection.close();
    }

    @Test
    public void testNotClosed() throws Exception {
        ConnectionProvider.connect();
        // server must recognize /dead/ client
    }

    @Test
    public void testNewGame() throws Exception {
        Connection connection = ConnectionProvider.connect();
        connection.sendLogin("Nick");
        Future<String> game = connection.sendNewGame();

        assertThat(game.get().matches("[0-9]+"), is(true));

        connection.close();
    }

}