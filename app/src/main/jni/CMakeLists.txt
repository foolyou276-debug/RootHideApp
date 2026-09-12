cmake_minimum_required(VERSION 3.22.1)
project("roothide")

add_library(
    roothide
    SHARED
    roothide.cpp
)

target_link_libraries(
    roothide
    android
    log
)
