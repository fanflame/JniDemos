# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
add_library(ffmpegcaller # 生成的动态库名称
        SHARED # 库的类型
        ffmpegcaller.cpp# 库的源码相对本文件的路径
        )

#add_library(
#        avcodec # 引入的静态库库名称
#        SHARED  # 设置库引入方式
#        IMPORTED # 引入
#)
#set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_CURRENT_SOURCE_DIR}/../../../sharelibs/armeabi-v7a/libavcodec-57.so # 设置静态库路径，必须是绝对路径
#        )
#add_library(
#        avdevice # 引入的静态库库名称
#        SHARED  # 设置库引入方式
#        IMPORTED # 引入
#)
#set_target_properties(avdevice PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_CURRENT_SOURCE_DIR}/../../../sharelibs/armeabi-v7a/libavdevice-57.so # 设置静态库路径，必须是绝对路径
#        )
#add_library(
#        avfilter # 引入的静态库库名称
#        SHARED  # 设置库引入方式
#        IMPORTED # 引入
#)
#set_target_properties(avfilter PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_CURRENT_SOURCE_DIR}/../../../sharelibs/armeabi-v7a/libavfilter-6.so # 设置静态库路径，必须是绝对路径
#        )
#add_library(
#        avformat # 引入的静态库库名称
#        SHARED  # 设置库引入方式
#        IMPORTED # 引入
#)
#set_target_properties(avformat PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_CURRENT_SOURCE_DIR}/../../../sharelibs/armeabi-v7a/libavformat-57.so # 设置静态库路径，必须是绝对路径
#        )
add_library(
        avutil # 引入的静态库库名称
        SHARED  # 设置库引入方式
        IMPORTED # 引入
)
set_target_properties(avutil PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../sharelibs/armeabi-v7a/libavutil-55.so # 设置静态库路径，必须是绝对路径
        )
#add_library(
#        postproc # 引入的静态库库名称
#        SHARED  # 设置库引入方式
#        IMPORTED # 引入
#)
#set_target_properties(postproc PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_CURRENT_SOURCE_DIR}/../../../sharelibs/armeabi-v7a/libpostproc-54.so # 设置静态库路径，必须是绝对路径
#        )
#add_library(
#        swresample # 引入的静态库库名称
#        SHARED  # 设置库引入方式
#        IMPORTED # 引入
#)
#set_target_properties(swresample PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_CURRENT_SOURCE_DIR}/../../../sharelibs/armeabi-v7a/libswresample-2.so # 设置静态库路径，必须是绝对路径
#        )
#
#add_library(
#        swscale # 引入的静态库库名称
#        SHARED  # 设置库引入方式
#        IMPORTED # 引入
#)
#set_target_properties(swscale PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_CURRENT_SOURCE_DIR}/../../../sharelibs/armeabi-v7a/libswscale-4.so # 设置静态库路径，必须是绝对路径
#        )

target_include_directories(ffmpegcaller PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include/libavutil) # 设置.h文件位置

# 链接库
target_link_libraries(
        ffmpegcaller
#        avcodec
#        avdevice
#        avfilter
#        avformat
        avutil
#        postproc
#        swresample
#        swscale
)