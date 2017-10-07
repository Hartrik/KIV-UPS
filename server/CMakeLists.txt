cmake_minimum_required(VERSION 3.8)
project(server)

set(CMAKE_C_STANDARD 99)

# sources
set(SOURCE_FILES src/main/c/main.c src/main/c/server.c src/main/c/server.h src/main/c/buffer.c src/main/c/buffer.h src/main/c/protocol.h src/main/c/utils.c src/main/c/utils.h src/main/c/session.c src/main/c/session.h src/main/c/game_pool.c src/main/c/game_pool.h src/main/c/game.c src/main/c/game.h src/main/c/controller.c src/main/c/controller.h)
add_executable(server-dist ${SOURCE_FILES})

# allow pthreads
set(THREADS_PREFER_PTHREAD_FLAG ON)
find_package(Threads REQUIRED)
target_link_libraries(server-dist Threads::Threads)