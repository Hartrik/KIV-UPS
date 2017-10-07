
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-07
 */

#include <stdlib.h>
#include "session.h"

void session_init(Session *session, int socket_fd) {
    session->socket_fd = socket_fd;
    session->game = NULL;
    buffer_init(&(session->to_send), SESSION_BUFFER_DEFAULT_CAPACITY);
}

void session_free(Session* session) {
    buffer_free(&(session->to_send));
}
