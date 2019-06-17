### 拷贝ffmpeg生成的so
编译ffmpeg生成so之后
- 将so文件拷贝到新建文件夹sharelibs/armeabi-v7a
- 将头文件拷贝到sr/main/cpp/include下
- 编写cmakelist.txt以及ffmpeg.cpp文件之后

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



基本上会遇到以上问题，解决掉之后，最终编译生成的so路径如下：

![so生成路径](https://github.com/fanflame/JniDemos/blob/master/ffmpegshare/pics/1.png?raw=true)


### 运行
将so拷贝到libs/armearbi-v7a中
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