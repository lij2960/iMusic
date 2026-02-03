#!/bin/bash

# iMusic 图标替换脚本
SOURCE_IMAGE="/Volumes/Jackey/iMusic.png"
RES_DIR="/Volumes/Jackey/iMusic/app/src/main/res"

echo "🎨 开始替换 iMusic 应用图标..."

# 检查源图片是否存在
if [ ! -f "$SOURCE_IMAGE" ]; then
    echo "❌ 错误: 找不到源图片 $SOURCE_IMAGE"
    exit 1
fi

echo "📱 生成不同尺寸的图标..."

# 创建临时目录
mkdir -p /tmp/imusic_icons

# 生成各种尺寸的图标
sips -z 48 48 "$SOURCE_IMAGE" --out "/tmp/imusic_icons/ic_launcher_48.png" > /dev/null 2>&1
sips -z 72 72 "$SOURCE_IMAGE" --out "/tmp/imusic_icons/ic_launcher_72.png" > /dev/null 2>&1
sips -z 96 96 "$SOURCE_IMAGE" --out "/tmp/imusic_icons/ic_launcher_96.png" > /dev/null 2>&1
sips -z 144 144 "$SOURCE_IMAGE" --out "/tmp/imusic_icons/ic_launcher_144.png" > /dev/null 2>&1
sips -z 192 192 "$SOURCE_IMAGE" --out "/tmp/imusic_icons/ic_launcher_192.png" > /dev/null 2>&1

echo "🗑️  删除旧的图标文件..."
# 删除旧的 webp 文件
find "$RES_DIR" -name "ic_launcher*.webp" -delete 2>/dev/null

echo "📋 复制新图标到项目目录..."

# 复制新图标到对应目录
cp "/tmp/imusic_icons/ic_launcher_48.png" "$RES_DIR/mipmap-mdpi/ic_launcher.png"
cp "/tmp/imusic_icons/ic_launcher_48.png" "$RES_DIR/mipmap-mdpi/ic_launcher_round.png"

cp "/tmp/imusic_icons/ic_launcher_72.png" "$RES_DIR/mipmap-hdpi/ic_launcher.png"
cp "/tmp/imusic_icons/ic_launcher_72.png" "$RES_DIR/mipmap-hdpi/ic_launcher_round.png"

cp "/tmp/imusic_icons/ic_launcher_96.png" "$RES_DIR/mipmap-xhdpi/ic_launcher.png"
cp "/tmp/imusic_icons/ic_launcher_96.png" "$RES_DIR/mipmap-xhdpi/ic_launcher_round.png"

cp "/tmp/imusic_icons/ic_launcher_144.png" "$RES_DIR/mipmap-xxhdpi/ic_launcher.png"
cp "/tmp/imusic_icons/ic_launcher_144.png" "$RES_DIR/mipmap-xxhdpi/ic_launcher_round.png"

cp "/tmp/imusic_icons/ic_launcher_192.png" "$RES_DIR/mipmap-xxxhdpi/ic_launcher.png"
cp "/tmp/imusic_icons/ic_launcher_192.png" "$RES_DIR/mipmap-xxxhdpi/ic_launcher_round.png"

# 清理临时文件
rm -rf /tmp/imusic_icons

echo "✅ 图标替换完成！"
echo "🔨 正在重新构建应用..."