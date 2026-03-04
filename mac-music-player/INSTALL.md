# 安装指南

## 步骤1: 安装VLC媒体播放器

本程序需要VLC作为音频播放引擎。

### 方法1: 使用Homebrew（推荐）

```bash
brew install --cask vlc
```

### 方法2: 手动下载

1. 访问 https://www.videolan.org/vlc/
2. 下载macOS版本
3. 安装到Applications文件夹

## 步骤2: 安装Python依赖

### 自动安装（推荐）

```bash
cd mac-music-player
./setup.sh
```

在提示时选择选项3（跳过VLC检查，因为已经安装）

### 手动安装

```bash
cd mac-music-player

# 创建虚拟环境
python3 -m venv venv

# 激活虚拟环境
source venv/bin/activate

# 升级pip
pip install --upgrade pip

# 安装依赖
pip install -r requirements.txt
```

## 步骤3: 运行程序

### 使用运行脚本

```bash
./run.sh
```

### 手动运行

```bash
source venv/bin/activate
python main.py
```

## 验证安装

如果程序成功启动并显示主窗口，说明安装成功！

## 常见问题

### Q: pip install时出错
A: 确保使用Python 3.8或更高版本：
```bash
python3 --version
```

### Q: 找不到VLC
A: 确保VLC已安装在 `/Applications/VLC.app`

### Q: 虚拟环境激活失败
A: 确保在项目目录中运行命令：
```bash
cd mac-music-player
source venv/bin/activate
```

### Q: 依赖安装很慢
A: 可以使用国内镜像加速：
```bash
pip install -i https://pypi.tuna.tsinghua.edu.cn/simple -r requirements.txt
```

## 卸载

```bash
# 删除虚拟环境
rm -rf venv

# 删除数据库（可选）
rm music_player.db
```

## 下一步

安装完成后，请查看 [QUICKSTART.md](QUICKSTART.md) 了解如何使用。