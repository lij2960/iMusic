#!/bin/bash

# 创建修复版macOS应用（包含VLC库）

set -e

APP_NAME="iMusic"
VERSION="1.0.1"
VLC_LIB_PATH="/Applications/VLC.app/Contents/MacOS/lib"

echo "=========================================="
echo "创建 ${APP_NAME} 应用（修复版）"
echo "版本: ${VERSION}"
echo "=========================================="

# 获取当前目录的绝对路径
CURRENT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${CURRENT_DIR}"

# 检查VLC是否安装
if [ ! -d "${VLC_LIB_PATH}" ]; then
    echo "❌ 错误：未找到VLC库"
    echo "请安装VLC.app到Applications文件夹"
    echo "下载地址: https://www.videolan.org/vlc/"
    exit 1
fi

echo "✅ 找到VLC库: ${VLC_LIB_PATH}"
echo ""

# 检查虚拟环境
if [ ! -d "venv" ]; then
    echo "❌ 错误：未找到虚拟环境"
    echo "请先运行: ./setup.sh"
    exit 1
fi

# 激活虚拟环境
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
rm -rf build dist "${APP_NAME}.app" "${APP_NAME}.spec"

# 创建PyInstaller配置文件
echo ""
echo "📝 创建PyInstaller配置..."
cat > "${APP_NAME}.spec" << SPEC_EOF
# -*- mode: python ; coding: utf-8 -*-

block_cipher = None

# VLC库路径
vlc_lib_path = '${VLC_LIB_PATH}'

# 收集VLC库文件
vlc_binaries = [
    (vlc_lib_path + '/libvlc.dylib', 'lib'),
    (vlc_lib_path + '/libvlccore.dylib', 'lib'),
]

# 收集VLC插件（可选，但建议包含）
import os
vlc_plugins_path = vlc_lib_path + '/../plugins'
if os.path.exists(vlc_plugins_path):
    for root, dirs, files in os.walk(vlc_plugins_path):
        for file in files:
            if file.endswith('.dylib'):
                rel_path = os.path.relpath(root, vlc_lib_path + '/..')
                vlc_binaries.append((os.path.join(root, file), rel_path))

a = Analysis(
    ['main.py'],
    pathex=[],
    binaries=vlc_binaries,
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
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    console=False,
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
    upx=True,
    upx_exclude=[],
    name='iMusic',
)

app = BUNDLE(
    coll,
    name='iMusic.app',
    icon='src/resources/app_icon.png',
    bundle_identifier='com.ijackey.iMusic',
    version='1.0.1',
    info_plist={
        'CFBundleName': 'iMusic',
        'CFBundleDisplayName': 'iMusic',
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
echo "🔨 使用PyInstaller构建应用..."
echo "这可能需要几分钟时间..."
pyinstaller --clean --noconfirm "${APP_NAME}.spec"

# 检查构建结果
if [ -d "dist/${APP_NAME}.app" ]; then
    echo ""
    echo "✅ 应用构建成功！"
    echo ""
    
    # 移动到当前目录
    mv "dist/${APP_NAME}.app" "./"
    
    # 显示应用信息
    APP_SIZE=$(du -sh "${APP_NAME}.app" | cut -f1)
    echo "📦 应用信息:"
    echo "  名称: ${APP_NAME}"
    echo "  版本: ${VERSION}"
    echo "  大小: ${APP_SIZE}"
    echo "  位置: ${CURRENT_DIR}/${APP_NAME}.app"
    echo ""
    
    # 清理构建文件
    echo "🧹 清理构建文件..."
    rm -rf build dist "${APP_NAME}.spec"
    
    echo ""
    echo "=========================================="
    echo "使用方法:"
    echo "=========================================="
    echo ""
    echo "方式1: 直接运行"
    echo "  open \"${APP_NAME}.app\""
    echo ""
    echo "方式2: 安装到Applications"
    echo "  cp -R \"${APP_NAME}.app\" /Applications/"
    echo "  open /Applications/${APP_NAME}.app"
    echo ""
    echo "方式3: 创建DMG安装包"
    echo "  ./create_dmg.sh"
    echo ""
    echo "=========================================="
    echo "✅ 修复完成！"
    echo "=========================================="
    echo ""
    echo "本版本已包含VLC库，应该可以正常运行"
    echo ""
else
    echo ""
    echo "❌ 构建失败"
    echo ""
    exit 1
fi
