
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-07
 */

#ifndef SERVER_GAME_POOL_H
#define SERVER_GAME_POOL_H

#include "game.h"

#define GAME_POOL_DEFAULT_CAPACITY 8
#define GAME_POOL_INCREASE_RATIO 2

void gp_init();
Game *gp_create_game(char *string);
void gp_free();

#endif
