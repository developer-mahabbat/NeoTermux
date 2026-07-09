cmake_minimum_required(VERSION 3.22.1)
project("termlib")

add_library(termlib SHARED
    terminal.c
    pty.c
    jni_bridge.c
)

target_include_directories(termlib PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
target_link_libraries(termlib log android)
