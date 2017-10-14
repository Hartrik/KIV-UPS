
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-14
 */

#ifndef SERVER_GAME_POOL_H
#define SERVER_GAME_POOL_H

#include <stdlib.h>
#include "game.h"

#define GAME_POOL_DEFAULT_CAPACITY 8
#define GAME_POOL_INCREASE_RATIO 2

typedef struct _GamePool {

    Game **games;
    size_t games_size;
    size_t games_capacity;
    int last_id;

} GamePool;

void gp_init(GamePool* game_pool);
Game *gp_create_game(GamePool *game_pool, unsigned int w, unsigned int h);
void gp_free(GamePool* game_pool);

#endif
