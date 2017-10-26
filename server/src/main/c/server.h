
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-17
 */

#ifndef SERVER_SERVER_H
#define SERVER_SERVER_H

#include <stdbool.h>

#define SERVER_SOCKET_BUFFER_SIZE 4
#define SERVER_MSG_BUFFER_SIZE 4
#define SERVER_CONNECTION_QUEUE 10  /* how many pending connections will be held */
#define SERVER_TIMEOUT 2000  /* ms */
#define SERVER_CYCLE 20  /* ms */
#define SERVER_MAX_CORRUPTED_MESSAGES 50  /* per session */

bool TERMINATED;

int server_start(int port);

#endif //SERVER_SERVER_H
