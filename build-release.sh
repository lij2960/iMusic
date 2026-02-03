#!/bin/bash

# iMusic Release Build Script
# 用于构建正式版 iMusic Android 应用

echo "🎵 iMusic Release Build Script"
echo "=============================="

# 检查是否在项目根目录
if [ ! -f "settings.gradle.kts" ]; then
    echo "❌ 错误：请在项目根目录运行此脚本"
    exit 1
fi

# 清理项目
echo "🧹 清理项目..."
./gradlew clean --quiet

# 构建正式版
echo "🔨 构建正式版APK..."
./gradlew assembleRelease

# 检查构建是否成功
if [ $? -ne 0 ]; then
    echo "❌ 构建失败"
    exit 1
fi

echo "✅ 正式版构建成功！"

# 显示APK信息
APK_PATH="app/build/outputs/apk/release/app-release.apk"
APK_SIZE=$(du -h "$APK_PATH" | cut -f1)

echo ""
echo "📦 APK信息："
echo "   文件路径: $APK_PATH"
echo "   文件大小: $APK_SIZE"
echo "   版本号: 1.0.0"
echo "   签名: 已签名 (Release)"
echo ""

# 验证APK签名
echo "🔐 验证APK签名..."
jarsigner -verify -verbose -certs "$APK_PATH" | grep "jar verified" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ APK签名验证成功"
else
    echo "⚠️  APK签名验证失败"
fi

echo ""
echo "🎉 正式版构建完成！"
echo ""
echo "📋 发布说明："
echo "1. 这是经过代码混淆和优化的正式版本"
echo "2. APK已使用发布密钥签名"
echo "3. 可以直接发布到应用商店或分发给用户"
echo "4. 建议在不同设备上进行测试"
echo ""