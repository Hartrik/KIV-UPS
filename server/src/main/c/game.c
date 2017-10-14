
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-14
 */

#include <stdlib.h>
#include <time.h>
#include "game.h"

void game_init(Game* game, int id, unsigned int w, unsigned int h) {
    game->id = id;
    game->w = w;
    game->h = h;

    game->pieces = (Piece**) calloc(w * h, sizeof(Piece*));
    for (int i = 0; i < w * h; ++i) {
        Piece* p = (Piece *) calloc(1, sizeof(Piece));
        p->id = i;
        p->x = 0;
        p->y = 0;
        game->pieces[i] = p;
    }
}

void game_free(Game* game) {
    for (int i = 0; i < game->w * game->h; ++i) {
        free(game->pieces[i]);
    }
}

void game_shuffle(Game* game, int max_w, int max_h) {
    unsigned int last = (unsigned int) time(NULL);

    for (int i = 0; i < game->w * game->h; ++i) {
        Piece* p = game->pieces[i];

        srand(last);
        last = (unsigned int) (rand() % (max_w * 2));
        p->x = last - max_w;

        srand(last);
        last = (unsigned int) (rand() % (max_h * 2));
        p->y = last - max_h;
    }
}
