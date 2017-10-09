
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-09
 */

#ifndef SERVER_SESSION_H
#define SERVER_SESSION_H

#include <stdbool.h>
#include "game.h"
#include "buffer.h"

#define SESSION_BUFFER_DEFAULT_CAPACITY 8
#define SESSION_PLAYER_MAX_NAME_LENGTH 12

typedef struct _Session {

    int socket_fd;
    char name[SESSION_PLAYER_MAX_NAME_LENGTH + 1];
    Game* game;

    Buffer to_send;

} Session;

void session_init(Session *session, int socket_fd);
void session_free(Session* session);

bool session_is_logged(Session* session);
bool session_is_in_game(Session* session);

#endif
