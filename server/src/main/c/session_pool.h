
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-28
 */

#ifndef SERVER_SESSION_POOL_H
#define SERVER_SESSION_POOL_H

#include <stdlib.h>
#include "session.h"

#define SESSION_POOL_DEFAULT_CAPACITY 8
#define SESSION_POOL_INCREASE_RATIO 2

typedef struct _SessionPool {

    Session **sessions;
    size_t sessions_size;
    size_t sessions_capacity;

} SessionPool;

void sp_init(SessionPool* session_pool);
bool sp_is_free_name(SessionPool* session_pool, char* name);
Session* sp_create(SessionPool *session_pool);
void sp_free(SessionPool* session_pool);

#endif
