
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-17
 */

#include <memory.h>
#include "session.h"

void session_init(Session *session, int socket_fd) {
    session->status = SESSION_STATUS_CONNECTED;
    session->socket_fd = socket_fd;
    session->last_activity = 0;
    session->last_ping = 0;
    memset(session->name, 0, sizeof(session->name));
    session->game = NULL;
    buffer_init(&(session->to_send), SESSION_BUFFER_DEFAULT_CAPACITY);
}

void session_free(Session* session) {
    buffer_free(&(session->to_send));
}

bool session_is_logged(Session* session) {
    return session->name[0] != 0;
}

bool session_is_in_game(Session* session) {
    return session_is_logged(session) && session->game != NULL;
}