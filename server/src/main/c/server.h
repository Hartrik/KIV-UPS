
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-14
 */

#ifndef SERVER_SERVER_H
#define SERVER_SERVER_H

#define SERVER_SOCKET_BUFFER_SIZE 4
#define SERVER_MSG_BUFFER_SIZE 4
#define SERVER_TIMEOUT 2000  /* ms */
#define SERVER_CYCLE 100  /* ms */

int server_start(int port);

#endif //SERVER_SERVER_H
