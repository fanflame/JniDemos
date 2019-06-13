# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

add_library(
        staticlib #库名称
        STATIC  # 设置库作为静态库
        src/statictest.c #提供相对与CMakeLists.txt相对路径native代码(不需要包含.h)
        )
# 链接库
target_link_libraries(
        staticlib)