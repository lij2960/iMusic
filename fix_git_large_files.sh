#!/bin/bash

echo "=========================================="
echo "🔧 修复 Git 大文件问题"
echo "=========================================="
echo ""

echo "步骤 1: 从 Git 缓存中移除大文件..."
git rm --cached -r mac-music-player/*.dmg 2>/dev/null || true
git rm --cached -r mac-music-player/*.app 2>/dev/null || true

echo ""
echo "步骤 2: 重置到上一次提交..."
git reset --soft HEAD~1

echo ""
echo "步骤 3: 重新添加文件（排除大文件）..."
git add .

echo ""
echo "步骤 4: 重新提交..."
git commit -m "清理项目，整理文档（排除大文件）

- 删除了 50+ 个无用文件
- 精简文档到 5 个核心文档
- 更新 .gitignore 排除 .app 和 .dmg 文件
- 保留源代码和脚本"

echo ""
echo "=========================================="
echo "✅ 完成！"
echo "=========================================="
echo ""
echo "📊 检查状态："
git status

echo ""
echo "🚀 下一步："
echo "  推送到远程："
echo "  git push origin master"
echo ""
echo "💡 提示："
echo "  - .app 和 .dmg 文件已被 .gitignore 忽略"
echo "  - 这些文件仍在本地，但不会上传到 GitHub"
echo "  - 用户可以通过运行脚本自己生成这些文件"
echo ""
