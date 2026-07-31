#!/bin/bash
# 构建全能工具箱APK脚本
# 使用方法: ./build_apk.sh [debug|release]

BUILD_TYPE=${1:-debug}
JAVA_HOME_SET="/root/.local/share/mise/installs/java/17.0.2"
ANDROID_HOME_SET="/opt/android-sdk"

export JAVA_HOME=$JAVA_HOME_SET
export ANDROID_HOME=$ANDROID_HOME_SET
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH

echo "============================================"
echo "全能工具箱 V1.0.9 构建脚本"
echo "============================================"
echo ""
echo "Java版本:"
java -version 2>&1
echo ""
echo "Android SDK: $ANDROID_HOME"
echo ""

cd "$(dirname "$0")" || exit 1

echo "开始构建 ${BUILD_TYPE} 版本..."
echo ""

case $BUILD_TYPE in
    debug)
        gradle assembleDebug --no-daemon
        APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
        ;;
    release)
        gradle assembleRelease --no-daemon
        APK_PATH="app/build/outputs/apk/release/app-release.apk"
        ;;
    *)
        echo "错误: 无效的构建类型。请使用 'debug' 或 'release'"
        exit 1
        ;;
esac

if [ $? -eq 0 ]; then
    echo ""
    echo "============================================"
    echo "构建成功!"
    echo "APK路径: $(pwd)/${APK_PATH}"
    echo "文件大小: $(du -h "${APK_PATH}" | cut -f1 2>/dev/null || echo 'unknown')"
    echo "============================================"
else
    echo ""
    echo "构建失败，请检查错误日志"
    exit 1
fi
