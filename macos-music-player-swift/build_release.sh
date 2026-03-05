#!/bin/bash

# iMusic macOS 打包脚本
# 用于构建和打包 Release 版本

set -e

echo "🎵 开始构建 iMusic..."
echo ""

# 清理之前的构建
echo "🧹 清理之前的构建..."
rm -rf build/
rm -rf ~/Library/Developer/Xcode/DerivedData/MusicPlayer-*

# 构建 Release 版本
echo "🔨 构建 Release 版本..."
xcodebuild -project MusicPlayer.xcodeproj \
    -scheme MusicPlayer \
    -configuration Release \
    -derivedDataPath build \
    clean build

# 检查构建是否成功
if [ ! -d "build/Build/Products/Release/iMusic.app" ]; then
    echo "❌ 构建失败：找不到 iMusic.app"
    exit 1
fi

echo "✅ 构建成功！"
echo ""

# 显示应用信息
echo "📦 应用信息："
APP_PATH="build/Build/Products/Release/iMusic.app"
APP_NAME=$(defaults read "$APP_PATH/Contents/Info.plist" CFBundleDisplayName 2>/dev/null || echo "未知")
APP_VERSION=$(defaults read "$APP_PATH/Contents/Info.plist" CFBundleShortVersionString 2>/dev/null || echo "未知")
APP_BUILD=$(defaults read "$APP_PATH/Contents/Info.plist" CFBundleVersion 2>/dev/null || echo "未知")
APP_BUNDLE_ID=$(defaults read "$APP_PATH/Contents/Info.plist" CFBundleIdentifier 2>/dev/null || echo "未知")

echo "  名称: $APP_NAME"
echo "  版本: $APP_VERSION (Build $APP_BUILD)"
echo "  Bundle ID: $APP_BUNDLE_ID"
echo "  路径: $APP_PATH"
echo ""

# 显示应用大小
APP_SIZE=$(du -sh "$APP_PATH" | cut -f1)
echo "  大小: $APP_SIZE"
echo ""

# 验证应用图标
echo "🎨 验证应用图标..."
if [ -f "$APP_PATH/Contents/Resources/ic_launcher.png" ]; then
    echo "  ✅ 应用图标已包含 (ic_launcher.png)"
else
    echo "  ⚠️  警告：未找到 ic_launcher.png"
fi

if [ -d "$APP_PATH/Contents/Resources/AppIcon.icns" ] || [ -f "$APP_PATH/Contents/Resources/AppIcon.icns" ]; then
    echo "  ✅ 应用图标集已包含 (AppIcon.icns)"
else
    echo "  ⚠️  警告：未找到 AppIcon.icns"
fi
echo ""

# 创建 DMG（可选）
echo "📀 是否创建 DMG 安装包？(y/n)"
read -r CREATE_DMG

if [ "$CREATE_DMG" = "y" ] || [ "$CREATE_DMG" = "Y" ]; then
    echo "🔨 创建 DMG 安装包..."
    
    DMG_NAME="iMusic-${APP_VERSION}.dmg"
    DMG_PATH="build/$DMG_NAME"
    
    # 删除旧的 DMG
    rm -f "$DMG_PATH"
    
    # 创建临时文件夹
    TMP_DIR="build/dmg_tmp"
    rm -rf "$TMP_DIR"
    mkdir -p "$TMP_DIR"
    
    # 复制应用到临时文件夹
    cp -R "$APP_PATH" "$TMP_DIR/"
    
    # 创建 Applications 快捷方式
    ln -s /Applications "$TMP_DIR/Applications"
    
    # 创建 DMG
    hdiutil create -volname "iMusic" \
        -srcfolder "$TMP_DIR" \
        -ov -format UDZO \
        "$DMG_PATH"
    
    # 清理临时文件夹
    rm -rf "$TMP_DIR"
    
    if [ -f "$DMG_PATH" ]; then
        DMG_SIZE=$(du -sh "$DMG_PATH" | cut -f1)
        echo "✅ DMG 创建成功！"
        echo "  路径: $DMG_PATH"
        echo "  大小: $DMG_SIZE"
    else
        echo "❌ DMG 创建失败"
    fi
    echo ""
fi

# 创建 ZIP（可选）
echo "📦 是否创建 ZIP 压缩包？(y/n)"
read -r CREATE_ZIP

if [ "$CREATE_ZIP" = "y" ] || [ "$CREATE_ZIP" = "Y" ]; then
    echo "🔨 创建 ZIP 压缩包..."
    
    ZIP_NAME="iMusic-${APP_VERSION}.zip"
    ZIP_PATH="build/$ZIP_NAME"
    
    # 删除旧的 ZIP
    rm -f "$ZIP_PATH"
    
    # 创建 ZIP
    cd build/Build/Products/Release
    zip -r -y "../../../../$ZIP_NAME" iMusic.app
    cd ../../../../
    
    if [ -f "$ZIP_PATH" ]; then
        ZIP_SIZE=$(du -sh "$ZIP_PATH" | cut -f1)
        echo "✅ ZIP 创建成功！"
        echo "  路径: $ZIP_PATH"
        echo "  大小: $ZIP_SIZE"
    else
        echo "❌ ZIP 创建失败"
    fi
    echo ""
fi

# 显示最终结果
echo "🎉 构建完成！"
echo ""
echo "📂 输出文件："
ls -lh build/*.dmg build/*.zip 2>/dev/null || echo "  (无打包文件)"
echo ""
echo "🚀 运行应用："
echo "  open \"$APP_PATH\""
echo ""
echo "📋 安装说明："
echo "  1. 将 iMusic.app 拖到 Applications 文件夹"
echo "  2. 首次运行时，右键点击 > 打开"
echo "  3. 允许应用访问音乐文件夹"
echo ""
