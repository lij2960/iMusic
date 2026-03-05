#!/bin/bash

echo "🔧 设置 Xcode 开发者目录..."
echo "🔧 Setting up Xcode developer directory..."
echo ""
echo "需要管理员密码 / Administrator password required"
echo ""

sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 设置成功！"
    echo "✅ Setup successful!"
    echo ""
    echo "当前开发者目录 / Current developer directory:"
    xcode-select -p
    echo ""
    echo "现在可以运行构建脚本了："
    echo "Now you can run the build script:"
    echo "  ./build.sh"
    echo ""
    echo "或者直接在 Xcode 中打开项目："
    echo "Or open the project in Xcode:"
    echo "  open MusicPlayer.xcodeproj"
else
    echo ""
    echo "❌ 设置失败"
    echo "❌ Setup failed"
    echo ""
    echo "请手动运行："
    echo "Please run manually:"
    echo "  sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer"
fi
