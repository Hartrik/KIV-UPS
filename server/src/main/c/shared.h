
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-14
 */

#include "game_pool.h"
#include "session.h"

#ifndef SERVER_SHARED_H
#define SERVER_SHARED_H

static GamePool game_pool;
static pthread_mutex_t shared_lock;

bool shared_can_create_game(Session* session);
Game *shared_create_game(Session* session, GamePool* game_pool, unsigned int w, unsigned int h);

#endif
