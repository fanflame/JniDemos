### 编译环境
- ffmpeg version 使用4.1.3
- macos High Sierra 10.13.4
- ndk android-ndk-r17c(不要使用android studio sdk下的ndk,需要重新下载 https://developer.android.com/ndk/downloads/older_releases.html)

下载完毕之后配置ndk的全局环境：
~/.bash_profile中修改ndkpath
```
export NDK_HOME=/Users/用户名/Downloads/android-ndk-r17c #需要修改为自己ndk17的路径
export PATH=$PATH:$NDK_HOME/
```
之后source ～/.bash_profile使环境变量生效

### 下载FFmpeg源码
```
git clone https://git.ffmpeg.org/ffmpeg.git
git checkout -b n4.1.3 n4.1.3
```
需要切换到4.1.3版本,需要先编译.a文件然后再编译为一个ffmpeg.so文件。

### 编写脚本生成类库
在ffmpeg中创建一个build_android.sh的脚本，并赋予可执行的权限(sudo chmod 777 build_android.sh)，脚本内容如下：
```
#!/bin/bash

# ndk环境    
export NDK=/Users/用户名/Library/Android/sdk/android-ndk-r17c
export SYSROOT=$NDK/platforms/android-21/arch-arm
export TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64
CPU=armv7-a

ISYSROOT=$NDK/sysroot
ASM=$ISYSROOT/usr/include/arm-linux-androideabi

# 要保存动态库的目录，这里保存在源码根目录下的android/armv7-a
export PREFIX=$(pwd)/android/$CPU
ADDI_CFLAGS="-marm"

function build_android
{
    ./configure \
        --target-os=android \
        --prefix=$PREFIX \
        --enable-cross-compile \
        --enable-static \
        --disable-shared \
        --disable-doc \
        --disable-ffmpeg \
        --disable-ffplay \
        --disable-ffprobe \
        --disable-avdevice \
        --disable-doc \
        --disable-symver \
        --cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
        --arch=arm \
        --sysroot=$SYSROOT \
        --extra-cflags="-I$ASM -isysroot $ISYSROOT -D__ANDROID_API__=21 -U_FILE_OFFSET_BITS -Os -fPIC -DANDROID -Wno-deprecated -mfloat-abi=softfp -marm" \
        --extra-ldflags="$ADDI_LDFLAGS" \
        $ADDITIONAL_CONFIGURE_FLAG

    make clean

    make -j16
    make install

    # 打包
    $TOOLCHAIN/bin/arm-linux-androideabi-ld \
        -rpath-link=$SYSROOT/usr/lib \
        -L$SYSROOT/usr/lib \
        -L$PREFIX/lib \
        -soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
        $PREFIX/libffmpeg.so \
        libavcodec/libavcodec.a \
        libavfilter/libavfilter.a \
        libavformat/libavformat.a \
        libavutil/libavutil.a \
        libswresample/libswresample.a \
        libswscale/libswscale.a \
        -lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
        $TOOLCHAIN/lib/gcc/arm-linux-androideabi/4.9.x/libgcc.a
 
    # strip 精简文件
    $TOOLCHAIN/bin/arm-linux-androideabi-strip  $PREFIX/libffmpeg.so

}

build_android
```

其中：
**有两处是需要根据自己的情况修改的地方：TMPDIR，NDK**

### 编译FFmpeg
在ffmpeg目录中，执行终端命令：
```
./build_android.sh
```

即可编译，然后等待生成so文件即可，生成的so文件位于PREFIX定义的路径中