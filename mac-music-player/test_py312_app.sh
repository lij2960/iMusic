#!/bin/bash

echo "=========================================="
echo "🧪 测试 Python 3.12 打包的应用"
echo "=========================================="

APP_PATH="./iMusic_py312.app"

if [ ! -d "$APP_PATH" ]; then
    echo "❌ 应用不存在: $APP_PATH"
    exit 1
fi

echo ""
echo "📦 应用信息:"
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
echo "🚀 直接运行可执行文件 (5秒后自动终止)..."
echo "=========================================="

# 在后台运行应用，捕获输出
timeout 5s "$APP_PATH/Contents/MacOS/iMusic" 2>&1 &
PID=$!

# 等待一下
sleep 2

# 检查进程是否还在运行
if ps -p $PID > /dev/null 2>&1; then
    echo "✅ 应用正在运行 (PID: $PID)"
    echo ""
    echo "📊 进程信息:"
    ps -p $PID -o pid,comm,%cpu,%mem,etime
    
    # 终止进程
    kill $PID 2>/dev/null
    wait $PID 2>/dev/null
    
    echo ""
    echo "✅ 应用可以正常启动!"
else
    echo "❌ 应用启动后立即退出"
    echo ""
    echo "尝试查看错误信息..."
    "$APP_PATH/Contents/MacOS/iMusic" 2>&1 | head -20
fi

echo ""
echo "=========================================="
echo "🔍 使用 open 命令测试..."
echo "=========================================="

# 使用 open 命令打开应用
open "$APP_PATH"

sleep 3

# 检查应用是否在运行
if pgrep -f "iMusic_py312.app" > /dev/null; then
    echo "✅ 应用通过 open 命令成功启动!"
    echo ""
    echo "📊 运行中的进程:"
    ps aux | grep -i imusic | grep -v grep
    
    echo ""
    echo "⚠️  请手动检查应用窗口是否显示"
    echo "⚠️  如果窗口正常显示，说明打包成功!"
else
    echo "❌ 应用未能通过 open 命令启动"
fi

echo ""
echo "=========================================="
echo "测试完成"
echo "=========================================="
