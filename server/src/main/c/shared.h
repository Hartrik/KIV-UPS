
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-26
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

Session *shared_create_session(int i);

void shared_free();

#endif
