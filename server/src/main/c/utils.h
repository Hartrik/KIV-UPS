
/**
 *
 * @author: Patrik Harag
 * @version: 2017-10-06
 */

#ifndef SERVER_UTILS_H
#define SERVER_UTILS_H

/**
 * Pause execution for a number of milliseconds.
 */
int utils_sleep(unsigned long ms);

/**
 * Returns current time in millis.
 */
unsigned long long utils_current_millis();

#endif
