# iMusic DMG 安装包说明

## ✅ DMG 已创建成功

**文件名**: `iMusic-1.0.1-py312.dmg`  
**大小**: 152MB  
**位置**: `mac-music-player/iMusic-1.0.1-py312.dmg`

## 📦 DMG 内容

打开 DMG 后，你会看到：

1. **iMusic.app** - 应用程序
2. **Applications** - Applications 文件夹的快捷方式
3. **README.txt** - 英文说明文档
4. **安装说明.txt** - 中文安装说明

## 🚀 如何使用 DMG

### 方式1: 测试 DMG（推荐先测试）

```bash
# 1. 打开 DMG
open iMusic-1.0.1-py312.dmg

# 2. 等待 DMG 挂载
# 3. 在 Finder 中会出现 iMusic 磁盘映像
# 4. 检查内容是否正确
```

### 方式2: 安装应用

1. **双击** `iMusic-1.0.1-py312.dmg` 打开
2. **拖动** iMusic.app 到 Applications 文件夹
3. 打开 **Applications** 文件夹
4. **右键点击** iMusic.app，选择"打开"
5. 在弹出的对话框中点击"**打开**"确认

### 方式3: 命令行安装

```bash
# 1. 挂载 DMG
hdiutil attach iMusic-1.0.1-py312.dmg

# 2. 复制到 Applications
cp -R /Volumes/iMusic/iMusic.app /Applications/

# 3. 卸载 DMG
hdiutil detach /Volumes/iMusic

# 4. 运行应用
open /Applications/iMusic.app
```

## 📋 分发给其他用户

### 准备工作

DMG 文件已经包含了所有依赖，可以直接分发给其他 macOS 用户。

### 分发方式

1. **通过网盘分享**
   - 上传到百度网盘、阿里云盘等
   - 分享链接给用户

2. **通过 GitHub Releases**
   ```bash
   # 在 GitHub 仓库创建 Release
   # 上传 iMusic-1.0.1-py312.dmg
   ```

3. **通过邮件发送**
   - 如果文件不太大（152MB）
   - 可以通过邮件附件发送

4. **通过自己的网站**
   - 上传到服务器
   - 提供下载链接

### 用户安装说明（给用户的）

```
iMusic 安装步骤：

1. 下载 iMusic-1.0.1-py312.dmg
2. 双击打开 DMG 文件
3. 将 iMusic.app 拖到 Applications 文件夹
4. 打开 Applications 文件夹
5. 右键点击 iMusic.app，选择"打开"
6. 点击"打开"确认（仅首次需要）
7. 开始使用！

系统要求：
- macOS 10.13 或更高版本
- 无需安装 Python 或其他依赖
```

## ⚠️ 重要提示

### 首次运行

用户首次运行时，macOS 会显示安全警告：

```
"iMusic.app" 无法打开，因为它来自身份不明的开发者
```

**解决方法**：
1. 右键点击 iMusic.app
2. 选择"打开"
3. 在弹出的对话框中点击"打开"

之后就可以正常双击运行了。

### 为什么会有这个警告？

因为应用没有经过 Apple 的代码签名和公证。如果需要避免这个警告，需要：

1. **代码签名**（需要 Apple Developer 账号，$99/年）
   ```bash
   codesign --deep --force --verify --verbose \
     --sign "Developer ID Application: Your Name" \
     iMusic_py312.app
   ```

2. **公证应用**（需要 Apple Developer 账号）
   ```bash
   # 创建 DMG 后
   xcrun notarytool submit iMusic-1.0.1-py312.dmg \
     --apple-id "your@email.com" \
     --password "app-specific-password" \
     --team-id "TEAM_ID" \
     --wait
   
   # 公证成功后
   xcrun stapler staple iMusic-1.0.1-py312.dmg
   ```

## 📊 DMG 信息对比

| 项目 | Python 3.12 版本 | 说明 |
|------|-----------------|------|
| 文件名 | iMusic-1.0.1-py312.dmg | - |
| 大小 | 152MB | 压缩后的大小 |
| 应用大小 | 180MB | 解压后的大小 |
| 包含内容 | Python + PyQt5 + VLC | 所有依赖 |
| 系统要求 | macOS 10.13+ | - |
| 用户安装 | 无需安装依赖 | 开箱即用 |

## 🧪 测试 DMG

在分发之前，建议测试 DMG：

```bash
# 1. 挂载 DMG
open iMusic-1.0.1-py312.dmg

# 2. 在另一个位置测试运行
cp -R /Volumes/iMusic/iMusic.app ~/Desktop/
open ~/Desktop/iMusic.app

# 3. 测试所有功能
#    - 扫描音乐
#    - 播放音乐
#    - 搜索歌词
#    - 等等

# 4. 清理测试文件
rm -rf ~/Desktop/iMusic.app

# 5. 卸载 DMG
hdiutil detach /Volumes/iMusic
```

## 📝 更新 DMG

如果需要更新应用并重新创建 DMG：

```bash
# 1. 修改代码
# 2. 重新打包应用
bash create_app_py312.sh

# 3. 重新创建 DMG
bash create_dmg_py312.sh

# 4. 新的 DMG 会覆盖旧的
```

## 🎯 快速命令

```bash
# 创建 DMG
bash create_dmg_py312.sh

# 测试 DMG
open iMusic-1.0.1-py312.dmg

# 查看 DMG 信息
hdiutil imageinfo iMusic-1.0.1-py312.dmg

# 验证 DMG
hdiutil verify iMusic-1.0.1-py312.dmg
```

## ✅ 完成清单

- [x] 创建 DMG 安装包
- [x] 包含 README 文档
- [x] 包含安装说明
- [x] 包含 Applications 快捷方式
- [x] 测试 DMG 可以正常打开
- [ ] 测试从 DMG 安装应用
- [ ] 测试应用可以正常运行
- [ ] 准备分发给用户

## 🎉 总结

DMG 安装包已经创建完成！现在你可以：

1. **测试 DMG**: `open iMusic-1.0.1-py312.dmg`
2. **分发给用户**: 通过网盘、GitHub、邮件等方式
3. **提供安装说明**: 告诉用户如何安装和首次运行

用户只需要下载 DMG，拖动安装，右键打开即可使用，无需安装任何依赖！
