
/**
 * Socket server, handles multiple clients using threads.
 *
 * @author: Patrik Harag
 * @version: 2017-10-07
 */

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <errno.h>
#include <unistd.h>

#include "server.h"
#include "utils.h"
#include "session.h"
#include "protocol.h"
#include "controller.h"


static void* connection_handler(void *);

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
    session_init(&session, socket_fd);

    // Set up timeout
    struct timeval timeout;
    timeout.tv_sec = SERVER_TIMEOUT / 1000;
    timeout.tv_usec = 0;
    setsockopt(socket_fd, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout));

    // Set up message buffer
    Buffer message_buffer;
    buffer_init(&message_buffer, SERVER_MSG_BUFFER_SIZE);

    unsigned long long last_client_activity = utils_current_millis();
    bool exit = false;

    printf("  [%d] Listening...\n", socket_fd);

    while (!exit) {
        unsigned long long cycle_start = utils_current_millis();
        bool sleep = true;

        // write
        if (session.to_send.index > 0) {
            // TODO: exceptions handling
            write(socket_fd, session.to_send.content, session.to_send.index);

            printf("  [%d] << %d B\n", socket_fd, (int) session.to_send.index);
            fflush(stdout);

            buffer_reset(&(session.to_send));
            sleep = false;
        }

        // read
        ssize_t recv_size = recv(socket_fd, socket_buffer, SERVER_SOCKET_BUFFER_SIZE, 0);
        if (recv_size > 0) {
            last_client_activity = utils_current_millis();

            for (int i = 0; i < recv_size; ++i) {
                char c = socket_buffer[i];
                if (c == PROTOCOL_MESSAGE_SEP) {
                    // end of message
                    if (message_buffer.index != 0) {
                        buffer_add(&message_buffer, 0);

                        char *type = message_buffer_get_type(&message_buffer);
                        char *content = message_buffer_get_content(&message_buffer);

                        exit |= process_message(&session, type, content);

                        // reset buffer
                        buffer_reset(&message_buffer);
                    }
                } else {
                    if (message_buffer.index == PROTOCOL_TYPE_SIZE) {
                        // divide message type and its content
                        buffer_add(&message_buffer, 0);
                    }

                    buffer_add(&message_buffer, socket_buffer[i]);
                }
            }
            sleep = false;

        } else if (recv_size == -1) {
            // end of stream
            if ((errno != EAGAIN) && (errno != EWOULDBLOCK)) {
                printf("  [%d] Error", socket_fd);
                perror("");
            }
            exit = true;
            sleep = false;

        } else {
            // no data...
        }

        // sleep
        unsigned long long cycle_end = utils_current_millis();
        unsigned long long cycle = cycle_end - cycle_start;
        if (sleep && cycle < SERVER_CYCLE) {
            utils_sleep(SERVER_CYCLE - cycle);
            printf("s");
        }

        // timeout
        unsigned long long diff = cycle_end - last_client_activity;
        if (diff > SERVER_TIMEOUT) {
            printf("  [%d] Timeout (after %llu ms)\n", socket_fd, diff);
            exit = true;
        }
    }

    printf("  [%d] Client disconnected\n", socket_fd);
    fflush(stdout);

    close(socket_fd);

    session_free(&session);
    buffer_free(&message_buffer);
    free(socket_desc);

    return 0;
}
