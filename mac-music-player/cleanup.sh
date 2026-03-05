#!/bin/bash

# 清理无用文件脚本
# 版本: 1.0.0

echo "=========================================="
echo "清理无用文件"
echo "=========================================="

# 删除打包相关的无用文件
echo ""
echo "🗑️  删除无用的打包脚本..."
rm -f build_app.sh              # py2app打包（失败）
rm -f build_executable.sh       # PyInstaller单文件（失败）
rm -f build_pyinstaller.sh      # PyInstaller .app（失败）
rm -f build_simple.sh           # PyInstaller简单打包（失败）
rm -f setup_app.py              # py2app配置（无用）
rm -f debug_app.sh              # 调试脚本（无用）
rm -f MusicPlayer.spec          # PyInstaller配置（自动生成）

# 删除打包输出
echo ""
echo "🗑️  删除打包输出..."
rm -rf build/
rm -rf dist/
rm -f *.dmg

# 删除测试文件
echo ""
echo "🗑️  删除测试文件..."
rm -f test_icons.py
rm -f test_install.py
rm -f test_lyrics.py
rm -f test_playback.py
rm -f verify_icon.py

# 删除重复的文档
echo ""
echo "🗑️  删除重复的文档..."
rm -f BUGFIX_ICON_COVER.md
rm -f BUGFIX_LYRICS_DISPLAY.md
rm -f BUILD_GUIDE.md
rm -f FIXED.md
rm -f ICON_AND_COVER_GUIDE.md
rm -f ICON_UPDATE_COMPLETE.md
rm -f INSTALL.md
rm -f ONLINE_LYRICS_COMPLETE.md
rm -f PACKAGE.md
rm -f PACKAGING_RECOMMENDATION.md
rm -f PACKAGING_SUCCESS.md
rm -f QUICK_START_PACKAGING.txt
rm -f STATUS.md
rm -f SUCCESS.md
rm -f UPDATE_NOTES.md
rm -f 打包说明.md
rm -f 最终打包方案.md

# 删除Python缓存
echo ""
echo "🗑️  删除Python缓存..."
rm -rf __pycache__/
find . -type d -name "__pycache__" -exec rm -rf {} + 2>/dev/null || true
find . -type f -name "*.pyc" -delete 2>/dev/null || true

# 删除macOS系统文件
echo ""
echo "🗑️  删除macOS系统文件..."
find . -name ".DS_Store" -delete 2>/dev/null || true

echo ""
echo "✅ 清理完成！"
echo ""
echo "保留的文件:"
echo "  - 核心代码 (src/)"
echo "  - 主程序 (main.py)"
echo "  - 配置文件 (requirements.txt)"
echo "  - 启动脚本 (run.sh, setup.sh)"
echo "  - 创建脚本 (create_app.sh, create_shortcut.sh)"
echo "  - 核心文档 (README.md, QUICKSTART.md等)"
echo ""
echo "=========================================="
