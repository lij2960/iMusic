#!/bin/bash

echo "=========================================="
echo "iMusic 应用诊断工具"
echo "=========================================="

APP_PATH="iMusic.app"

if [ ! -d "$APP_PATH" ]; then
    echo "❌ 应用不存在: $APP_PATH"
    echo "请先运行: ./create_app.sh"
    exit 1
fi

echo ""
echo "✅ 应用存在"
echo ""

# 检查应用结构
echo "📁 应用结构:"
ls -lh "$APP_PATH/Contents/MacOS/"
echo ""

# 检查可执行文件
echo "🔍 可执行文件信息:"
file "$APP_PATH/Contents/MacOS/iMusic"
echo ""

# 检查应用签名
echo "🔐 应用签名:"
codesign -dv "$APP_PATH" 2>&1 | head -10
echo ""

# 尝试运行并捕获错误
echo "🚀 尝试运行应用（5秒超时）..."
echo "如果应用窗口出现，说明运行成功"
echo ""

timeout 5 "$APP_PATH/Contents/MacOS/iMusic" 2>&1 &
APP_PID=$!

sleep 2

if ps -p $APP_PID > /dev/null 2>&1; then
    echo "✅ 应用正在运行 (PID: $APP_PID)"
    echo ""
    echo "如果看到应用窗口，说明打包成功！"
    echo "如果没有看到窗口，可能是以下原因:"
    echo "1. VLC库未找到"
    echo "2. PyQt5初始化失败"
    echo "3. 缺少依赖"
    kill $APP_PID 2>/dev/null
else
    echo "❌ 应用启动后立即退出"
    echo ""
    echo "查看崩溃日志:"
    echo "open ~/Library/Logs/DiagnosticReports/"
    echo ""
    echo "或运行以下命令查看详细错误:"
    echo "$APP_PATH/Contents/MacOS/iMusic"
fi

echo ""
echo "=========================================="
echo "建议"
echo "=========================================="
echo ""
echo "Python版本打包比较复杂，建议使用Swift版本:"
echo ""
echo "cd ../macos-music-player-swift"
echo "open MusicPlayer.xcodeproj"
echo "# 在Xcode中按 Cmd+R 运行"
echo ""
echo "或者构建Swift版本:"
echo "cd ../macos-music-player-swift"
echo "./build_release.sh"
echo ""
echo "Swift版本优势:"
echo "✅ 原生macOS应用"
echo "✅ 启动速度快"
echo "✅ 内存占用低"
echo "✅ 打包简单"
echo "✅ 无需Python环境"
echo ""
