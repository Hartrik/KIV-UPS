
/**
 * Socket server, handles multiple clients using threads.
 *
 * @author: Patrik Harag
 * @version: 2017-10-16
 */

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <errno.h>
#include <unistd.h>
#include <fcntl.h>

#include "server.h"
#include "utils.h"
#include "session.h"
#include "protocol.h"
#include "controller.h"


static void* connection_handler(void *);

int server_start(int port) {
    // Create socket
    int server_fd = socket(AF_INET, SOCK_STREAM, IPPROTO_IP /* TPC */);
    if (server_fd == -1) {
        printf("Could not create server socket\n");
    }
    printf("Server socket created\n");

    struct sockaddr_in server;
    server.sin_family = AF_INET;
    server.sin_addr.s_addr = INADDR_ANY;
    server.sin_port = htons(port);

    // Bind
    if (bind(server_fd, (struct sockaddr *) &server, sizeof(server)) < 0) {
        perror("Bind failed");
        return 1;
    }
    printf("Bind done\n");

    // Set accept to be non-blocking
    int flags = fcntl(server_fd, F_GETFL, 0);
    if (flags < 0) {
        perror("Could not get server socket flags");
        return 1;
    }

    int err = fcntl(server_fd, F_SETFL, flags | O_NONBLOCK);
    if (err < 0) {
        perror("Could set server socket to be non blocking");
        return 1;
    }

    // Listen
    if (listen(server_fd, SERVER_CONNECTION_QUEUE) < 0) {
        perror("Listen failed");
        return 1;
    }

    // Accept and incoming connection
    printf("Waiting for incoming connections...\n");

    do {
        struct sockaddr client;
        socklen_t client_len = sizeof(client);

        int client_fd = accept(server_fd, &client, &client_len);

        if (client_fd > 0) {
            printf("Connection accepted\n");

            pthread_t sniffer_thread;
            int *new_socket;
            new_socket = malloc(1);
            *new_socket = client_fd;

            if (pthread_create(&sniffer_thread, NULL, connection_handler, new_socket) < 0) {
                perror("Could not create thread");
                return 1;
            }
        } else {
            utils_sleep(100);
        }
    } while (!TERMINATED);

    close(server_fd);
    printf("Server socket closed\n");

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
    session.last_activity = utils_current_millis();

    // Set up timeout
    struct timeval timeout;
    timeout.tv_sec = 0;
    timeout.tv_usec = SERVER_CYCLE * 1000;
    setsockopt(socket_fd, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout));

    // Set up message buffer
    Buffer message_buffer;
    buffer_init(&message_buffer, SERVER_MSG_BUFFER_SIZE);

    bool exit = false;

    printf("  [%d] Listening...\n", socket_fd);

    while (!exit && !TERMINATED) {
        unsigned long long cycle_start = utils_current_millis();
        bool sleep = true;

        controller_update(&session, cycle_start);

        // write
        if (session.to_send.index > 0) {
            ssize_t written = write(socket_fd, session.to_send.content, session.to_send.index);
            if (written > 0) {

                printf("  [%d] << %d B\n", socket_fd, (int) session.to_send.index);
                fflush(stdout);

                if (session.to_send.index == written) {
                    buffer_reset(&session.to_send);
                } else {
                    printf("  [%d] not everything sent\n", socket_fd);
                    buffer_shift_left(&session.to_send, (int) written);
                }

            } else {
                // end of stream or timeout
                if ((errno != EAGAIN) && (errno != EWOULDBLOCK)) {
                    printf("  [%d] Error", socket_fd);
                    perror("");
                }
            }
            sleep = false;
        }

        // read
        ssize_t recv_size = recv(socket_fd, socket_buffer, SERVER_SOCKET_BUFFER_SIZE, 0);
        if (recv_size > 0) {
            session.last_activity = utils_current_millis();

            for (int i = 0; i < recv_size; ++i) {
                char c = socket_buffer[i];
                if (c == PROTOCOL_MESSAGE_SEP) {
                    // end of message
                    if (message_buffer.index != 0) {
                        buffer_add(&message_buffer, 0);

                        char *type = message_buffer_get_type(&message_buffer);
                        char *content = message_buffer_get_content(&message_buffer);

                        exit |= controller_process_message(&session, type, content);

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
            // end of stream or timeout
            if ((errno != EAGAIN) && (errno != EWOULDBLOCK)) {
                printf("  [%d] Error", socket_fd);
                perror("");
            }

        } else {
            // no data...
        }

        // sleep
        unsigned long long cycle_end = utils_current_millis();
        unsigned long long cycle = cycle_end - cycle_start;
        if (sleep && cycle < SERVER_CYCLE) {
            utils_sleep(SERVER_CYCLE - cycle);
        }

        // timeout
        unsigned long long diff = cycle_end - session.last_activity;
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
