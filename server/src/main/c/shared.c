
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-14
 */

#include <pthread.h>
#include "shared.h"
#include "session.h"

Game* shared_create_game(Session* session, GamePool* game_pool, unsigned int w, unsigned int h) {
    pthread_mutex_lock(&shared_lock);

    Game* game = gp_create_game(game_pool, w, h);

    pthread_mutex_unlock(&shared_lock);
    return game;
}
