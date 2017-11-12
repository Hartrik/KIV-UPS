
/**
 * Protocol constants.
 *
 * @author: Patrik Harag
 * @version: 2017-10-29
 */

#ifndef SERVER_PROTOCOL_H
#define SERVER_PROTOCOL_H

#define PROTOCOL_MESSAGE_SEP 124  /* | */
#define PROTOCOL_TYPE_SIZE 3

#define PROTOCOL_LIN_OK 0
#define PROTOCOL_LIN_ALREADY_LOGGED 1
#define PROTOCOL_LIN_NAME_TOO_SHORT 2
#define PROTOCOL_LIN_NAME_TOO_LONG 3
#define PROTOCOL_LIN_UNSUPPORTED_CHARS 4
#define PROTOCOL_LIN_NAME_ALREADY_IN_USE 5

#define PROTOCOL_LOF_OK 0
#define PROTOCOL_LOF_IN_GAME 1

#define PROTOCOL_GNW_WRONG_FORMAT (-1)
#define PROTOCOL_GNW_WRONG_SIZE (-2)
#define PROTOCOL_GNW_NO_PERMISSIONS (-3)

#define PROTOCOL_GJO_OK 0
#define PROTOCOL_GJO_CANNOT_JOIN 1
#define PROTOCOL_GJO_NO_PERMISSIONS 2

#define PROTOCOL_GAC_OK 0
#define PROTOCOL_GAC_NO_PERMISSIONS 1
#define PROTOCOL_GAC_WRONG_FORMAT 2
#define PROTOCOL_GAC_WRONG_PIECE 3

#define PROTOCOL_GOF_OK 0

#endif
