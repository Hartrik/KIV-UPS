package cz.hartrik.puzzle.net;

import java.util.Arrays;
import java.util.Random;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Patrik Harag
 * @version 2017-10-28
 */
public class ServerStabilityTest {

    private static final Random RANDOM = new Random(65962052);

    @Test
    public void testLongMessage() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {
            char[] chars = new char[1024*10];
            Arrays.fill(chars, 'a');
            connection.sendLogIn(new String(chars));
        }
    }

    @Test
    public void testRandomMessages() throws Exception {
        try (Connection connection = ConnectionProvider.connect()) {
            for (int s = 1; s <= 10; s++) {
                for (int j = 0; j < 10; j++) {
                    connection.sendRaw(randomString(s));
                }
            }
        }
    }

    @Test
    public void testSemiRandomMessages() throws Exception {
        String[] types = new String[] {
                "NOP", "LOF", "LIN", "GAM", "NEW"
        };

        try (Connection connection = ConnectionProvider.connect()) {
            for (String type : types) {
                for (int j = 0; j < 10; j++) {
                    connection.sendMessage(type, randomString(j));
                }
            }
        }
    }

    private String randomString(int size) {
        StringBuilder builder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            builder.append((char) RANDOM.nextInt(128));
        }
        return builder.toString();
    }

}