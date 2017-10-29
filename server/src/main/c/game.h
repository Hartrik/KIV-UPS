
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-29
 */

#ifndef SERVER_GAME_H
#define SERVER_GAME_H

#include <stdbool.h>

#define GAME_MAX_SIZE 100
#define GAME_MIN_SIZE 2
#define GAME_PIECE_SIZE 100
#define GAME_SHUFFLE_MULTIPLIER 1.5
#define GAME_WH_RATIO 1.5
#define GAME_TOLERANCE 5

typedef struct _Piece {
    int id;
    int x, y;
} Piece;

typedef struct _Game {
    int id;
    unsigned int w, h;
    Piece** pieces;
    bool finished;
} Game;

void game_init(Game* game, int id, unsigned int w, unsigned int h);
void game_shuffle(Game* game, int max_w, int max_h);
void game_check_is_finished(Game* game);
void game_free(Game* game);

#endif
