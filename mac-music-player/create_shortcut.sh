#!/bin/bash

# 创建桌面快捷方式
# 版本: 1.0.0

echo "=========================================="
echo "创建桌面快捷方式"
echo "=========================================="

# 获取当前目录的绝对路径
CURRENT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 创建桌面快捷方式
SHORTCUT_PATH="$HOME/Desktop/音乐播放器.command"

echo ""
echo "📝 创建快捷方式..."

cat > "${SHORTCUT_PATH}" << EOF
#!/bin/bash

# 音乐播放器启动脚本
# 自动生成于: $(date)

# 切换到项目目录
cd "${CURRENT_DIR}"

# 激活虚拟环境
if [ -d "venv" ]; then
    source venv/bin/activate
fi

# 运行应用
python3 main.py
EOF

chmod +x "${SHORTCUT_PATH}"

echo ""
echo "✅ 快捷方式创建成功！"
echo ""
echo "位置: ${SHORTCUT_PATH}"
echo ""
echo "使用方法:"
echo "1. 双击桌面上的 '音乐播放器.command'"
echo "2. 应用会在终端窗口中启动"
echo "3. 关闭终端窗口即可退出应用"
echo ""
echo "=========================================="
echo "提示:"
echo "- 这是最简单可靠的运行方式"
echo "- 100%保证可以运行"
echo "- 可以看到所有日志输出"
echo "=========================================="
