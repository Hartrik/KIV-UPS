
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-17
 */

#include "game_pool.h"
#include "session.h"
#include "session_pool.h"

#ifndef SERVER_SHARED_H
#define SERVER_SHARED_H

GamePool game_pool;
SessionPool session_pool;
static pthread_mutex_t shared_lock;

void shared_init();

Session* shared_create_session();

bool shared_can_create_game(Session* session);
bool shared_can_join_game(Session* session);
Game* shared_join_game(Session* session, int game_id);
Game* shared_create_game(Session* session, unsigned int w, unsigned int h);

void shared_free();

#endif
