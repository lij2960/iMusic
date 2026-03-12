#!/bin/bash

# 修复打包问题的快速脚本

echo "=========================================="
echo "修复打包问题"
echo "=========================================="

# 激活虚拟环境
if [ -d "venv" ]; then
    echo ""
    echo "🔧 激活虚拟环境..."
    source venv/bin/activate
else
    echo "❌ 错误：未找到虚拟环境"
    echo "请先运行: ./setup.sh"
    exit 1
fi

# 检查Python版本
PYTHON_VERSION=$(python3 --version | cut -d' ' -f2 | cut -d'.' -f1,2)
echo ""
echo "� Python版本: $PYTHON_VERSION"

# 安装Pillow
echo ""
echo "📦 安装Pillow（图标转换需要）..."
echo "尝试安装最新版本..."

# 先尝试安装最新版本
if pip3 install --upgrade Pillow 2>/dev/null; then
    echo "✅ Pillow安装成功"
else
    echo "⚠️  最新版本安装失败，尝试使用预编译版本..."
    # 如果失败，尝试使用预编译的wheel
    pip3 install --upgrade --only-binary :all: Pillow || {
        echo "⚠️  预编译版本也失败，尝试从源码编译..."
        # 安装编译依赖
        echo "安装编译依赖..."
        if command -v brew &> /dev/null; then
            brew install libjpeg libtiff little-cms2 openjpeg webp
        fi
        pip3 install --upgrade Pillow
    }
fi

# 验证安装
if python3 -c "import PIL" 2>/dev/null; then
    PIL_VERSION=$(python3 -c "import PIL; print(PIL.__version__)")
    echo "✅ Pillow已安装: $PIL_VERSION"
else
    echo "❌ Pillow安装失败"
    echo ""
    echo "请尝试手动安装:"
    echo "  brew install libjpeg libtiff little-cms2 openjpeg webp"
    echo "  pip3 install Pillow"
    exit 1
fi

# 安装PyInstaller
echo ""
echo "📦 安装/更新PyInstaller..."
pip3 install --upgrade pyinstaller

# 验证PyInstaller
if python3 -c "import PyInstaller" 2>/dev/null; then
    PYINSTALLER_VERSION=$(python3 -c "import PyInstaller; print(PyInstaller.__version__)")
    echo "✅ PyInstaller已安装: $PYINSTALLER_VERSION"
else
    echo "❌ PyInstaller安装失败"
    exit 1
fi

echo ""
echo "✅ 修复完成！"
echo ""
echo "现在可以运行:"
echo "  ./create_app.sh"
echo ""
