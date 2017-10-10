package cz.hartrik.puzzle.net;

import cz.hartrik.puzzle.net.protocol.LogInResponse;
import cz.hartrik.puzzle.net.protocol.LogOutResponse;
import java.util.concurrent.Future;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author Patrik Harag
 * @version 2017-10-10
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

    // LOG IN

    @Test
    public void testLogIn() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Future<LogInResponse> response = connection.sendLogIn("Test99");
        assertThat(response.get(), is(LogInResponse.OK));

        connection.close();
    }

    @Test
    public void testLogInNameTooLong() throws Exception {
        Connection connection = ConnectionProvider.connect();

        String n = "123456789012345678901234567890";
        Future<LogInResponse> response = connection.sendLogIn(n);
        assertThat(response.get(), is(LogInResponse.NAME_TOO_LONG));

        connection.close();
    }

    @Test
    public void testLogInWrongChars() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Future<LogInResponse> response = connection.sendLogIn("Te\ts*/t");
        assertThat(response.get(), is(LogInResponse.UNSUPPORTED_CHARS));

        connection.close();
    }

    @Test
    public void testLogInTwice() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Future<LogInResponse> response1 = connection.sendLogIn("Test99");
        assertThat(response1.get(), is(LogInResponse.OK));

        Future<LogInResponse> response2 = connection.sendLogIn("Other");
        assertThat(response2.get(), is(LogInResponse.ALREADY_LOGGED));

        connection.close();
    }

    // LOG OUT

    @Test
    public void testLogOut() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {

            Future<LogInResponse> inRes = connection.sendLogIn("Test");
            assertThat(inRes.get(), is(LogInResponse.OK));

            Future<LogOutResponse> outRes = connection.sendLogOut();
            assertThat(outRes.get(), is(LogOutResponse.OK));
        }
    }

    @Test
    public void testLogOutWithoutLogIn() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {
            Future<LogOutResponse> outRes = connection.sendLogOut();
            assertThat(outRes.get(), is(LogOutResponse.OK));
        }
    }

    // NEW GAME

    @Test
    public void testNewGame() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {
            Future<LogInResponse> response = connection.sendLogIn("Nick");
            assertThat(response.get(), is(LogInResponse.OK));

            Future<String> game = connection.sendNewGame();
            assertThat(game.get().matches("[0-9]+"), is(true));
        }
    }

}