#!/bin/bash

# 创建DMG安装包
# 需要先运行 create_app.sh 创建应用

set -e

APP_NAME="iMusic"
VERSION="1.0.1"
DMG_NAME="${APP_NAME}-${VERSION}.dmg"

echo "=========================================="
echo "创建 ${APP_NAME} DMG安装包"
echo "版本: ${VERSION}"
echo "=========================================="

# 获取当前目录
CURRENT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${CURRENT_DIR}"

# 检查应用是否存在
if [ ! -d "${APP_NAME}.app" ]; then
    echo ""
    echo "❌ 错误：未找到 ${APP_NAME}.app"
    echo ""
    echo "请先运行: ./create_app.sh"
    echo ""
    exit 1
fi

# 删除旧的DMG
if [ -f "${DMG_NAME}" ]; then
    echo ""
    echo "🗑️  删除旧的DMG..."
    rm -f "${DMG_NAME}"
fi

# 创建临时文件夹
echo ""
echo "📁 创建临时文件夹..."
TMP_DIR="dmg_tmp"
rm -rf "${TMP_DIR}"
mkdir -p "${TMP_DIR}"

# 复制应用到临时文件夹
echo ""
echo "📦 复制应用..."
cp -R "${APP_NAME}.app" "${TMP_DIR}/"

# 创建Applications快捷方式
echo ""
echo "🔗 创建Applications快捷方式..."
ln -s /Applications "${TMP_DIR}/Applications"

# 创建README
echo ""
echo "📝 创建README..."
cat > "${TMP_DIR}/README.txt" << 'EOF'
iMusic - macOS音乐播放器

安装方法:
1. 将 iMusic.app 拖到 Applications 文件夹
2. 打开 Applications 文件夹
3. 右键点击 iMusic > 打开
4. 点击"打开"确认

功能特性:
- 本地音乐播放
- 歌词显示和同步
- 在线歌词搜索
- 多种播放模式
- 专辑封面显示

系统要求:
- macOS 10.13 或更高版本

版权信息:
Copyright © 2026 iJackey. All rights reserved.

更多信息请访问:
https://github.com/yourusername/iMusic
EOF

# 创建DMG
echo ""
echo "🔨 创建DMG..."
hdiutil create -volname "${APP_NAME}" \
    -srcfolder "${TMP_DIR}" \
    -ov -format UDZO \
    "${DMG_NAME}"

# 清理临时文件夹
echo ""
echo "🧹 清理临时文件..."
rm -rf "${TMP_DIR}"

# 检查结果
if [ -f "${DMG_NAME}" ]; then
    DMG_SIZE=$(du -sh "${DMG_NAME}" | cut -f1)
    
    echo ""
    echo "✅ DMG创建成功！"
    echo ""
    echo "📦 DMG信息:"
    echo "  名称: ${DMG_NAME}"
    echo "  大小: ${DMG_SIZE}"
    echo "  位置: ${CURRENT_DIR}/${DMG_NAME}"
    echo ""
    echo "=========================================="
    echo "使用方法:"
    echo "=========================================="
    echo ""
    echo "1. 双击 ${DMG_NAME} 打开"
    echo "2. 将 iMusic.app 拖到 Applications 文件夹"
    echo "3. 打开 Applications 文件夹"
    echo "4. 右键点击 iMusic > 打开"
    echo ""
    echo "=========================================="
    echo "分发说明:"
    echo "=========================================="
    echo ""
    echo "✅ 可以分发给其他用户"
    echo "✅ 包含所有依赖，无需安装Python"
    echo "⚠️  用户首次运行需要右键 > 打开"
    echo ""
else
    echo ""
    echo "❌ DMG创建失败"
    exit 1
fi
