package cz.hartrik.puzzle.net;

import cz.hartrik.puzzle.net.protocol.LogInResponse;
import java.util.concurrent.Future;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author Patrik Harag
 * @version 2017-10-09
 */
public class ConnectionTest {

    // GENERAL

    @Test(expected = Exception.class)
    public void testNoActivity() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Thread.sleep(5_000);
        // server must recognize /dead/ client

        connection.sendNop();
        connection.close();
    }

    @Test
    public void testNotClosed() throws Exception {
        ConnectionProvider.connect();
        // server must recognize /dead/ client
    }

    // LOGIN

    @Test
    public void testLogin() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Future<LogInResponse> response = connection.sendLogin("Test99");
        assertThat(response.get(), is(LogInResponse.OK));

        connection.close();
    }

    @Test
    public void testLoginNameTooLong() throws Exception {
        Connection connection = ConnectionProvider.connect();

        String n = "123456789012345678901234567890";
        Future<LogInResponse> response = connection.sendLogin(n);
        assertThat(response.get(), is(LogInResponse.NAME_TOO_LONG));

        connection.close();
    }

    @Test
    public void testLoginWrongChars() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Future<LogInResponse> response = connection.sendLogin("Te\ts*/t");
        assertThat(response.get(), is(LogInResponse.UNSUPPORTED_CHARS));

        connection.close();
    }

    @Test
    public void testLoginTwice() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Future<LogInResponse> response1 = connection.sendLogin("Test99");
        assertThat(response1.get(), is(LogInResponse.OK));

        Future<LogInResponse> response2 = connection.sendLogin("Other");
        assertThat(response2.get(), is(LogInResponse.ALREADY_LOGGED));

        connection.close();
    }

    // NEW GAME

    @Test
    public void testNewGame() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {
            Future<LogInResponse> response = connection.sendLogin("Nick");
            assertThat(response.get(), is(LogInResponse.OK));

            Future<String> game = connection.sendNewGame();
            assertThat(game.get().matches("[0-9]+"), is(true));
        }
    }

}