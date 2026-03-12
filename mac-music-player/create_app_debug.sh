#!/bin/bash

# 创建调试版本的macOS应用（带控制台输出）
# 用于诊断应用无法运行的问题

set -e

APP_NAME="iMusic"
VERSION="1.0.1"

echo "=========================================="
echo "创建 ${APP_NAME} 调试版本"
echo "版本: ${VERSION}"
echo "=========================================="

# 获取当前目录的绝对路径
CURRENT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${CURRENT_DIR}"

# 检查虚拟环境
if [ ! -d "venv" ]; then
    echo "❌ 错误：未找到虚拟环境"
    echo "请先运行: ./setup.sh"
    exit 1
fi

# 激活虚拟环境
echo ""
echo "🔧 激活虚拟环境..."
source venv/bin/activate

# 检查PyInstaller
if ! python3 -c "import PyInstaller" 2>/dev/null; then
    echo ""
    echo "📦 安装PyInstaller..."
    pip3 install pyinstaller
fi

# 清理之前的构建
echo ""
echo "🧹 清理之前的构建..."
rm -rf build dist "${APP_NAME}_Debug.app" "${APP_NAME}_Debug.spec"

# 创建PyInstaller配置文件（调试版本）
echo ""
echo "📝 创建PyInstaller配置（调试模式）..."
cat > "${APP_NAME}_Debug.spec" << 'SPEC_EOF'
# -*- mode: python ; coding: utf-8 -*-

block_cipher = None

a = Analysis(
    ['main.py'],
    pathex=[],
    binaries=[],
    datas=[
        ('src/resources/app_icon.png', 'resources'),
    ],
    hiddenimports=[
        'PyQt5',
        'PyQt5.QtCore',
        'PyQt5.QtGui',
        'PyQt5.QtWidgets',
        'vlc',
        'mutagen',
        'requests',
        'sqlite3',
    ],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[],
    win_no_prefer_redirects=False,
    win_private_assemblies=False,
    cipher=block_cipher,
    noarchive=False,
)

pyz = PYZ(a.pure, a.zipped_data, cipher=block_cipher)

exe = EXE(
    pyz,
    a.scripts,
    [],
    exclude_binaries=True,
    name='iMusic',
    debug=True,
    bootloader_ignore_signals=False,
    strip=False,
    upx=False,
    console=True,  # 启用控制台输出
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
)

coll = COLLECT(
    exe,
    a.binaries,
    a.zipfiles,
    a.datas,
    strip=False,
    upx=False,
    upx_exclude=[],
    name='iMusic',
)

app = BUNDLE(
    coll,
    name='iMusic_Debug.app',
    icon=None,  # 调试版本不设置图标
    bundle_identifier='com.ijackey.iMusic.debug',
    version='1.0.1',
    info_plist={
        'CFBundleName': 'iMusic Debug',
        'CFBundleDisplayName': 'iMusic Debug',
        'CFBundleShortVersionString': '1.0.1',
        'CFBundleVersion': '1.0.1',
        'NSHighResolutionCapable': True,
        'LSMinimumSystemVersion': '10.13',
        'NSHumanReadableCopyright': 'Copyright © 2026 iJackey. All rights reserved.',
    },
)
SPEC_EOF

# 使用PyInstaller构建
echo ""
echo "🔨 使用PyInstaller构建调试版本..."
echo "这可能需要几分钟时间..."
pyinstaller --clean --noconfirm "${APP_NAME}_Debug.spec"

# 检查构建结果
if [ -d "dist/${APP_NAME}_Debug.app" ]; then
    echo ""
    echo "✅ 调试版本构建成功！"
    echo ""
    
    # 移动到当前目录
    mv "dist/${APP_NAME}_Debug.app" "./"
    
    # 显示应用信息
    APP_SIZE=$(du -sh "${APP_NAME}_Debug.app" | cut -f1)
    echo "📦 应用信息:"
    echo "  名称: ${APP_NAME} Debug"
    echo "  版本: ${VERSION}"
    echo "  大小: ${APP_SIZE}"
    echo "  位置: ${CURRENT_DIR}/${APP_NAME}_Debug.app"
    echo ""
    
    # 清理构建文件
    echo "🧹 清理构建文件..."
    rm -rf build dist "${APP_NAME}_Debug.spec"
    
    echo ""
    echo "=========================================="
    echo "调试方法:"
    echo "=========================================="
    echo ""
    echo "方式1: 从终端运行（推荐）"
    echo "  ./${APP_NAME}_Debug.app/Contents/MacOS/iMusic"
    echo ""
    echo "方式2: 使用open命令"
    echo "  open \"${APP_NAME}_Debug.app\""
    echo ""
    echo "=========================================="
    echo "注意:"
    echo "=========================================="
    echo ""
    echo "✅ 调试版本会显示控制台输出"
    echo "✅ 可以看到所有错误信息"
    echo "✅ 用于诊断应用无法运行的问题"
    echo "⚠️  不要分发调试版本给用户"
    echo ""
else
    echo ""
    echo "❌ 构建失败"
    echo ""
    echo "请检查错误信息并重试"
    echo ""
    exit 1
fi
