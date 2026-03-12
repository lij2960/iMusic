#!/bin/bash

# 诊断打包后的应用问题

APP_NAME="iMusic"
APP_PATH="${APP_NAME}.app"

echo "=========================================="
echo "诊断 ${APP_NAME} 应用"
echo "=========================================="
echo ""

# 检查应用是否存在
if [ ! -d "${APP_PATH}" ]; then
    echo "❌ 应用不存在: ${APP_PATH}"
    exit 1
fi

echo "✅ 应用存在: ${APP_PATH}"
echo ""

# 检查应用结构
echo "📁 应用结构:"
ls -la "${APP_PATH}/Contents/"
echo ""

# 检查可执行文件
EXEC_PATH="${APP_PATH}/Contents/MacOS/${APP_NAME}"
if [ -f "${EXEC_PATH}" ]; then
    echo "✅ 可执行文件存在: ${EXEC_PATH}"
    echo "   权限: $(ls -l "${EXEC_PATH}" | awk '{print $1}')"
    echo ""
else
    echo "❌ 可执行文件不存在: ${EXEC_PATH}"
    echo ""
    echo "可用的可执行文件:"
    ls -la "${APP_PATH}/Contents/MacOS/" 2>/dev/null || echo "  MacOS目录不存在"
    echo ""
fi

# 检查Info.plist
PLIST_PATH="${APP_PATH}/Contents/Info.plist"
if [ -f "${PLIST_PATH}" ]; then
    echo "✅ Info.plist存在"
    echo ""
    echo "应用信息:"
    /usr/libexec/PlistBuddy -c "Print CFBundleName" "${PLIST_PATH}" 2>/dev/null && echo "  名称: $(/usr/libexec/PlistBuddy -c "Print CFBundleName" "${PLIST_PATH}")"
    /usr/libexec/PlistBuddy -c "Print CFBundleVersion" "${PLIST_PATH}" 2>/dev/null && echo "  版本: $(/usr/libexec/PlistBuddy -c "Print CFBundleVersion" "${PLIST_PATH}")"
    /usr/libexec/PlistBuddy -c "Print CFBundleExecutable" "${PLIST_PATH}" 2>/dev/null && echo "  可执行文件: $(/usr/libexec/PlistBuddy -c "Print CFBundleExecutable" "${PLIST_PATH}")"
    echo ""
else
    echo "❌ Info.plist不存在"
    echo ""
fi

# 尝试直接运行可执行文件（控制台模式）
echo "=========================================="
echo "测试1: 直接运行可执行文件"
echo "=========================================="
echo ""

if [ -f "${EXEC_PATH}" ]; then
    echo "运行: ${EXEC_PATH}"
    echo "按 Ctrl+C 停止..."
    echo ""
    
    # 设置超时，避免卡住
    timeout 5s "${EXEC_PATH}" 2>&1 || {
        EXIT_CODE=$?
        if [ $EXIT_CODE -eq 124 ]; then
            echo ""
            echo "✅ 应用正在运行（超时后自动停止）"
        else
            echo ""
            echo "❌ 应用退出，退出码: $EXIT_CODE"
        fi
    }
else
    echo "⚠️  跳过：可执行文件不存在"
fi

echo ""
echo "=========================================="
echo "测试2: 使用open命令运行"
echo "=========================================="
echo ""

echo "运行: open -a \"${APP_PATH}\""
echo "等待3秒..."
echo ""

# 在后台运行，避免阻塞
open -a "${APP_PATH}" &
OPEN_PID=$!

sleep 3

# 检查应用是否在运行
if pgrep -f "${APP_NAME}" > /dev/null; then
    echo "✅ 应用正在运行"
    echo ""
    echo "进程信息:"
    ps aux | grep -i "${APP_NAME}" | grep -v grep
    echo ""
    echo "请检查应用窗口是否显示"
    echo ""
    echo "如需停止应用，运行: pkill -f ${APP_NAME}"
else
    echo "❌ 应用未运行或立即退出"
    echo ""
    echo "检查系统日志:"
    echo "----------------------------------------"
    log show --predicate 'process == "iMusic"' --last 1m 2>/dev/null | tail -20
fi

echo ""
echo "=========================================="
echo "诊断完成"
echo "=========================================="
echo ""
echo "如果应用无法运行，可能的原因："
echo "1. VLC库未正确打包"
echo "2. PyQt5依赖缺失"
echo "3. Python运行时问题"
echo "4. 权限问题"
echo ""
echo "建议："
echo "1. 查看控制台日志: Console.app"
echo "2. 尝试从终端运行: ${EXEC_PATH}"
echo "3. 检查Python版本兼容性"
echo "4. 使用console=True重新打包以查看错误"
echo ""
