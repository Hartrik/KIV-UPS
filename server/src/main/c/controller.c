
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-10
 */

#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <stdbool.h>
#include "controller.h"
#include "protocol.h"
#include "game_pool.h"
#include "utils.h"

bool process_message(Session* session, char* type, char* content) {
    printf("  [%d] Message: type='%s' content='%s'\n",
           session->socket_fd, type, content);
    fflush(stdout);

    if (strncmp(type, "BYE", 3) == 0) {
        return true;

    } else if (strncmp(type, "NOP", 3) == 0) {
        // nothing

    } else if (strncmp(type, "LIN", 3) == 0) {
        char* name = content;

        if (session_is_logged(session)) {
            controller_send_int(session, "LIN", PROTOCOL_LIN_ERR_ALREADY_LOGGED);
        } else if (strlen(name) > SESSION_PLAYER_MAX_NAME_LENGTH) {
            controller_send_int(session, "LIN", PROTOCOL_LIN_ERR_NAME_TOO_LONG);
        } else if (!utils_is_valid_name(name)) {
            controller_send_int(session, "LIN", PROTOCOL_LIN_ERR_UNSUPPORTED_CHARS);
        } else {
            // TODO: uživatelé se stejnými jmény
            strcpy(session->name, name);
            controller_send_int(session, "LIN", PROTOCOL_LIN_OK);
            printf("  [%d] - User logged in: %s\n", session->socket_fd, session->name);
        }

    } else if (strncmp(type, "LOF", 3) == 0) {
        if (session_is_in_game(session)) {
            controller_send_int(session, "LOF", PROTOCOL_LOF_IN_GAME);
        } else {
            session->name[0] = 0;
            controller_send_int(session, "LOF", PROTOCOL_LOF_OK);
            printf("  [%d] - User logged out", session->socket_fd);
        }

    } else if (strncmp(type, "NEW", 3) == 0) {
        Game* game = gp_create_game(content);
        session->game = game;

        controller_send_int(session, "GAM", game == NULL ? -1 : game->id);

    } else {
        printf("  [%d] - Unknown command: %s\n", session->socket_fd, type);
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
