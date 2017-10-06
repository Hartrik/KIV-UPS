
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-06
 */

#include <time.h>
#include <stdio.h>
#include <errno.h>
#include <sys/time.h>
#include "utils.h"

int utils_sleep(unsigned long ms) {
    int result = 0;

    struct timespec ts_remaining;
    ts_remaining.tv_sec = ms / 1000;
    ts_remaining.tv_nsec = (ms % 1000) * 1000000L;

    do {
        struct timespec ts_sleep = ts_remaining;
        result = nanosleep(&ts_sleep, &ts_remaining);
    } while (EINTR == result);

    if (result) {
        perror("nanosleep() failed");
        result = -1;
    }

    return result;
}

unsigned long long utils_current_millis() {
    struct timeval time;

    gettimeofday(&time, NULL);

    return (unsigned long) (time.tv_sec) * 1000
           + (unsigned long) (time.tv_usec) / 1000;
}
