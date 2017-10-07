
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-07
 */

#include "server.h"
#include "game_pool.h"

#define PORT 8076

int main() {
    gp_init();

    int ret = server_start(PORT);

    gp_free();
    return ret;
}