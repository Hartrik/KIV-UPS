
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-14
 */

#include <stdlib.h>
#include "game_pool.h"

static void ensure_capacity(GamePool* game_pool) {
    if (game_pool->games_capacity >= game_pool->games_size + 1)
        return;  // not needed

    if (game_pool->games == NULL) {
        game_pool->games_capacity = GAME_POOL_DEFAULT_CAPACITY;
        game_pool->games = (Game **) calloc(game_pool->games_capacity, sizeof(Game*));
    } else  {
        game_pool->games_capacity *= GAME_POOL_INCREASE_RATIO;
        game_pool->games = (Game **) realloc(
                game_pool->games, game_pool->games_capacity * sizeof(Game*));
    }
}

void gp_init(GamePool* game_pool) {
    gp_free(game_pool);
}

Game *gp_create_game(GamePool *game_pool, unsigned int w, unsigned int h) {
    Game* game = (Game *) calloc(1, sizeof(Game));
    game_init(game, ++game_pool->last_id, w, h);
    game_shuffle(game,
                 (int) (game->h * GAME_PIECE_SIZE * GAME_SHUFFLE_MULTIPIER / 2),
                 (int) (game->w * GAME_PIECE_SIZE * GAME_SHUFFLE_MULTIPIER / 2));

    game_pool->games_size++;
    ensure_capacity(game_pool);
    game_pool->games[game_pool->games_size - 1] = game;
    return game;
}

void gp_free(GamePool* game_pool) {
    if (game_pool->games_size > 0) {
        int i;

        for (i = 0; i < game_pool->games_size; i++) {
            Game *entry = game_pool->games[i];

            game_free(entry);
            free(entry);
        }

        free(game_pool->games);
    }

    game_pool->games = NULL;
    game_pool->games_size = 0;
    game_pool->games_capacity = 0;
    game_pool->last_id = -1;
}

