
/**
 * Socket server, handles multiple clients using threads.
 *
 * @author: Patrik Harag
 * @version: 2017-10-03
 */

#include<stdbool.h>
#include<stdio.h>
#include<string.h>
#include<stdlib.h>
#include<sys/socket.h>
#include<arpa/inet.h>
#include<pthread.h>

#include "server.h"
#include "message_buffer.h"
#include "protocol.h"


void *connection_handler(void *);

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

        printf("Handler assigned\n");
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
void* connection_handler(void *socket_desc) {
    // Get the socket descriptor
    int socket = *(int *) socket_desc;
    char socket_buffer[SERVER_BUFFER_SIZE];
    MessageBuffer message_buffer;

    message_buffer.index = 0;
    message_buffer.content_length = 4;
    message_buffer.content = malloc(4);

    printf("  [%d] ", socket);
    printf("Listening...\n");

    bool exit = false;
    while (!exit) {
        ssize_t read_size = recv(socket, socket_buffer, SERVER_BUFFER_SIZE, 0);
        if (read_size > 0) {
            for (int i = 0; i < read_size; ++i) {
                char c = socket_buffer[i];
                if (c == PROTOCOL_MESSAGE_SEP) {
                    // end of message
                    if (message_buffer.index != 0) {
                        message_buffer_add(&message_buffer, NULL);

                        char* type = message_buffer_get_type(&message_buffer);
                        char* content = message_buffer_get_content(&message_buffer);

                        printf("  [%d] ", socket);
                        printf("Message: type='%s' content='%s'\n", type, content);
                        fflush(stdout);

                        if (strncmp(type, "BYE", 3) == 0) {
                            exit = true;
                            break;
                        }

                        // reset buffer
                        message_buffer.index = 0;
                    }
                } else {
                    if (message_buffer.index == PROTOCOL_TYPE_SIZE)
                        message_buffer_add(&message_buffer, NULL);

                    message_buffer_add(&message_buffer, socket_buffer[i]);
                }
            }

        }
    }

    // write(sock, message, strlen(message));

    printf("  [%d] ", socket);
    puts("Client disconnected");
    fflush(stdout);

    free(socket_desc);
    free(message_buffer.content);

    return 0;
}
