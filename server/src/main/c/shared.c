
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-15
 */

#include <pthread.h>
#include "shared.h"
#include "session.h"

bool shared_can_create_game(Session* session) {
    if (!session_is_logged(session))
        return false;

    if (session_is_in_game(session))
        return false;

    // TODO: limit number of games...

    return true;
}

bool shared_can_join_game(Session* session) {
    if (!session_is_logged(session))
        return false;

    if (session_is_in_game(session))
        return false;

    return true;
}

Game* shared_join_game(Session* session, int game_id) {
    pthread_mutex_lock(&shared_lock);

    Game* game = gp_find_game(&game_pool, game_id);
    if (game != NULL) {
        session->game = game;
    }

    pthread_mutex_unlock(&shared_lock);

    return game;
}

Game* shared_create_game(Session* session, unsigned int w, unsigned int h) {
    pthread_mutex_lock(&shared_lock);

    Game* game = gp_create_game(&game_pool, w, h);

    pthread_mutex_unlock(&shared_lock);
    return game;
}
