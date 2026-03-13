#!/bin/bash

echo "=========================================="
echo "🧪 简单测试 Python 3.12 应用"
echo "=========================================="

APP_PATH="./iMusic_py312.app"

echo "1️⃣ 使用 open 命令启动应用..."
open "$APP_PATH"

echo ""
echo "⏳ 等待 3 秒..."
sleep 3

echo ""
echo "2️⃣ 检查应用是否在运行..."
if pgrep -f "iMusic" > /dev/null; then
    echo "✅ 应用正在运行!"
    echo ""
    echo "📊 进程信息:"
    ps aux | grep -i imusic | grep -v grep | head -5
    echo ""
    echo "=========================================="
    echo "✅ 打包成功! 应用可以正常运行!"
    echo "=========================================="
    echo ""
    echo "请检查:"
    echo "  1. 应用窗口是否显示"
    echo "  2. 能否扫描音乐文件"
    echo "  3. 能否播放音乐"
    echo "  4. 能否搜索歌词"
else
    echo "❌ 应用未在运行"
    echo ""
    echo "尝试直接运行查看错误:"
    "$APP_PATH/Contents/MacOS/iMusic" 2>&1 | head -30 &
    sleep 2
    pkill -f "iMusic"
fi

echo ""
echo "=========================================="
