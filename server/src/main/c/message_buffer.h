
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-03
 */

#ifndef SERVER_MESSAGE_BUFFER_H
#define SERVER_MESSAGE_BUFFER_H

#define MESSAGE_BUFFER_INC_RATIO 2

typedef struct _MessageBuffer {
    int index;
    size_t content_length;
    char *content;
} MessageBuffer;

/**
 * Adds char at the end of a buffer.
 *
 * @param buffer buffer
 * @param c char
 */
void message_buffer_add(MessageBuffer* buffer, char c);

/**
 * Gets message type from a buffer.
 *
 * @param buffer buffer
 * @return type
 */
char* message_buffer_get_type(MessageBuffer* buffer);


/**
 * Gets message content from a buffer.
 *
 * @param buffer buffer
 * @return content
 */
char* message_buffer_get_content(MessageBuffer* buffer);

#endif
