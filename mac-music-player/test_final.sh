#!/bin/bash

echo "=========================================="
echo "🎉 iMusic Python 3.12 打包测试"
echo "=========================================="
echo ""

APP_PATH="./iMusic_py312.app"

if [ ! -d "$APP_PATH" ]; then
    echo "❌ 应用不存在: $APP_PATH"
    echo "请先运行: bash create_app_py312.sh"
    exit 1
fi

echo "📦 应用信息:"
echo "  名称: iMusic (Python 3.12)"
echo "  路径: $APP_PATH"
echo "  大小: $(du -sh "$APP_PATH" | cut -f1)"
echo ""

echo "🔍 检查应用结构..."
if [ -f "$APP_PATH/Contents/MacOS/iMusic" ]; then
    echo "  ✅ 可执行文件存在"
else
    echo "  ❌ 可执行文件不存在"
    exit 1
fi

if [ -f "$APP_PATH/Contents/Info.plist" ]; then
    echo "  ✅ Info.plist 存在"
else
    echo "  ❌ Info.plist 不存在"
fi

echo ""
echo "🚀 启动应用..."
echo "=========================================="
echo ""

# 使用 open 命令启动应用
open "$APP_PATH"

echo "⏳ 等待应用启动..."
sleep 3

# 检查应用是否在运行
if pgrep -f "iMusic" > /dev/null; then
    echo ""
    echo "✅ 应用成功启动!"
    echo ""
    echo "=========================================="
    echo "📋 测试清单"
    echo "=========================================="
    echo ""
    echo "请在应用中测试以下功能:"
    echo ""
    echo "  1. ✓ 窗口是否正常显示"
    echo "  2. ✓ 点击'扫描音乐'按钮，选择音乐文件夹"
    echo "  3. ✓ 音乐列表是否正常显示"
    echo "  4. ✓ 双击歌曲，是否能正常播放"
    echo "  5. ✓ 播放控制按钮是否正常（播放/暂停/上一首/下一首）"
    echo "  6. ✓ 进度条是否正常工作"
    echo "  7. ✓ 点击'搜索歌词'，是否能搜索并显示歌词"
    echo "  8. ✓ 歌词是否随播放进度滚动"
    echo "  9. ✓ 专辑封面是否正常显示"
    echo "  10. ✓ 应用图标是否正确显示"
    echo ""
    echo "=========================================="
    echo "📊 进程信息"
    echo "=========================================="
    ps aux | grep -i imusic | grep -v grep | head -3
    echo ""
    echo "=========================================="
    echo "📁 数据库位置"
    echo "=========================================="
    echo "  ~/Library/Application Support/iMusic/music_player.db"
    echo ""
    if [ -f ~/Library/Application\ Support/iMusic/music_player.db ]; then
        echo "  ✅ 数据库文件已创建"
        echo "  大小: $(du -sh ~/Library/Application\ Support/iMusic/music_player.db | cut -f1)"
    else
        echo "  ⚠️  数据库文件尚未创建（需要先扫描音乐）"
    fi
    echo ""
    echo "=========================================="
    echo "✅ 测试完成"
    echo "=========================================="
    echo ""
    echo "如果所有功能都正常，打包成功！"
    echo ""
    echo "下一步:"
    echo "  1. 创建 DMG 安装包: bash create_dmg.sh"
    echo "  2. 或直接使用: open iMusic_py312.app"
    echo ""
else
    echo "❌ 应用未能启动"
    echo ""
    echo "尝试从终端运行查看错误:"
    echo "  $APP_PATH/Contents/MacOS/iMusic"
    echo ""
fi
