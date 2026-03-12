# Python 3.14 兼容性说明

## 问题

Python 3.14是一个非常新的版本（可能是预发布版本），某些依赖包可能还没有提供预编译的wheel，导致需要从源码编译。

## Pillow安装问题

### 错误信息
```
KeyError: '__version__'
ERROR: Failed to build 'Pillow' when getting requirements to build wheel
```

### 原因
Pillow 10.2.0的setup.py与Python 3.14不完全兼容。

## 解决方案

### 方案1：使用最新版本的Pillow（推荐）

```bash
source venv/bin/activate
pip3 install --upgrade Pillow
```

最新版本的Pillow通常会修复与新Python版本的兼容性问题。

### 方案2：使用预编译的wheel

```bash
source venv/bin/activate
pip3 install --upgrade --only-binary :all: Pillow
```

这会强制使用预编译的二进制包，避免从源码编译。

### 方案3：安装编译依赖后从源码编译

```bash
# 1. 安装系统依赖
brew install libjpeg libtiff little-cms2 openjpeg webp

# 2. 安装Pillow
source venv/bin/activate
pip3 install Pillow
```

### 方案4：使用Python 3.12或3.13（最稳定）

```bash
# 1. 安装Python 3.12
brew install python@3.12

# 2. 删除现有虚拟环境
rm -rf venv

# 3. 使用Python 3.12创建虚拟环境
python3.12 -m venv venv

# 4. 重新安装依赖
source venv/bin/activate
pip install -r requirements.txt
```

## 自动修复脚本

我们提供了自动修复脚本：

```bash
./fix_packaging.sh
```

这个脚本会：
1. 检测Python版本
2. 尝试多种方式安装Pillow
3. 验证安装结果
4. 安装PyInstaller

## 手动修复步骤

如果自动脚本失败，按以下步骤手动修复：

### 步骤1：激活虚拟环境
```bash
cd mac-music-player
source venv/bin/activate
```

### 步骤2：升级pip和setuptools
```bash
pip3 install --upgrade pip setuptools wheel
```

### 步骤3：安装Pillow
```bash
# 尝试方法1：最新版本
pip3 install --upgrade Pillow

# 如果失败，尝试方法2：预编译版本
pip3 install --upgrade --only-binary :all: Pillow

# 如果还是失败，尝试方法3：安装依赖后编译
brew install libjpeg libtiff little-cms2 openjpeg webp
pip3 install Pillow
```

### 步骤4：验证安装
```bash
python3 -c "import PIL; print(f'Pillow version: {PIL.__version__}')"
```

### 步骤5：安装PyInstaller
```bash
pip3 install --upgrade pyinstaller
```

### 步骤6：打包应用
```bash
./create_app.sh
```

## 替代方案：不使用图标

如果Pillow实在无法安装，可以修改spec文件，不使用PNG图标：

### 方法1：移除图标
编辑 `iMusic.spec`，将：
```python
icon='src/resources/app_icon.png',
```
改为：
```python
icon=None,
```

### 方法2：使用系统默认图标
直接删除icon参数。

### 方法3：手动创建ICNS文件

```bash
# 1. 创建iconset目录
mkdir -p icon.iconset

# 2. 生成各种尺寸的图标
sips -z 16 16     src/resources/app_icon.png --out icon.iconset/icon_16x16.png
sips -z 32 32     src/resources/app_icon.png --out icon.iconset/icon_16x16@2x.png
sips -z 32 32     src/resources/app_icon.png --out icon.iconset/icon_32x32.png
sips -z 64 64     src/resources/app_icon.png --out icon.iconset/icon_32x32@2x.png
sips -z 128 128   src/resources/app_icon.png --out icon.iconset/icon_128x128.png
sips -z 256 256   src/resources/app_icon.png --out icon.iconset/icon_128x128@2x.png
sips -z 256 256   src/resources/app_icon.png --out icon.iconset/icon_256x256.png
sips -z 512 512   src/resources/app_icon.png --out icon.iconset/icon_256x256@2x.png
sips -z 512 512   src/resources/app_icon.png --out icon.iconset/icon_512x512.png
sips -z 1024 1024 src/resources/app_icon.png --out icon.iconset/icon_512x512@2x.png

# 3. 创建ICNS文件
iconutil -c icns icon.iconset -o src/resources/app_icon.icns

# 4. 在spec文件中使用ICNS
icon='src/resources/app_icon.icns',
```

## 推荐的Python版本

为了获得最佳兼容性，推荐使用：

- ✅ Python 3.12（最稳定）
- ✅ Python 3.11（稳定）
- ✅ Python 3.10（稳定）
- ⚠️  Python 3.13（较新，大部分包支持）
- ⚠️  Python 3.14（太新，可能有兼容性问题）

## 检查当前Python版本

```bash
python3 --version
```

## 切换Python版本

### 使用Homebrew安装特定版本

```bash
# 安装Python 3.12
brew install python@3.12

# 使用Python 3.12创建虚拟环境
python3.12 -m venv venv

# 激活并安装依赖
source venv/bin/activate
pip install -r requirements.txt
```

### 使用pyenv管理多个Python版本

```bash
# 安装pyenv
brew install pyenv

# 安装Python 3.12
pyenv install 3.12.0

# 设置本地Python版本
pyenv local 3.12.0

# 创建虚拟环境
python -m venv venv

# 激活并安装依赖
source venv/bin/activate
pip install -r requirements.txt
```

## 验证修复

运行以下命令验证所有依赖都已正确安装：

```bash
source venv/bin/activate

# 检查Pillow
python3 -c "import PIL; print(f'✅ Pillow: {PIL.__version__}')"

# 检查PyInstaller
python3 -c "import PyInstaller; print(f'✅ PyInstaller: {PyInstaller.__version__}')"

# 检查其他依赖
python3 -c "import PyQt5; print('✅ PyQt5')"
python3 -c "import vlc; print('✅ python-vlc')"
python3 -c "import mutagen; print('✅ mutagen')"
python3 -c "import requests; print('✅ requests')"
```

如果所有检查都通过，就可以运行：

```bash
./create_app.sh
```

## 总结

Python 3.14太新，建议：
1. 使用 `./fix_packaging.sh` 自动修复
2. 或切换到Python 3.12/3.13
3. 或手动安装编译依赖

---

更新时间：2026-03-05
状态：✅ 已提供解决方案
