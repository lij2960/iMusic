#!/bin/bash

# 创建可双击运行的macOS应用
# 版本: 1.0.0

set -e

APP_NAME="音乐播放器"
VERSION="1.0.0"

echo "=========================================="
echo "创建macOS应用包装器"
echo "版本: ${VERSION}"
echo "=========================================="

# 获取当前目录的绝对路径
CURRENT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 创建.app目录结构
APP_DIR="${CURRENT_DIR}/${APP_NAME}.app"
CONTENTS_DIR="${APP_DIR}/Contents"
MACOS_DIR="${CONTENTS_DIR}/MacOS"
RESOURCES_DIR="${CONTENTS_DIR}/Resources"

echo ""
echo "🗂️  创建应用目录结构..."
rm -rf "${APP_DIR}"
mkdir -p "${MACOS_DIR}"
mkdir -p "${RESOURCES_DIR}"

# 创建启动脚本
echo ""
echo "📝 创建启动脚本..."
cat > "${MACOS_DIR}/${APP_NAME}" << EOF
#!/bin/bash

# 获取应用所在目录
APP_DIR="\$(cd "\$(dirname "\${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_DIR="\${APP_DIR}/.."

# 切换到项目目录
cd "\${PROJECT_DIR}"

# 激活虚拟环境
if [ -d "venv" ]; then
    source venv/bin/activate
fi

# 运行应用
python3 main.py
EOF

chmod +x "${MACOS_DIR}/${APP_NAME}"

# 创建Info.plist
echo ""
echo "📄 创建Info.plist..."
cat > "${CONTENTS_DIR}/Info.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>${APP_NAME}</string>
    <key>CFBundleDisplayName</key>
    <string>${APP_NAME}</string>
    <key>CFBundleIdentifier</key>
    <string>com.ijackey.musicplayer</string>
    <key>CFBundleVersion</key>
    <string>${VERSION}</string>
    <key>CFBundleShortVersionString</key>
    <string>${VERSION}</string>
    <key>CFBundleExecutable</key>
    <string>${APP_NAME}</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>LSMinimumSystemVersion</key>
    <string>10.13</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
EOF

# 复制图标（如果存在）
if [ -f "src/resources/app_icon.png" ]; then
    echo ""
    echo "🎨 复制应用图标..."
    cp "src/resources/app_icon.png" "${RESOURCES_DIR}/"
fi

echo ""
echo "✅ 应用创建成功！"
echo ""
echo "应用位置: ${APP_DIR}"
echo ""
echo "使用方法:"
echo "1. 双击 ${APP_NAME}.app 启动"
echo "2. 或拖到Applications文件夹"
echo "3. 或从终端运行: open \"${APP_DIR}\""
echo ""
echo "=========================================="
echo "注意事项:"
echo "- 应用依赖当前目录的Python环境"
echo "- 不要移动或删除项目文件夹"
echo "- 如果移动了项目，需要重新运行此脚本"
echo "=========================================="
