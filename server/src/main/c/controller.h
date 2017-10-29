
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-29
 */

#ifndef SERVER_CONTROLLER_H
#define SERVER_CONTROLLER_H

#include "session.h"

void controller_update(Session *session, unsigned long long int i);

void controller_process_message(Session *session, char *type, char *content);

void controller_send(Session* session, char* type, char* content);
void controller_send_int(Session* session, char* type, int content);

void controller_broadcast_game_action(Session* session, Game* game, Piece* p);

#endif
