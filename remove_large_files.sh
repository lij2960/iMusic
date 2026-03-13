#!/bin/bash

echo "=========================================="
echo "🗑️  从 Git 历史中移除大文件"
echo "=========================================="
echo ""

echo "⚠️  警告：此操作会重写 Git 历史！"
echo ""
echo "将移除以下文件："
echo "  - mac-music-player/iMusic-1.0.1-py312.dmg (151MB)"
echo "  - mac-music-player/iMusic-1.0.1.dmg (53MB)"
echo "  - mac-music-player/iMusic.app/ (整个目录)"
echo "  - mac-music-player/iMusic_py312.app/ (整个目录)"
echo ""

read -p "确认继续？(y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ 取消操作"
    exit 0
fi

echo ""
echo "🔧 开始移除大文件..."
echo ""

# 使用 git filter-branch 移除大文件
echo "1️⃣ 移除 DMG 文件..."
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch mac-music-player/*.dmg' \
  --prune-empty --tag-name-filter cat -- --all

echo ""
echo "2️⃣ 移除 .app 目录..."
git filter-branch --force --index-filter \
  'git rm -r --cached --ignore-unmatch mac-music-player/*.app' \
  --prune-empty --tag-name-filter cat -- --all

echo ""
echo "3️⃣ 清理引用..."
rm -rf .git/refs/original/
git reflog expire --expire=now --all
git gc --prune=now --aggressive

echo ""
echo "=========================================="
echo "✅ 大文件已从 Git 历史中移除"
echo "=========================================="
echo ""
echo "📊 仓库大小："
du -sh .git
echo ""
echo "🚀 下一步："
echo "  1. 强制推送到远程："
echo "     git push origin --force --all"
echo ""
echo "  2. 如果有标签，也需要强制推送："
echo "     git push origin --force --tags"
echo ""
echo "⚠️  注意："
echo "  - 其他协作者需要重新克隆仓库"
echo "  - 或者使用 git pull --rebase"
echo ""
