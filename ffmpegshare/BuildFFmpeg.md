# ffmpeg编译android so库

### 环境配置
- ffmpeg version 使用3.3.7
- macos High Sierra 10.13.4
- ndk android-ndk-r14b(不要使用android studio sdk下的ndk,需要重新下载 https://developer.android.com/ndk/downloads/older_releases.html)

下载完毕之后配置ndk的全局环境：
~/.bash_profile中修改ndkpath
```
export NDK_HOME=/Users/momo/Downloads/android-ndk-r14b #需要修改为自己ndk14的路径
export PATH=$PATH:$NDK_HOME/
```

### 下载FFmpeg源码
git clone https://git.ffmpeg.org/ffmpeg.git

git checkout -b n3.3.7
需要切换到n3.3.7版本
### 修改configure文件
下载FFmpeg源代码之后，首先需要对源代码中的configure文件进行修改。由于编译出来的动态库文件名的版本号在.so之后（例如“libavcodec.so.5.100.1”），而android平台不能识别这样文件名，所以需要修改这种文件名。在configure文件中找到下面几行代码：
```
SLIBNAME_WITH_MAJOR='$(SLIBNAME).$(LIBMAJOR)'
LIB_INSTALL_EXTRA_CMD='$$(RANLIB)"$(LIBDIR)/$(LIBNAME)"'
SLIB_INSTALL_NAME='$(SLIBNAME_WITH_VERSION)'
SLIB_INSTALL_LINKS='$(SLIBNAME_WITH_MAJOR)$(SLIBNAME)'
```
复制代码替换成
```
SLIBNAME_WITH_MAJOR='$(SLIBPREF)$(FULLNAME)-$(LIBMAJOR)$(SLIBSUF)'
LIB_INSTALL_EXTRA_CMD='$$(RANLIB) "$(LIBDIR)/$(LIBNAME)"'
SLIB_INSTALL_NAME='$(SLIBNAME_WITH_MAJOR)'
SLIB_INSTALL_LINKS='$(SLIBNAME)'
```
### 编写脚本生成类库
在ffmpeg中创建一个build_android.sh的脚本，并赋予可执行的权限(sudo chmod 777 build_android.sh)，脚本内容如下：
```
#!/bin/bash

make clean
# NDK的路径，根据自己的安装位置进行设置
export TMPDIR=/Users/momo/Documents/ffmpeg/ffmpeg_make_temp
export NDK=/Users/momo/Downloads/android-ndk-r14b
export SYSROOT=$NDK/platforms/android-21/arch-arm/
export TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64
#export CPU=armeabi-v7a
export PREFIX=$(pwd)/android/$CPU
export ADDI_CFLAGS="-marm"
function build_one
{
./configure \
    --prefix=$PREFIX \
    --target-os=linux \
    --cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
    --arch=arm \
    --sysroot=$SYSROOT \
    --extra-cflags="-Os -fpic $ADDI_CFLAGS" \
    --extra-ldflags="$ADDI_LDFLAGS" \
    --cc=$TOOLCHAIN/bin/arm-linux-androideabi-gcc \
    --nm=$TOOLCHAIN/bin/arm-linux-androideabi-nm \
    --enable-shared \
    --enable-runtime-cpudetect \
    --enable-gpl \
    --enable-small \
    --enable-cross-compile \
    --disable-debug \
    --disable-static \
    --disable-doc \
    --disable-asm \
    --disable-ffmpeg \
    --disable-ffplay \
    --disable-ffprobe \
    --disable-ffserver \
    --enable-postproc \
    --enable-avdevice \
    --disable-symver \
    --disable-stripping \
$ADDITIONAL_CONFIGURE_FLAG
sed -i '' 's/HAVE_LRINT 0/HAVE_LRINT 1/g' config.h
sed -i '' 's/HAVE_LRINTF 0/HAVE_LRINTF 1/g' config.h
sed -i '' 's/HAVE_ROUND 0/HAVE_ROUND 1/g' config.h
sed -i '' 's/HAVE_ROUNDF 0/HAVE_ROUNDF 1/g' config.h
sed -i '' 's/HAVE_TRUNC 0/HAVE_TRUNC 1/g' config.h
sed -i '' 's/HAVE_TRUNCF 0/HAVE_TRUNCF 1/g' config.h
sed -i '' 's/HAVE_CBRT 0/HAVE_CBRT 1/g' config.h
sed -i '' 's/HAVE_RINT 0/HAVE_RINT 1/g' config.h
make clean
# 这里是定义用几个CPU编译，我用4个，一般在5分钟之内编译完成
make -j4
make install
}
# arm v7vfp
CPU=armv7a
OPTIMIZE_CFLAGS="-mfloat-abi=softfp -mfpu=vfp -marm -march=$CPU "
ADDITIONAL_CONFIGURE_FLAG=
build_one
# CPU=armv
# ADDI_CFLAGS="-marm"
# build_one
#arm v6
#CPU=armv6
#OPTIMIZE_CFLAGS="-marm -march=$CPU"
#ADDITIONAL_CONFIGURE_FLAG=
#build_one
#arm v7vfpv3
# CPU=armv7-a
# OPTIMIZE_CFLAGS="-mfloat-abi=softfp -mfpu=vfpv3-d16 -marm -march=$CPU "
# ADDITIONAL_CONFIGURE_FLAG=
# build_one
#arm v7n
#CPU=armv7-a
#OPTIMIZE_CFLAGS="-mfloat-abi=softfp -mfpu=neon -marm -march=$CPU -mtune=cortex-a8"
#ADDITIONAL_CONFIGURE_FLAG=--enable-neon
#build_one
#arm v6+vfp
#CPU=armv6
#OPTIMIZE_CFLAGS="-DCMP_HAVE_VFP -mfloat-abi=softfp -mfpu=vfp -marm -march=$CPU"
#ADDITIONAL_CONFIGURE_FLAG=
#build_one\
```

其中：

- TMPDIR为编译生成的临时文件存放的目录
- SYSROOT为so文件支持的最低Android版本的平台目录
- CPU为指定so文件支持的平台
- PREFIX为生成的so文件存放目录
- TOOLCHAIN为编译所使用的工具链目录
- cross-prefix为编译所使用的工具链文件
- enable和disable指定了需要编译的项
- target-os为目标操作系统；

**有两处是需要根据自己的情况修改的地方：TMPDIR，NDK**
### 编译FFmpeg
在ffmpeg目录中，执行终端命令：
```
./build_android.sh
```

即可编译，然后等待生成so文件即可，生成的so文件位于PREFIX定义的路径中

