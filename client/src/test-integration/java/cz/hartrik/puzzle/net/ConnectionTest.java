package cz.hartrik.puzzle.net;

import cz.hartrik.common.Exceptions;
import cz.hartrik.puzzle.net.protocol.JoinGameResponse;
import cz.hartrik.puzzle.net.protocol.LogInResponse;
import cz.hartrik.puzzle.net.protocol.LogOutResponse;
import cz.hartrik.puzzle.net.protocol.NewGameResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author Patrik Harag
 * @version 2017-10-15
 */
public class ConnectionTest {

    private static final int DEFAULT_TIMEOUT = 1000;

    // GENERAL

    @Test(expected = Exception.class)
    public void testNoActivity() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Thread.sleep(5_000);
        // server must recognize /dead/ client

        connection.sendPing();
        connection.close();
    }

    @Test
    public void testNotClosed() throws Exception {
        ConnectionProvider.connect();
        // server must recognize /dead/ client
    }

    @Test
    public void testLongConnection() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {

            connection.addConsumer("PIN", MessageConsumer.persistant(s -> {
                Exceptions.silent(connection::sendPing);
            }));

            Thread.sleep(5_000);
        }
    }

    // LOG IN

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testLogIn() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Future<LogInResponse> response = connection.sendLogIn("Test99");
        assertThat(response.get(), is(LogInResponse.OK));

        connection.close();
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testLogInNameTooLong() throws Exception {
        Connection connection = ConnectionProvider.connect();

        String n = "123456789012345678901234567890";
        Future<LogInResponse> response = connection.sendLogIn(n);
        assertThat(response.get(), is(LogInResponse.NAME_TOO_LONG));

        connection.close();
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testLogInWrongChars() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Future<LogInResponse> response = connection.sendLogIn("Te\ts*/t");
        assertThat(response.get(), is(LogInResponse.UNSUPPORTED_CHARS));

        connection.close();
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testLogInTwice() throws Exception {
        Connection connection = ConnectionProvider.connect();

        Future<LogInResponse> response1 = connection.sendLogIn("Test99");
        assertThat(response1.get(), is(LogInResponse.OK));

        Future<LogInResponse> response2 = connection.sendLogIn("Other");
        assertThat(response2.get(), is(LogInResponse.ALREADY_LOGGED));

        connection.close();
    }

    // LOG OUT

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testLogOut() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {

            Future<LogInResponse> inRes = connection.sendLogIn("Test");
            assertThat(inRes.get(), is(LogInResponse.OK));

            Future<LogOutResponse> outRes = connection.sendLogOut();
            assertThat(outRes.get(), is(LogOutResponse.OK));
        }
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testLogOutWithoutLogIn() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {
            Future<LogOutResponse> outRes = connection.sendLogOut();
            assertThat(outRes.get(), is(LogOutResponse.OK));
        }
    }

    // NEW GAME

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testNewGame() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {
            Future<LogInResponse> response = connection.sendLogIn("Nick");
            assertThat(response.get(), is(LogInResponse.OK));

            Future<NewGameResponse> game = connection.sendNewGame(10, 5);
            assertThat(game.get().getStatus(), is(NewGameResponse.Status.OK));
            assertThat(game.get().getGameID() >= 0, is(true));
        }
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testNewGameNotLogged() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {
            Future<NewGameResponse> game = connection.sendNewGame(10, 5);
            assertThat(game.get().getStatus(), is(NewGameResponse.Status.NO_PERMISSIONS));
        }
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testNewGameWrongFormat() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {
            Future<LogInResponse> response = connection.sendLogIn("Nick");
            assertThat(response.get(), is(LogInResponse.OK));

            List<String> messages = Arrays.asList(
                    "",
                    ",",
                    "10,-2",
                    "-1,2",
                    "1000000,20",
                    "1,10",
                    "10",
                    "10,10g"
            );

            for (String message : messages) {
                Future<String> game = connection.sendMessageAndHook("GNW", message);
                assertThat(game.get().matches("-[12]"), is(true));
            }
        }
    }

    // JOIN

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testNewGameAndJoin() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {
            Future<LogInResponse> response = connection.sendLogIn("Nick");
            assertThat(response.get(), is(LogInResponse.OK));

            Future<NewGameResponse> game = connection.sendNewGame(10, 5);
            assertThat(game.get().getStatus(), is(NewGameResponse.Status.OK));
            assertThat(game.get().getGameID() >= 0, is(true));

            Future<JoinGameResponse> join = connection.sendJoinGame(game.get().getGameID());
            assertThat(join.get(), is(JoinGameResponse.OK));
        }
    }

}