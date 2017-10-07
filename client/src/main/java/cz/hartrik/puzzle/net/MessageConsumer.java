package cz.hartrik.puzzle.net;

import java.util.function.Consumer;

/**
 *
 * @author Patrik Harag
 * @version 2017-10-07
 */
public interface MessageConsumer extends Consumer<String> {

    boolean isTemporary();


    static MessageConsumer temporary(Consumer<String> consumer) {
        return create(consumer, true);
    }

    static MessageConsumer persistant(Consumer<String> consumer) {
        return create(consumer, false);
    }

    static MessageConsumer create(Consumer<String> consumer, boolean temporary) {
        return new MessageConsumer() {
            @Override
            public boolean isTemporary() {
                return temporary;
            }

            @Override
            public void accept(String s) {
                consumer.accept(s);
            }
        };
    }

}
