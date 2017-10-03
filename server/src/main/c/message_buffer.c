
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-03
 */

#include <stdlib.h>
#include "protocol.h"
#include "message_buffer.h"


void message_buffer_add(MessageBuffer* buffer, char c) {
    if (buffer->index >= buffer->content_length) {
        /* we have to increase the buffer */
        size_t new_size = buffer->content_length * MESSAGE_BUFFER_INC_RATIO;
        buffer->content = realloc(buffer->content, new_size);
        buffer->content_length = new_size;
    }

    buffer->content[buffer->index++] = c;
}

char* message_buffer_get_type(MessageBuffer* buffer) {
    return buffer->content;
}

char* message_buffer_get_content(MessageBuffer* buffer) {
    int pos = PROTOCOL_TYPE_SIZE + 1;

    if (pos < buffer->index)
        return buffer->content + pos;
    else
        return NULL;  // no content
}