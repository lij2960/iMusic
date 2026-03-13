#!/bin/bash

# 使用py2app打包iMusic应用
# py2app是专门为macOS设计的Python打包工具

set -e

APP_NAME="iMusic"
VERSION="1.0.1"

echo "=========================================="
echo "使用py2app创建 ${APP_NAME}"
echo "版本: ${VERSION}"
echo "=========================================="

# 获取当前目录
CURRENT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${CURRENT_DIR}"

# 检查虚拟环境
if [ ! -d "venv" ]; then
    echo "❌ 错误：未找到虚拟环境"
    echo "请先运行: ./setup.sh"
    exit 1
fi

# 激活虚拟环境
echo ""
echo "🔧 激活虚拟环境..."
source venv/bin/activate

# 安装py2app
echo ""
echo "📦 检查py2app..."
if ! python3 -c "import py2app" 2>/dev/null; then
    echo "安装py2app..."
    pip3 install py2app
fi

# 清理之前的构建
echo ""
echo "🧹 清理之前的构建..."
rm -rf build dist "${APP_NAME}.app"

# 使用py2app构建
echo ""
echo "🔨 使用py2app构建应用..."
echo "这可能需要几分钟时间..."
python3 setup_py2app.py py2app

# 检查构建结果
if [ -d "dist/${APP_NAME}.app" ]; then
    echo ""
    echo "✅ 应用构建成功！"
    echo ""
    
    # 移动到当前目录
    mv "dist/${APP_NAME}.app" "./"
    
    # 显示应用信息
    APP_SIZE=$(du -sh "${APP_NAME}.app" | cut -f1)
    echo "📦 应用信息:"
    echo "  名称: ${APP_NAME}"
    echo "  版本: ${VERSION}"
    echo "  大小: ${APP_SIZE}"
    echo "  位置: ${CURRENT_DIR}/${APP_NAME}.app"
    echo ""
    
    # 清理构建文件
    echo "🧹 清理构建文件..."
    rm -rf build dist
    
    echo ""
    echo "=========================================="
    echo "测试应用:"
    echo "=========================================="
    echo ""
    echo "方式1: 直接运行"
    echo "  open \"${APP_NAME}.app\""
    echo ""
    echo "方式2: 从终端运行（查看输出）"
    echo "  ./${APP_NAME}.app/Contents/MacOS/${APP_NAME}"
    echo ""
    echo "方式3: 使用测试脚本"
    echo "  ./test_run.sh"
    echo ""
else
    echo ""
    echo "❌ 构建失败"
    echo ""
    echo "请检查错误信息"
    exit 1
fi
