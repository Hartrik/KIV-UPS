
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-15
 */

#ifndef SERVER_CONTROLLER_H
#define SERVER_CONTROLLER_H

#include "session.h"

void controller_update(Session *session, unsigned long long int i);

bool controller_process_message(Session *session, char *type, char *content);

void controller_send(Session* session, char* type, char* content);
void controller_send_int(Session* session, char* type, int content);

#endif
