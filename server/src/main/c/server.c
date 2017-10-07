
/**
 * Socket server, handles multiple clients using threads.
 *
 * @author: Patrik Harag
 * @version: 2017-10-07
 */

#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <errno.h>
#include <unistd.h>

#include "server.h"
#include "utils.h"
#include "session.h"
#include "message_buffer.h"
#include "protocol.h"


void* connection_handler(void *);
bool process_message(Session* session, char* type, char* content);

int server_start(int port) {
    int socket_desc, client_socket, c, *new_socket;
    struct sockaddr_in server, client;

    // Create socket
    socket_desc = socket(AF_INET, SOCK_STREAM, IPPROTO_IP /* TPC */);
    if (socket_desc == -1) {
        printf("Could not create socket\n");
    }
    printf("Socket created\n");

    server.sin_family = AF_INET;
    server.sin_addr.s_addr = INADDR_ANY;
    server.sin_port = htons(port);

    // Bind
    if (bind(socket_desc, (struct sockaddr *) &server, sizeof(server)) < 0) {
        perror("Bind failed");
        return 1;
    }
    printf("Bind done\n");

    // Listen
    if (listen(socket_desc, 3) < 0) {
        perror("Listen failed");
        return 1;
    }

    // Accept and incoming connection
    printf("Waiting for incoming connections...\n");

    c = sizeof(struct sockaddr_in);
    while ((client_socket = accept(socket_desc, (struct sockaddr *) &client, (socklen_t *) &c))) {
        printf("Connection accepted\n");

        pthread_t sniffer_thread;
        new_socket = malloc(1);
        *new_socket = client_socket;

        if (pthread_create(&sniffer_thread, NULL, connection_handler, (void *) new_socket) < 0) {
            perror("Could not create thread");
            return 1;
        }
    }

    if (client_socket < 0) {
        perror("Accept failed");
        return 1;
    }

    return 0;
}

/**
 * This will handle connection for each client.
 */
void* connection_handler(void* socket_desc) {
    // Set up socket
    int socket_fd = *(int *) socket_desc;
    char socket_buffer[SERVER_SOCKET_BUFFER_SIZE];

    // Set up session
    Session session;
    session.socket_fd = socket_fd;

    // Set up timeout
    struct timeval timeout;
    timeout.tv_sec = SERVER_TIMEOUT / 1000;
    timeout.tv_usec = 0;
    setsockopt(socket_fd, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout));

    // Set up message buffer
    MessageBuffer message_buffer;
    message_buffer.index = 0;
    message_buffer.content_length = SERVER_MSG_BUFFER_SIZE;
    message_buffer.content = malloc(SERVER_MSG_BUFFER_SIZE);

    unsigned long long last_client_activity = utils_current_millis();
    bool exit = false;

    printf("  [%d] Listening...\n", socket_fd);

    while (!exit) {
        ssize_t recv_size = recv(socket_fd, socket_buffer, SERVER_SOCKET_BUFFER_SIZE, 0);
        if (recv_size > 0) {
            last_client_activity = utils_current_millis();

            for (int i = 0; i < recv_size; ++i) {
                char c = socket_buffer[i];
                if (c == PROTOCOL_MESSAGE_SEP) {
                    // end of message
                    if (message_buffer.index != 0) {
                        message_buffer_add(&message_buffer, NULL);

                        char *type = message_buffer_get_type(&message_buffer);
                        char *content = message_buffer_get_content(&message_buffer);

                        exit |= process_message(&session, type, content);

                        // reset buffer
                        message_buffer.index = 0;
                    }
                } else {
                    if (message_buffer.index == PROTOCOL_TYPE_SIZE) {
                        // divide message type and its content
                        message_buffer_add(&message_buffer, NULL);
                    }

                    message_buffer_add(&message_buffer, socket_buffer[i]);
                }
            }
        } else if (recv_size == -1) {
            if ((errno != EAGAIN) && (errno != EWOULDBLOCK)) {
                printf("  [%d] Error", socket_fd);
                perror("");
            }
            exit = true;
        } else {
            // no data...
            utils_sleep(100);
        }

        unsigned long long diff = utils_current_millis() - last_client_activity;
        if (diff > SERVER_TIMEOUT) {
            printf("  [%d] Timeout (after %llu ms)\n", socket_fd, diff);
            exit = true;
        }
    }

    // write(sock, message, strlen(message));

    printf("  [%d] Client disconnected\n", socket_fd);
    fflush(stdout);

    free(socket_desc);
    free(message_buffer.content);

    close(socket_fd);
    return 0;
}

bool process_message(Session* session, char* type, char* content) {
    printf("  [%d] Message: type='%s' content='%s'\n",
           session->socket_fd, type, content);
    fflush(stdout);

    if (strncmp(type, "BYE", 3) == 0) {
        return true;
    }
    return false;
}