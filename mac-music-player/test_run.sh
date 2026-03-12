#!/bin/bash

# 测试运行应用并捕获输出

APP="iMusic.app"
LOG_FILE="app_run.log"

echo "测试运行 ${APP}..."
echo ""

# 清空日志文件
> "${LOG_FILE}"

# 运行应用并捕获所有输出
echo "启动应用..."
"${APP}/Contents/MacOS/iMusic" > "${LOG_FILE}" 2>&1 &
APP_PID=$!

echo "应用PID: ${APP_PID}"
echo "等待5秒..."
sleep 5

# 检查进程是否还在运行
if ps -p ${APP_PID} > /dev/null 2>&1; then
    echo "✅ 应用正在运行"
    echo ""
    echo "进程信息:"
    ps -p ${APP_PID}
    echo ""
    echo "如需停止: kill ${APP_PID}"
else
    echo "❌ 应用已退出"
    echo ""
    echo "退出日志:"
    echo "----------------------------------------"
    cat "${LOG_FILE}"
    echo "----------------------------------------"
fi

echo ""
echo "完整日志已保存到: ${LOG_FILE}"
