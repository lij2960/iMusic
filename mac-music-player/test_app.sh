#!/bin/bash
# 测试打包后的应用

APP="iMusic.app"
EXEC="${APP}/Contents/MacOS/iMusic"

echo "测试 ${APP}..."
echo ""

if [ ! -f "${EXEC}" ]; then
    echo "❌ 可执行文件不存在"
    exit 1
fi

echo "运行应用（10秒后自动停止）..."
echo "----------------------------------------"
timeout 10s "${EXEC}" 2>&1 &
PID=$!

sleep 2

if ps -p $PID > /dev/null; then
    echo ""
    echo "✅ 应用正在运行 (PID: $PID)"
    echo "检查是否有窗口显示..."
else
    echo ""
    echo "❌ 应用已退出"
fi

wait $PID 2>/dev/null
echo ""
echo "测试完成"
