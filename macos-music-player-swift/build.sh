#!/bin/bash

# 构建 macOS 音乐播放器
# Build macOS Music Player

echo "🔨 开始构建 macOS 音乐播放器..."
echo "🔨 Building macOS Music Player..."

# 检查 Xcode 是否安装
if ! command -v xcodebuild &> /dev/null; then
    echo "❌ 错误: 未找到 xcodebuild，请安装 Xcode"
    echo "❌ Error: xcodebuild not found, please install Xcode"
    exit 1
fi

# 清理之前的构建
echo "🧹 清理之前的构建..."
rm -rf build/

# 构建项目
echo "🔨 构建项目..."
xcodebuild \
    -project MusicPlayer.xcodeproj \
    -scheme MusicPlayer \
    -configuration Release \
    -derivedDataPath build \
    clean build

if [ $? -eq 0 ]; then
    echo "✅ 构建成功！"
    echo "✅ Build successful!"
    echo ""
    echo "📦 应用程序位置:"
    echo "📦 Application location:"
    echo "   build/Build/Products/Release/MusicPlayer.app"
    echo ""
    echo "💡 运行应用:"
    echo "💡 Run application:"
    echo "   open build/Build/Products/Release/MusicPlayer.app"
else
    echo "❌ 构建失败"
    echo "❌ Build failed"
    exit 1
fi
