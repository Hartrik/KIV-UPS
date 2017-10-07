
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-07
 */

#include <stdlib.h>
#include "game_pool.h"

static Game **games = NULL;
static size_t games_size = 0;
static size_t games_capacity = 0;
static int last_id = -1;

static void ensure_capacity() {
    if (games_capacity >= games_size + 1)
        return;  // not needed

    if (games == NULL) {
        games_capacity = GAME_POOL_DEFAULT_CAPACITY;
        games = (Game **) calloc(games_capacity, sizeof(Game*));
    } else  {
        games_capacity *= GAME_POOL_INCREASE_RATIO;
        games = (Game **) realloc(games, games_capacity * sizeof(Game*));
    }
}

void gp_init() {
    gp_free();
}

Game *gp_create_game(char* settings) {
    Game* game = (Game *) calloc(1, sizeof(Game));
    game->id = ++last_id;

    games_size++;
    ensure_capacity();
    games[games_size - 1] = game;
    return game;
}

void gp_free() {
    if (games_size > 0) {
        int i;

        for (i = 0; i < games_size; i++) {
            Game *entry = games[i];

            free(entry);
        }

        free(games);
    }

    games = NULL;
    games_size = 0;
    games_capacity = 0;
}

