# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
add_library(usestaticlib # 生成的动态库名称
        SHARED # 库的类型
        userstatic.cpp# 库的源码相对本文件的路径
        )

add_library(
        staticlib # 引入的静态库库名称
        STATIC  # 设置库作为静态库
        IMPORTED # 引入)
set_target_properties(staticlib PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../libs/${CMAKE_ANDROID_ARCH_ABI}/libstaticlib.a# 设置静态库路径，必须是绝对路径
        )

target_include_directories(usestaticlib PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include) # 设置.h文件位置

# 链接库
target_link_libraries(
        usestaticlib
        staticlib)