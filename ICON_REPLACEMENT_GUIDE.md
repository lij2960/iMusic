# 📱 iMusic 应用图标替换指南

## 🎯 图标要求

**提供的图标**: `iMusic.png`
- 设计风格：深蓝色渐变背景，银色边框
- 主要元素：iMusic 文字 + 音频波形图案
- 标语：SOUND. REDEFINED.
- 日期：FEB. 3, 2026

## 📐 需要的图标尺寸

### Android 图标尺寸规范
```
mipmap-mdpi/     - 48x48 px
mipmap-hdpi/     - 72x72 px  
mipmap-xhdpi/    - 96x96 px
mipmap-xxhdpi/   - 144x144 px
mipmap-xxxhdpi/  - 192x192 px
```

## 🔧 替换步骤

### 1. 图片处理
使用图片编辑软件（如 Photoshop、GIMP 等）将 `iMusic.png` 调整为以下尺寸：

```bash
# 创建不同尺寸的图标
iMusic_48.png   -> 48x48 px   (mdpi)
iMusic_72.png   -> 72x72 px   (hdpi)
iMusic_96.png   -> 96x96 px   (xhdpi)
iMusic_144.png  -> 144x144 px (xxhdpi)
iMusic_192.png  -> 192x192 px (xxxhdpi)
```

### 2. 文件替换
将生成的图标文件重命名并替换到对应目录：

```bash
# 替换启动图标
cp iMusic_48.png  /Volumes/Jackey/iMusic/app/src/main/res/mipmap-mdpi/ic_launcher.png
cp iMusic_72.png  /Volumes/Jackey/iMusic/app/src/main/res/mipmap-hdpi/ic_launcher.png
cp iMusic_96.png  /Volumes/Jackey/iMusic/app/src/main/res/mipmap-xhdpi/ic_launcher.png
cp iMusic_144.png /Volumes/Jackey/iMusic/app/src/main/res/mipmap-xxhdpi/ic_launcher.png
cp iMusic_192.png /Volumes/Jackey/iMusic/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png

# 替换圆形图标
cp iMusic_48.png  /Volumes/Jackey/iMusic/app/src/main/res/mipmap-mdpi/ic_launcher_round.png
cp iMusic_72.png  /Volumes/Jackey/iMusic/app/src/main/res/mipmap-hdpi/ic_launcher_round.png
cp iMusic_96.png  /Volumes/Jackey/iMusic/app/src/main/res/mipmap-xhdpi/ic_launcher_round.png
cp iMusic_144.png /Volumes/Jackey/iMusic/app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png
cp iMusic_192.png /Volumes/Jackey/iMusic/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png
```

### 3. 删除旧的 WebP 文件
```bash
# 删除原有的 webp 格式图标
rm /Volumes/Jackey/iMusic/app/src/main/res/mipmap-*/ic_launcher.webp
rm /Volumes/Jackey/iMusic/app/src/main/res/mipmap-*/ic_launcher_round.webp
```

## 🛠️ 自动化脚本

创建一个脚本来自动处理图标替换：

```bash
#!/bin/bash
# icon_replace.sh

# 设置源图片路径
SOURCE_IMAGE="iMusic.png"
RES_DIR="/Volumes/Jackey/iMusic/app/src/main/res"

# 检查源图片是否存在
if [ ! -f "$SOURCE_IMAGE" ]; then
    echo "错误: 找不到源图片 $SOURCE_IMAGE"
    exit 1
fi

# 使用 ImageMagick 或 sips 调整图片尺寸
echo "正在生成不同尺寸的图标..."

# 生成各种尺寸
sips -z 48 48 "$SOURCE_IMAGE" --out "ic_launcher_48.png"
sips -z 72 72 "$SOURCE_IMAGE" --out "ic_launcher_72.png"
sips -z 96 96 "$SOURCE_IMAGE" --out "ic_launcher_96.png"
sips -z 144 144 "$SOURCE_IMAGE" --out "ic_launcher_144.png"
sips -z 192 192 "$SOURCE_IMAGE" --out "ic_launcher_192.png"

# 删除旧的 webp 文件
echo "删除旧的图标文件..."
find "$RES_DIR" -name "ic_launcher*.webp" -delete

# 复制新图标到对应目录
echo "复制新图标..."
cp ic_launcher_48.png "$RES_DIR/mipmap-mdpi/ic_launcher.png"
cp ic_launcher_48.png "$RES_DIR/mipmap-mdpi/ic_launcher_round.png"

cp ic_launcher_72.png "$RES_DIR/mipmap-hdpi/ic_launcher.png"
cp ic_launcher_72.png "$RES_DIR/mipmap-hdpi/ic_launcher_round.png"

cp ic_launcher_96.png "$RES_DIR/mipmap-xhdpi/ic_launcher.png"
cp ic_launcher_96.png "$RES_DIR/mipmap-xhdpi/ic_launcher_round.png"

cp ic_launcher_144.png "$RES_DIR/mipmap-xxhdpi/ic_launcher.png"
cp ic_launcher_144.png "$RES_DIR/mipmap-xxhdpi/ic_launcher_round.png"

cp ic_launcher_192.png "$RES_DIR/mipmap-xxxhdpi/ic_launcher.png"
cp ic_launcher_192.png "$RES_DIR/mipmap-xxxhdpi/ic_launcher_round.png"

# 清理临时文件
rm ic_launcher_*.png

echo "图标替换完成！"
echo "请运行以下命令重新构建应用："
echo "cd /Volumes/Jackey/iMusic && ./gradlew assembleDebug"
```

## 🎨 图标设计建议

### 适配性优化
1. **圆形适配**: 确保图标在圆形裁剪下仍然美观
2. **小尺寸清晰**: 在48x48像素下仍能清晰识别
3. **背景处理**: 考虑透明背景或适配系统主题

### 品牌一致性
- 保持深蓝色科技风格
- 银色边框与应用主题呼应
- iMusic 品牌标识清晰可见

## 📱 验证步骤

### 1. 构建应用
```bash
cd /Volumes/Jackey/iMusic
./gradlew assembleDebug
```

### 2. 安装测试
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 3. 检查效果
- 在应用抽屉中查看图标
- 在桌面上查看图标
- 在设置-应用管理中查看图标
- 测试不同系统主题下的显示效果

## 🔍 注意事项

### 文件格式
- 推荐使用 PNG 格式
- 确保透明背景正确处理
- 避免使用过于复杂的细节

### 兼容性
- 测试在不同 Android 版本上的显示
- 确保在不同设备密度下都清晰
- 验证自适应图标的效果

## 🎉 完成后效果

替换完成后，iMusic 应用将拥有：
- 专业的品牌图标
- 与应用主题一致的视觉风格
- 在各种设备上的完美显示效果
- 提升的品牌识别度

按照以上步骤完成图标替换后，重新构建应用即可看到新的图标效果！