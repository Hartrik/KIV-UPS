package cz.hartrik.puzzle.net;

import java.util.Random;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Patrik Harag
 * @version 2017-10-12
 */
public class RandomMessagesTest {

    private static final Random RANDOM = new Random(65962052);

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