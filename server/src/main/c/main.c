
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-14
 */

#include "server.h"
#include "game_pool.h"
#include "shared.h"

#define PORT 8076

int main() {
    gp_init(&game_pool);

    int ret = server_start(PORT);

    gp_free(&game_pool);
    return ret;
}
