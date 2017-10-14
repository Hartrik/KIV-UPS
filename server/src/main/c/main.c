
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-14
 */

#include <stdio.h>
#include <pthread.h>
#include "server.h"
#include "game_pool.h"
#include "shared.h"

#define PORT 8076

int main() {
    if (pthread_mutex_init(&shared_lock, NULL) != 0) {
        printf("Mutex init failed\n");
        return 1;
    }

    gp_init(&game_pool);

    int ret = server_start(PORT);

    gp_free(&game_pool);
    pthread_mutex_destroy(&shared_lock);

    return ret;
}
