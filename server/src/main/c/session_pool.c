
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-17
 */

#include "session_pool.h"

static void ensure_capacity(SessionPool* session_pool) {
    if (session_pool->sessions_capacity >= session_pool->sessions_size + 1)
        return;  // not needed

    if (session_pool->sessions == NULL) {
        session_pool->sessions_capacity = SESSION_POOL_DEFAULT_CAPACITY;
        session_pool->sessions = (Session **) calloc(session_pool->sessions_capacity, sizeof(Session*));
    } else  {
        session_pool->sessions_capacity *= SESSION_POOL_INCREASE_RATIO;
        session_pool->sessions = (Session **) realloc(
                session_pool->sessions, session_pool->sessions_capacity * sizeof(Session*));
    }
}

void sp_init(SessionPool* session_pool) {
    sp_free(session_pool);
}

Session* sp_create(SessionPool* session_pool) {
    Session* session = (Session *) calloc(1, sizeof(Session));
    session_init(session);

    session_pool->sessions_size++;
    ensure_capacity(session_pool);
    session_pool->sessions[session_pool->sessions_size - 1] = session;
    return session;
}

void sp_free(SessionPool* session_pool) {
    if (session_pool->sessions_size > 0) {
        int i;

        for (i = 0; i < session_pool->sessions_size; i++) {
            Session *entry = session_pool->sessions[i];

            session_free(entry);
            free(entry);
        }

        free(session_pool->sessions);
    }

    session_pool->sessions = NULL;
    session_pool->sessions_size = 0;
    session_pool->sessions_capacity = 0;
}
