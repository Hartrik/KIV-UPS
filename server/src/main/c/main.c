
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-16
 */

#include <stdio.h>
#include <pthread.h>
#include <signal.h>
#include <string.h>
#include "server.h"
#include "game_pool.h"
#include "shared.h"
#include "utils.h"
#include "stats.h"

#define PORT 8076


void sigint_handler(int sig_number) {
    if (TERMINATED) return;

    printf("\nTERMINATED\n");
    TERMINATED = true;

    utils_sleep(SERVER_TIMEOUT);  // wait for other threads to finish
}

void init_SIGINT_handler() {
    struct sigaction old_action;
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = &sigint_handler;
    sigaction(SIGINT, &sa, &old_action);
}

int main() {
    init_SIGINT_handler();

    stats_init();

    if (pthread_mutex_init(&shared_lock, NULL) != 0) {
        printf("Mutex init failed\n");
        return 1;
    }

    gp_init(&game_pool);

    int ret = server_start(PORT);

    gp_free(&game_pool);
    pthread_mutex_destroy(&shared_lock);

    stats_store();
    stats_free();

    return ret;
}
