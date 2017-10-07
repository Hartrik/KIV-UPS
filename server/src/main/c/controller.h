
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-07
 */

#ifndef SERVER_CONTROLLER_H
#define SERVER_CONTROLLER_H

#include "session.h"

bool process_message(Session* session, char* type, char* content);

void controller_send(Session* session, char* type, char* content);
void controller_send_int(Session* session, char* type, int content);

#endif
