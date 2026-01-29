#!/bin/bash

# iMusic Build Script
# 用于构建和安装 iMusic Android 应用

echo "🎵 iMusic Build Script"
echo "======================"

# 检查是否在项目根目录
if [ ! -f "settings.gradle.kts" ]; then
    echo "❌ 错误：请在项目根目录运行此脚本"
    exit 1
fi

# 清理项目
echo "🧹 清理项目..."
./gradlew clean

# 检查清理是否成功
if [ $? -ne 0 ]; then
    echo "❌ 清理失败"
    exit 1
fi

# 构建项目
echo "🔨 构建项目..."
./gradlew assembleDebug

# 检查构建是否成功
if [ $? -ne 0 ]; then
    echo "❌ 构建失败"
    exit 1
fi

echo "✅ 构建成功！"

# 检查是否有连接的设备
adb devices | grep -q "device$"
if [ $? -eq 0 ]; then
    echo "📱 发现Android设备，正在安装应用..."
    ./gradlew installDebug
    
    if [ $? -eq 0 ]; then
        echo "✅ 应用安装成功！"
        echo "🚀 可以在设备上启动 iMusic 应用了"
    else
        echo "❌ 应用安装失败"
        exit 1
    fi
else
    echo "⚠️  未发现连接的Android设备"
    echo "📦 APK文件位置: app/build/outputs/apk/debug/app-debug.apk"
fi

echo ""
echo "🎉 构建完成！"
echo ""
echo "📋 使用说明："
echo "1. 首次运行需要授予存储权限"
echo "2. 点击'导入'选择音乐目录或全盘扫描"
echo "3. 在音乐库中选择歌曲开始播放"
echo "4. 将.lrc歌词文件放在音乐文件同目录下即可显示歌词"
echo ""