
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-14
 */

#ifndef SERVER_GAME_H
#define SERVER_GAME_H

#define GAME_MAX_SIZE 1000
#define GAME_MIN_SIZE 5
#define GAME_PIECE_SIZE 100
#define GAME_SHUFFLE_MULTIPIER 1.5

typedef struct _Piece {
    int id;
    int x, y;
} Piece;

typedef struct _Game {
    int id;
    unsigned int w, h;
    Piece** pieces;
} Game;

void game_init(Game* game, int id, unsigned int w, unsigned int h);
void game_free(Game* game);
void game_shuffle(Game* game, int max_w, int max_h);

#endif
