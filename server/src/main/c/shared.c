
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-26
 */

#include <pthread.h>
#include <stdio.h>
#include "shared.h"
#include "utils.h"


void shared_init() {
    if (pthread_mutex_init(&shared_lock, NULL) != 0) {
        perror("Mutex init failed");
    }

    gp_init(&game_pool);
    sp_init(&session_pool);
}

Session *shared_create_session(int socket_fd) {
    pthread_mutex_lock(&shared_lock);

    Session* session = sp_create(&session_pool);
    session->socket_fd = socket_fd;
    session->last_activity = utils_current_millis();

    pthread_mutex_unlock(&shared_lock);

    return session;
}

void shared_free() {
    sp_free(&session_pool);
    gp_free(&game_pool);
    pthread_mutex_destroy(&shared_lock);
}
