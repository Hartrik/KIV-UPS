
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-07
 */

#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <stdbool.h>
#include "controller.h"
#include "protocol.h"
#include "game_pool.h"

bool process_message(Session* session, char* type, char* content) {
    printf("  [%d] Message: type='%s' content='%s'\n",
           session->socket_fd, type, content);
    fflush(stdout);

    if (strncmp(type, "BYE", 3) == 0) {
        return true;

    } else if (strncmp(type, "NOP", 3) == 0) {
        // nothing

    } else if (strncmp(type, "LOG", 3) == 0) {
        // TODO: login

    } else if (strncmp(type, "NEW", 3) == 0) {
        Game* game = gp_create_game(content);
        session->game = game;

        controller_send_int(session, "GAM", game == NULL ? -1 : game->id);

    } else {
        printf("  [%d] Unknown command: %s\n", session->socket_fd, type);
    }

    return false;
}

void controller_send(Session* session, char* type, char* content) {
    assert(strlen(type) == 3);

    Buffer* buffer = &(session->to_send);

    buffer_add(buffer, PROTOCOL_MESSAGE_SEP);
    buffer_add_string(buffer, type);
    buffer_add_string(buffer, content);
    buffer_add(buffer, PROTOCOL_MESSAGE_SEP);
}

void controller_send_int(Session* session, char* type, int content) {
    char str[24];
    sprintf(str, "%d", content);

    controller_send(session, type, str);
}
