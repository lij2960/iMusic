#!/bin/bash

# 启动iMusic应用的推荐方式

APP="iMusic.app"

echo "=========================================="
echo "启动 iMusic"
echo "=========================================="
echo ""

# 检查应用是否存在
if [ ! -d "${APP}" ]; then
    echo "❌ 错误：未找到 ${APP}"
    echo "请先运行: ./create_app.sh"
    exit 1
fi

# 检查是否已经在运行
if pgrep -f "iMusic.app/Contents/MacOS/iMusic" > /dev/null; then
    echo "⚠️  iMusic已经在运行"
    echo ""
    echo "进程信息:"
    pgrep -fl iMusic
    echo ""
    echo "如需重启，请先停止现有进程:"
    echo "  pkill -f iMusic"
    exit 0
fi

echo "🚀 启动应用..."
echo ""

# 使用open命令启动（这是macOS的标准方式）
open "${APP}"

# 等待应用启动
sleep 2

# 检查是否成功启动
if pgrep -f "iMusic.app/Contents/MacOS/iMusic" > /dev/null; then
    echo "✅ 应用已启动"
    echo ""
    echo "进程信息:"
    pgrep -fl iMusic
    echo ""
    echo "提示:"
    echo "- 如果看不到窗口，请按 Command+Tab 切换到iMusic"
    echo "- 或者按 F3 查看所有窗口"
    echo "- 首次运行可能需要选择音乐文件夹"
    echo ""
    echo "停止应用:"
    echo "  pkill -f iMusic"
else
    echo "❌ 应用启动失败"
    echo ""
    echo "请尝试从终端运行查看错误:"
    echo "  ./iMusic.app/Contents/MacOS/iMusic"
    echo ""
    echo "或者查看日志:"
    echo "  ./test_run.sh"
    echo "  cat app_run.log"
fi

echo ""
