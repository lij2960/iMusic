#!/bin/bash

# 使用Python 3.12设置环境
# Python 3.12比3.14更稳定，兼容性更好

set -e

# 切换到脚本所在目录
cd "$(dirname "${BASH_SOURCE[0]}")"

echo "=========================================="
echo "设置Python 3.12环境"
echo "=========================================="
echo ""

# 检查是否已安装Python 3.12
if command -v python3.12 &> /dev/null; then
    echo "✅ Python 3.12已安装"
    python3.12 --version
else
    echo "📦 安装Python 3.12..."
    if command -v brew &> /dev/null; then
        brew install python@3.12
    else
        echo "❌ 错误：未找到Homebrew"
        echo "请先安装Homebrew: https://brew.sh"
        exit 1
    fi
fi

echo ""
echo "🔧 创建Python 3.12虚拟环境..."
rm -rf venv312
python3.12 -m venv venv312

echo ""
echo "📦 安装依赖..."
source venv312/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
pip install pyinstaller

echo ""
echo "✅ 设置完成！"
echo ""
echo "=========================================="
echo "下一步："
echo "=========================================="
echo ""
echo "1. 激活虚拟环境:"
echo "   source venv312/bin/activate"
echo ""
echo "2. 运行应用（开发模式）:"
echo "   python3 main.py"
echo ""
echo "3. 打包应用:"
echo "   ./create_app_fixed.sh"
echo ""
