
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-07
 */

#ifndef SERVER_SESSION_H
#define SERVER_SESSION_H

#include "game.h"
#include "buffer.h"

#define SESSION_BUFFER_DEFAULT_CAPACITY 8

typedef struct _Session {

    int socket_fd;

    Game* game;

    Buffer to_send;

} Session;

void session_init(Session *session, int socket_fd);
void session_free(Session* session);

#endif
