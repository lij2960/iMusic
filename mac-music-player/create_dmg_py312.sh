#!/bin/bash

# 为 Python 3.12 版本创建 DMG 安装包

set -e

APP_NAME="iMusic"
VERSION="1.0.1"
DMG_NAME="${APP_NAME}-${VERSION}-py312.dmg"
SOURCE_APP="iMusic_py312.app"

echo "=========================================="
echo "创建 ${APP_NAME} DMG 安装包"
echo "版本: ${VERSION} (Python 3.12)"
echo "=========================================="

# 获取当前目录
CURRENT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${CURRENT_DIR}"

# 检查应用是否存在
if [ ! -d "${SOURCE_APP}" ]; then
    echo ""
    echo "❌ 错误：未找到 ${SOURCE_APP}"
    echo ""
    echo "请先运行: bash create_app_py312.sh"
    echo ""
    exit 1
fi

echo ""
echo "✅ 找到应用: ${SOURCE_APP}"
APP_SIZE=$(du -sh "${SOURCE_APP}" | cut -f1)
echo "   大小: ${APP_SIZE}"

# 删除旧的DMG
if [ -f "${DMG_NAME}" ]; then
    echo ""
    echo "🗑️  删除旧的DMG..."
    rm -f "${DMG_NAME}"
fi

# 创建临时文件夹
echo ""
echo "📁 创建临时文件夹..."
TMP_DIR="dmg_tmp_py312"
rm -rf "${TMP_DIR}"
mkdir -p "${TMP_DIR}"

# 复制应用到临时文件夹（重命名为 iMusic.app）
echo ""
echo "📦 复制应用..."
cp -R "${SOURCE_APP}" "${TMP_DIR}/${APP_NAME}.app"

# 创建Applications快捷方式
echo ""
echo "🔗 创建Applications快捷方式..."
ln -s /Applications "${TMP_DIR}/Applications"

# 创建README
echo ""
echo "📝 创建README..."
cat > "${TMP_DIR}/README.txt" << 'EOF'
iMusic - macOS 音乐播放器
版本: 1.0.1 (Python 3.12)

========================================
安装方法
========================================

1. 将 iMusic.app 拖到 Applications 文件夹
2. 打开 Applications 文件夹
3. 右键点击 iMusic.app，选择"打开"
4. 在弹出的对话框中点击"打开"确认

注意：首次运行需要右键打开，之后可以直接双击运行。

========================================
功能特性
========================================

✓ 本地音乐播放
  - 支持 MP3, WAV, FLAC, AAC, OGG, M4A, WMA
  - 高质量音频播放（VLC 引擎）

✓ 歌词功能
  - 自动加载本地歌词文件（.lrc, .txt）
  - 在线搜索歌词
  - 实时同步显示
  - 点击歌词跳转

✓ 播放控制
  - 列表循环、单曲循环、随机播放
  - 8种排序方式
  - 进度条拖动

✓ 界面
  - 专辑封面显示
  - 简洁易用的界面
  - 播放状态保存

========================================
系统要求
========================================

- macOS 10.13 或更高版本
- 无需安装 Python（已内置）
- 无需安装其他依赖

========================================
使用说明
========================================

1. 首次运行
   - 点击"扫描音乐"按钮
   - 选择音乐文件夹
   - 等待扫描完成

2. 播放音乐
   - 双击歌曲开始播放
   - 使用底部控制按钮

3. 查看歌词
   - 如果有本地歌词文件会自动加载
   - 点击"搜索歌词"在线搜索

4. 数据位置
   - 数据库: ~/Library/Application Support/iMusic/

========================================
版权信息
========================================

Copyright © 2026 iJackey. All rights reserved.

本软件基于以下开源项目：
- Python 3.12.8
- PyQt5 5.15.10
- VLC Media Player
- Mutagen

========================================
技术支持
========================================

如遇问题，请访问：
https://github.com/yourusername/iMusic

或发送邮件至：
support@example.com

========================================
EOF

# 创建安装说明（中文）
echo ""
echo "📝 创建安装说明..."
cat > "${TMP_DIR}/安装说明.txt" << 'EOF'
iMusic 安装说明

第一步：安装
  将 iMusic.app 拖到 Applications 文件夹

第二步：首次运行
  1. 打开 Applications 文件夹
  2. 找到 iMusic.app
  3. 右键点击，选择"打开"
  4. 在弹出的对话框中点击"打开"

第三步：开始使用
  1. 点击"扫描音乐"选择音乐文件夹
  2. 双击歌曲开始播放
  3. 享受音乐！

注意事项：
  - 首次运行必须右键打开
  - 之后可以直接双击运行
  - 数据保存在 ~/Library/Application Support/iMusic/
EOF

# 设置DMG背景和窗口大小（可选）
echo ""
echo "🎨 配置DMG外观..."

# 创建DMG
echo ""
echo "🔨 创建DMG（这可能需要几分钟）..."
hdiutil create -volname "${APP_NAME}" \
    -srcfolder "${TMP_DIR}" \
    -ov -format UDZO \
    -fs HFS+ \
    "${DMG_NAME}"

# 清理临时文件夹
echo ""
echo "🧹 清理临时文件..."
rm -rf "${TMP_DIR}"

# 检查结果
if [ -f "${DMG_NAME}" ]; then
    DMG_SIZE=$(du -sh "${DMG_NAME}" | cut -f1)
    
    echo ""
    echo "=========================================="
    echo "✅ DMG 创建成功！"
    echo "=========================================="
    echo ""
    echo "📦 DMG 信息:"
    echo "  名称: ${DMG_NAME}"
    echo "  大小: ${DMG_SIZE}"
    echo "  位置: ${CURRENT_DIR}/${DMG_NAME}"
    echo ""
    echo "=========================================="
    echo "测试 DMG"
    echo "=========================================="
    echo ""
    echo "1. 双击 ${DMG_NAME} 打开"
    echo "2. 检查内容是否正确"
    echo "3. 尝试将 iMusic.app 拖到 Applications"
    echo ""
    echo "=========================================="
    echo "分发说明"
    echo "=========================================="
    echo ""
    echo "✅ 可以分发给其他 macOS 用户"
    echo "✅ 包含所有依赖（Python、PyQt5、VLC）"
    echo "✅ 无需用户安装任何额外软件"
    echo ""
    echo "⚠️  注意事项:"
    echo "  - 用户首次运行需要右键 > 打开"
    echo "  - 需要 macOS 10.13 或更高版本"
    echo "  - DMG 大小约 ${DMG_SIZE}"
    echo ""
    echo "=========================================="
    echo "下一步（可选）"
    echo "=========================================="
    echo ""
    echo "如需分发给更多用户，建议："
    echo ""
    echo "1. 代码签名（需要 Apple Developer 账号）"
    echo "   codesign --deep --force --sign \"Developer ID\" iMusic_py312.app"
    echo ""
    echo "2. 公证应用（需要 Apple Developer 账号）"
    echo "   xcrun notarytool submit ${DMG_NAME} --wait"
    echo ""
    echo "3. 上传到网站或 GitHub Releases"
    echo ""
    echo "=========================================="
    echo "🎉 完成！"
    echo "=========================================="
    echo ""
else
    echo ""
    echo "❌ DMG 创建失败"
    echo ""
    echo "可能的原因："
    echo "  1. 磁盘空间不足"
    echo "  2. 权限问题"
    echo "  3. hdiutil 命令不可用"
    echo ""
    exit 1
fi
