#!/bin/bash

echo "=========================================="
echo "🐛 调试运行 iMusic"
echo "=========================================="

APP_PATH="./iMusic_py312.app/Contents/MacOS/iMusic"

echo "运行应用并捕获所有输出..."
echo ""

# 设置环境变量以显示更多调试信息
export PYTHONUNBUFFERED=1
export QT_DEBUG_PLUGINS=1

# 运行应用
"$APP_PATH" 2>&1 &
PID=$!

echo "应用已启动 (PID: $PID)"
echo "等待 5 秒..."

sleep 5

if ps -p $PID > /dev/null 2>&1; then
    echo ""
    echo "✅ 应用仍在运行!"
    echo "进程信息:"
    ps -p $PID -o pid,comm,%cpu,%mem,etime
    
    # 不要杀死进程，让用户手动测试
    echo ""
    echo "=========================================="
    echo "✅ 应用正在运行中"
    echo "=========================================="
    echo ""
    echo "请检查应用窗口并测试功能"
    echo "完成后请手动关闭应用或运行: kill $PID"
else
    echo ""
    echo "❌ 应用已退出"
    wait $PID
    EXIT_CODE=$?
    echo "退出代码: $EXIT_CODE"
fi

echo ""
