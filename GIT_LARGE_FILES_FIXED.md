# Git 大文件问题已解决

## 问题

GitHub 不允许上传超过 100MB 的文件。DMG 文件有 151MB，导致推送失败。

## 解决方案

已从 Git 历史中移除所有大文件：
- `mac-music-player/iMusic-1.0.1-py312.dmg` (151MB)
- `mac-music-player/iMusic-1.0.1.dmg` (53MB)
- `mac-music-player/iMusic.app/` (整个目录)
- `mac-music-player/iMusic_py312.app/` (整个目录)

## 更新的 .gitignore

已更新 .gitignore 文件，忽略所有 .app 和 .dmg 文件：

```gitignore
# macOS Python app
mac-music-player/*.app/
mac-music-player/*.dmg
```

## 现在可以推送了

```bash
git push origin master
```

## 用户如何获取应用？

用户需要自己构建应用：

```bash
cd mac-music-player

# 设置 Python 3.12 环境
bash setup_python312.sh

# 打包应用
bash create_app_py312.sh

# 创建 DMG（可选）
bash create_dmg_py312.sh
```

## 或者使用 GitHub Releases

如果需要分发打包好的应用，可以使用 GitHub Releases：

1. 在 GitHub 上创建一个 Release
2. 上传 DMG 文件作为 Release 资产
3. GitHub Releases 允许上传大文件（最大 2GB）

## 文件位置

大文件仍然在本地：
- `mac-music-player/iMusic_py312.app` - 本地可用
- `mac-music-player/iMusic-1.0.1-py312.dmg` - 本地可用

但不会被 Git 跟踪和上传到 GitHub。

## 完成时间

2026-03-13
