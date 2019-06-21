# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -D__STDC_CONSTANT_MACROS -L${CMAKE_SOURCE_DIR}/../../../../libs/${ANDROID_ABI}")
add_library(useopencv # 生成的动态库名称
        SHARED # 库的类型
        use_opencv.cpp# 库的源码相对本文件的路径
        )

add_library(
        opencv # 引入的静态库库名称
        SHARED  # 设置库作为动态库
        IMPORTED # 引入
        )
set_target_properties(opencv PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../libs/${CMAKE_ANDROID_ARCH_ABI}/libopencv_java3.so# 设置静态库路径，必须是绝对路径
        )

include_directories(include/) # 设置.h文件位置

find_library(log-lib log)
# 链接库
target_link_libraries(
        useopencv
        opencv
#        opencv_java3
        android
        ${log-lib})