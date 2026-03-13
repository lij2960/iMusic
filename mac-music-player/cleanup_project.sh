#!/bin/bash

# 项目清理脚本 - 删除无用的文件和文档

echo "=========================================="
echo "🧹 清理 iMusic 项目"
echo "=========================================="
echo ""

# 切换到脚本所在目录
cd "$(dirname "${BASH_SOURCE[0]}")"

echo "📋 清理计划："
echo ""
echo "将删除："
echo "  - 旧的调试文档（保留最终文档）"
echo "  - 失败的打包尝试的脚本"
echo "  - 测试应用（保留 iMusic_py312.app）"
echo "  - 临时文件和日志"
echo ""
echo "将保留："
echo "  - README.md（主文档）"
echo "  - 完整打包流程.md"
echo "  - Python3.12打包成功.md"
echo "  - DMG安装包说明.md"
echo "  - 快速使用指南.md"
echo "  - 有效的打包脚本"
echo "  - iMusic_py312.app"
echo "  - iMusic-1.0.1-py312.dmg"
echo ""

read -p "确认清理？(y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ 取消清理"
    exit 0
fi

echo ""
echo "🗑️  开始清理..."
echo ""

# 1. 删除旧的调试文档
echo "1️⃣ 删除旧的调试文档..."
rm -f ANDROID_COMPATIBILITY.md
rm -f BUGFIX_DEADLOCK.md
rm -f FINAL_SUMMARY.md
rm -f PACKAGING.md
rm -f Python3.14兼容性说明.md
rm -f Python打包最终方案.md
rm -f Python打包深度诊断.md
rm -f QUICKSTART.md
rm -f SOLUTION_SUMMARY.md
rm -f SYNC_COMPLETE.md
rm -f 总结.md
rm -f 修复VLC错误和编码问题.md
rm -f 快速开始.md
rm -f 打包说明.md
rm -f 最终建议.md
rm -f 清理完成.md
rm -f 项目结构.md
rm -f 推荐使用Swift版本.md
rm -f 完成总结_打包修复.md
rm -f 最终说明_应用可以运行.md
rm -f 应用运行说明.md
rm -f 打包成功说明.md
rm -f 打包问题修复.md
rm -f 简单使用指南.md
rm -f 打包问题已解决.md
rm -f 打包完成总结.md
rm -f DMG快速指南.md

echo "   ✅ 已删除旧文档"

# 2. 删除失败的打包脚本
echo "2️⃣ 删除失败的打包脚本..."
rm -f create_app.sh
rm -f create_app_debug.sh
rm -f create_app_fixed.sh
rm -f create_app_py2app.sh
rm -f create_dmg.sh
rm -f fix_packaging.sh
rm -f cleanup_old_apps.sh
rm -f create_shortcut.sh
rm -f debug_run.sh
rm -f diagnose_app.sh
rm -f find_vlc.sh
rm -f simple_test.sh
rm -f test_app.sh
rm -f test_py312_app.sh
rm -f test_run.sh
rm -f 启动应用.sh
rm -f 诊断应用问题.sh

echo "   ✅ 已删除旧脚本"

# 3. 删除测试应用（保留 iMusic_py312.app）
echo "3️⃣ 删除测试应用..."
rm -rf iMusic.app
rm -rf iMusic_Debug.app
rm -rf iMusic_py2app.app

echo "   ✅ 已删除测试应用"

# 4. 删除旧的 DMG
echo "4️⃣ 删除旧的 DMG..."
rm -f iMusic-1.0.1.dmg

echo "   ✅ 已删除旧 DMG"

# 5. 删除临时文件和日志
echo "5️⃣ 删除临时文件..."
rm -f app_run.log
rm -f *.spec
rm -f setup_py2app.py
rm -rf build/
rm -rf dist/
rm -rf dmg_tmp*/
rm -rf hooks/

echo "   ✅ 已删除临时文件"

# 6. 清理 Python 缓存
echo "6️⃣ 清理 Python 缓存..."
find . -type d -name "__pycache__" -exec rm -rf {} + 2>/dev/null
find . -type f -name "*.pyc" -delete 2>/dev/null
find . -type f -name "*.pyo" -delete 2>/dev/null
find . -type d -name "*.egg-info" -exec rm -rf {} + 2>/dev/null

echo "   ✅ 已清理 Python 缓存"

echo ""
echo "=========================================="
echo "✅ 清理完成！"
echo "=========================================="
echo ""
echo "📊 保留的文件："
echo ""
echo "📚 文档："
echo "  - README.md"
echo "  - 完整打包流程.md"
echo "  - Python3.12打包成功.md"
echo "  - DMG安装包说明.md"
echo "  - 快速使用指南.md"
echo ""
echo "🔧 脚本："
echo "  - setup.sh (开发环境)"
echo "  - run.sh (运行应用)"
echo "  - setup_python312.sh (Python 3.12 环境)"
echo "  - create_app_py312.sh (打包应用)"
echo "  - create_dmg_py312.sh (创建 DMG)"
echo "  - test_final.sh (测试应用)"
echo "  - cleanup.sh (清理构建文件)"
echo ""
echo "📦 应用："
echo "  - iMusic_py312.app (打包的应用)"
echo "  - iMusic-1.0.1-py312.dmg (DMG 安装包)"
echo ""
echo "💡 提示："
echo "  - 源代码在 src/ 目录"
echo "  - Python 环境在 venv/ 和 venv312/ 目录"
echo "  - 运行 'bash run.sh' 开始开发"
echo "  - 运行 'bash create_app_py312.sh' 打包应用"
echo ""
