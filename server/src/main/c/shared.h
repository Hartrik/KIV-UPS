
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-29
 */

#include "game_pool.h"
#include "session.h"
#include "session_pool.h"

#ifndef SERVER_SHARED_H
#define SERVER_SHARED_H

#define SHARED_FILE "games.pzl"

GamePool game_pool;
SessionPool session_pool;
static pthread_mutex_t shared_lock;

void shared_init();
void shared_load();

Session *shared_create_session(int i);

void shared_store();
void shared_free();

#endif
