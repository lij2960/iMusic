#!/bin/bash

# macOS 音乐播放器安装脚本

echo "======================================"
echo "  macOS 音乐播放器 - 安装脚本"
echo "======================================"
echo ""

# 检查Python版本
echo "检查Python版本..."
python3 --version

if [ $? -ne 0 ]; then
    echo "错误: 未找到Python3，请先安装Python 3.8或更高版本"
    exit 1
fi

# 检查VLC是否安装
echo ""
echo "检查VLC媒体播放器..."
if [ ! -d "/Applications/VLC.app" ]; then
    echo "警告: 未检测到VLC媒体播放器"
    echo "本程序需要VLC来播放音频"
    echo ""
    echo "请选择安装方式："
    echo "1. 使用Homebrew安装 (推荐)"
    echo "2. 手动下载安装"
    echo "3. 跳过（如果已安装在其他位置）"
    read -p "请输入选项 (1/2/3): " choice
    
    case $choice in
        1)
            echo "使用Homebrew安装VLC..."
            if command -v brew &> /dev/null; then
                brew install --cask vlc
            else
                echo "错误: 未找到Homebrew"
                echo "请先安装Homebrew: https://brew.sh"
                echo "或选择手动安装VLC: https://www.videolan.org/vlc/"
                exit 1
            fi
            ;;
        2)
            echo "请访问 https://www.videolan.org/vlc/ 下载并安装VLC"
            echo "安装完成后重新运行此脚本"
            exit 0
            ;;
        3)
            echo "跳过VLC检查..."
            ;;
        *)
            echo "无效选项"
            exit 1
            ;;
    esac
fi

# 创建虚拟环境
echo ""
echo "创建虚拟环境..."
python3 -m venv venv

if [ $? -ne 0 ]; then
    echo "错误: 创建虚拟环境失败"
    exit 1
fi

# 激活虚拟环境
echo "激活虚拟环境..."
source venv/bin/activate

# 升级pip
echo ""
echo "升级pip..."
pip install --upgrade pip

# 安装依赖
echo ""
echo "安装依赖包..."
pip install -r requirements.txt

if [ $? -ne 0 ]; then
    echo "错误: 安装依赖失败"
    exit 1
fi

echo ""
echo "======================================"
echo "  安装完成！"
echo "======================================"
echo ""
echo "运行以下命令启动程序："
echo "  source venv/bin/activate"
echo "  python main.py"
echo ""
echo "或者直接运行："
echo "  ./run.sh"
echo ""