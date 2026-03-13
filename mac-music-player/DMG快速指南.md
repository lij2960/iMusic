# iMusic DMG 快速指南

## ✅ DMG 已创建

**文件**: `iMusic-1.0.1-py312.dmg` (152MB)

## 🚀 三步创建 DMG

```bash
cd mac-music-player

# 1. 打包应用（如果还没打包）
bash create_app_py312.sh

# 2. 创建 DMG
bash create_dmg_py312.sh

# 3. 完成！
```

## 📦 测试 DMG

```bash
# 打开 DMG
open iMusic-1.0.1-py312.dmg

# 验证 DMG
hdiutil verify iMusic-1.0.1-py312.dmg
```

## 👥 分发给用户

### 用户安装步骤（复制给用户）

```
1. 双击 iMusic-1.0.1-py312.dmg
2. 拖动 iMusic.app 到 Applications 文件夹
3. 打开 Applications，右键点击 iMusic.app
4. 选择"打开"，点击"打开"确认
5. 开始使用！

系统要求：macOS 10.13+
无需安装 Python 或其他软件
```

### 分发方式

- 📤 上传到网盘（百度网盘、阿里云盘）
- 🐙 GitHub Releases
- 📧 邮件发送
- 🌐 自己的网站

## ⚠️ 首次运行提示

用户首次运行会看到安全警告，这是正常的。

**解决方法**：右键点击 → 打开 → 确认

## 📊 文件信息

| 项目 | 信息 |
|------|------|
| DMG 文件 | iMusic-1.0.1-py312.dmg |
| DMG 大小 | 152MB |
| 应用大小 | 180MB（解压后） |
| 包含内容 | Python + PyQt5 + VLC + 所有依赖 |
| 系统要求 | macOS 10.13+ |
| 用户安装 | 无需安装任何依赖 |

## 🎉 完成

DMG 安装包已经可以分发了！

详细说明请查看：[DMG安装包说明.md](DMG安装包说明.md)
