### 拷贝ffmpeg生成的so
编译ffmpeg生成so之后
- 将so文件拷贝到新建文件夹libs/armeabi-v7a
- 将头文件拷贝到sr/main/cpp/include下
- 编写cmakelist.txt以及ffmpeg.cpp文件

cmakelists.txt如下：
```
cmake_minimum_required(VERSION 3.4.1)
add_library(ffmpegcaller # 生成的动态库名称
        SHARED # 库的类型
        ffmpegcaller.cpp# 库的源码相对本文件的路径
        )

add_library(
        ffmpeg # 引入的静态库库名称
        SHARED  # 设置库引入方式
        IMPORTED # 引入
)
set_target_properties(ffmpeg PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../libs/armeabi-v7a/libffmpeg.so # 设置静态库路径，必须是绝对路径
        )
# 与ffmpegshare_3.3.7不同的是使用了include_directories
include_directories(ffmpegcaller PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include) # 设置.h文件位置


# 链接库
target_link_libraries(
        ffmpegcaller
        ffmpeg
)
```

### 修改gradle文件
- android/defaultconfig 添加以下代码配置编译支持的cpu类型：
```
externalNativeBuild{
    cmake{
        abiFilters "armeabi-v7a"
    }
}
```

- 需要注释掉以下内容（如果有的话），否则会报错：More than one file was found with OS independent path 'lib/armeabi-v7a/libffmpeg.so'
```
//    sourceSets {
//        main{
//            jniLibs.srcDirs = ["libs"]
//        }
//    }
```
- android/defaultconfig 添加以下内容，否则报错：missing and no known rule to make it
```
externalNativeBuild{
    cmake{
        abiFilters "armeabi-v7a"
    }
}
```

### 编译

执行make module时jni可能会出现的问题
- 出现类似：fatal error: 'libavutil/avconfig.h' file not found

删除"libavutil/"

- error: undefined reference to 'av_version_info()'

将#include <avutil.h>放在extern "C" 里边

- error: unknown type name 'JNIEXPORT'

基本上会遇到以上问题，解决掉之后，最终编译生成的so路径如下：

<!--![so生成路径](https://github.com/fanflame/JniDemos/blob/master/ffmpegshare/pics/1.png?raw=true)-->


### 运行
注释掉
```
#externalNativeBuild {
#    cmake {
#        path file('src/main/cpp/CMakeLists.txt')
#    }
#}
```
添加
```
sourceSets {
    main{
        jniLibs.srcDirs = ["libs"]
    }
}
```
以及android/defaultConfig下添加
    
``` 
ndk{
    abiFilters "armeabi-v7a" //如果不添加这行：点击run生成的apk没有so文件，make module生成的apk有so文件
}
```

按照以上操作就可以看到运行结果了。