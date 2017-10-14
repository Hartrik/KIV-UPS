
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-14
 */

#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <stdbool.h>
#include "controller.h"
#include "protocol.h"
#include "utils.h"
#include "server.h"
#include "shared.h"

void controller_update(Session *session, unsigned long long time) {
    unsigned long long last_activity_diff = time - session->last_activity;

    if (last_activity_diff > (SERVER_TIMEOUT / 2)) {
        unsigned long long last_ping_diff = time - session->last_ping;
        if (last_ping_diff > (SERVER_TIMEOUT / 2)) {
            session->last_ping = time;
            controller_send(session, "PIN", "");
        }
    }
}

static bool parse_size(char* string, long* w, long* h) {
    if (string == NULL)
        return false;

    char* sep_str = strchr(string, ',');
    if (sep_str == NULL)
        return false;

    int sep_index = (int) (sep_str - string);
    if (sep_index >= strlen(string))
        return false;  // xxx,
    string[sep_index] = '\0';

    char *rest;
    *w = strtol(string, &rest, 10);

    if (strlen(rest) != 0)
        return false;

    *h = strtol(string + sep_index + 1, &rest, 10);

    if (strlen(rest) != 0)
        return false;

    return true;
}

bool controller_process_message(Session *session, char *type, char *content) {
    printf("  [%d] Message: type='%s' content='%s'\n",
           session->socket_fd, type, content);
    fflush(stdout);

    if (strncmp(type, "BYE", 3) == 0) {
        return true;

    } else if (strncmp(type, "PIN", 3) == 0) {
        // nothing

    } else if (strncmp(type, "LIN", 3) == 0) {
        char* name = content;

        if (session_is_logged(session)) {
            controller_send_int(session, "LIN", PROTOCOL_LIN_ERR_ALREADY_LOGGED);
        } else if (name == NULL || strlen(name) < SESSION_PLAYER_MIN_NAME_LENGTH) {
            controller_send_int(session, "LIN", PROTOCOL_LIN_ERR_NAME_TOO_SHORT);
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

    } else if (strncmp(type, "GNW", 3) == 0) {
        long w, h;

        if (parse_size(content, &w, &h)) {
            if (w >= GAME_MIN_SIZE && w <= GAME_MAX_SIZE
                    && h >= GAME_MIN_SIZE && h <= GAME_MAX_SIZE) {

                Game* game = shared_create_game(
                        session, &game_pool, (unsigned int) w, (unsigned int) h);

                session->game = game;
                controller_send_int(session, "GNW", game->id);
                printf("  [%d] - New game [id=%d, w=%ld, h=%ld]\n",
                       session->socket_fd, game->id, w, h);

            } else {
                controller_send_int(session, "GNW", PROTOCOL_GNW_WRONG_SIZE);
            }
        } else {
            controller_send_int(session, "GNW", PROTOCOL_GNW_WRONG_FORMAT);
        }

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
