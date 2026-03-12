#!/bin/bash
# 查找VLC库位置

echo "查找VLC库..."
echo ""

# 激活虚拟环境
source venv/bin/activate

# 查找python-vlc安装位置
PYTHON_VLC=$(python3 -c "import vlc; print(vlc.__file__)" 2>/dev/null)
echo "python-vlc位置: ${PYTHON_VLC}"
echo ""

# 查找系统VLC库
echo "查找系统VLC库:"
find /usr/local -name "libvlc*.dylib" 2>/dev/null || echo "  未在/usr/local找到"
find /Applications/VLC.app -name "libvlc*.dylib" 2>/dev/null || echo "  未在VLC.app找到"
echo ""

# 使用brew查找
if command -v brew &> /dev/null; then
    echo "Homebrew VLC信息:"
    brew list libvlc 2>/dev/null || echo "  未通过brew安装libvlc"
fi
