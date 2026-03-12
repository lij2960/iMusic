# Python版音乐播放器 - 打包说明

## 快速开始

### 创建独立应用
```bash
cd mac-music-player
./create_app.sh
```

### 创建DMG安装包
```bash
./create_dmg.sh
```

## 详细说明

### 使用PyInstaller打包

#### 1. 准备环境
```bash
# 安装依赖
./setup.sh

# 激活虚拟环境
source venv/bin/activate

# 安装PyInstaller
pip3 install pyinstaller
```

#### 2. 创建应用
```bash
./create_app.sh
```

**输出：** `iMusic.app`（约50-100MB）

**特点：**
- ✅ 独立应用，包含所有依赖
- ✅ 可以移动到任何位置
- ✅ 可以分发给其他用户
- ✅ 无需安装Python或依赖

#### 3. 创建DMG
```bash
./create_dmg.sh
```

**输出：** `iMusic-1.0.1.dmg`（约50-100MB）

**特点：**
- ✅ 标准的macOS安装包
- ✅ 包含Applications快捷方式
- ✅ 包含README说明
- ✅ 易于分发

## 常见问题

### Q1: create_app.sh失败
**症状：**
```
❌ 构建失败
```

**解决方案：**
1. 确保已安装所有依赖：
   ```bash
   ./setup.sh
   ```

2. 检查Python版本：
   ```bash
   python3 --version  # 需要3.8+
   ```

3. 手动安装PyInstaller：
   ```bash
   source venv/bin/activate
   pip3 install pyinstaller
   ```

4. 查看详细错误：
   ```bash
   pyinstaller --clean --noconfirm iMusic.spec
   ```

### Q2: 应用无法运行
**症状：**
- 双击应用没有反应
- 或显示"应用已损坏"

**解决方案：**

**方法1：右键打开**
```bash
右键点击 iMusic.app > 打开 > 打开
```

**方法2：移除隔离属性**
```bash
xattr -cr iMusic.app
```

**方法3：允许任何来源**
```bash
sudo spctl --master-disable
```

### Q3: 缺少依赖
**症状：**
```
ModuleNotFoundError: No module named 'xxx'
```

**解决方案：**

编辑 `iMusic.spec`，在 `hiddenimports` 中添加缺失的模块：
```python
hiddenimports=[
    'PyQt5',
    'PyQt5.QtCore',
    'PyQt5.QtGui',
    'PyQt5.QtWidgets',
    'vlc',
    'mutagen',
    'requests',
    # 添加其他缺失的模块
],
```

然后重新构建：
```bash
pyinstaller --clean --noconfirm iMusic.spec
```

### Q4: VLC库找不到
**症状：**
```
OSError: cannot load library 'libvlc.dylib'
```

**解决方案：**

**方法1：安装VLC**
```bash
brew install vlc
```

**方法2：在spec文件中添加VLC库**
```python
binaries=[
    ('/Applications/VLC.app/Contents/MacOS/lib/libvlc.dylib', '.'),
    ('/Applications/VLC.app/Contents/MacOS/lib/libvlccore.dylib', '.'),
],
```

### Q5: 应用图标不显示
**症状：**
- 应用显示默认图标

**解决方案：**

确保图标文件存在：
```bash
ls -la src/resources/app_icon.png
```

如果不存在，从Android版本复制：
```bash
cp ../app/src/main/res/mipmap-xxxhdpi/ic_launcher.png src/resources/app_icon.png
```

## 手动打包步骤

如果自动脚本不工作，可以手动操作：

### 1. 创建spec文件
```bash
source venv/bin/activate
pyi-makespec --name=iMusic \
    --windowed \
    --icon=src/resources/app_icon.png \
    --add-data="src/resources/app_icon.png:resources" \
    main.py
```

### 2. 编辑spec文件
```python
# iMusic.spec
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
    ],
    # ... 其他配置
)
```

### 3. 构建应用
```bash
pyinstaller --clean --noconfirm iMusic.spec
```

### 4. 测试应用
```bash
open dist/iMusic.app
```

### 5. 移动应用
```bash
mv dist/iMusic.app ./
```

## 优化建议

### 减小应用体积

**1. 排除不需要的模块**
```python
excludes=[
    'tkinter',
    'matplotlib',
    'numpy',
    'pandas',
],
```

**2. 使用UPX压缩**
```bash
brew install upx
```

在spec文件中启用：
```python
upx=True,
upx_exclude=[],
```

**3. 单文件模式**
```python
exe = EXE(
    pyz,
    a.scripts,
    a.binaries,  # 添加这行
    a.zipfiles,  # 添加这行
    a.datas,     # 添加这行
    [],
    name='iMusic',
    # ...
)
```

### 提高启动速度

**1. 使用--onefile模式**
```bash
pyinstaller --onefile --windowed main.py
```

**2. 预编译Python文件**
```bash
python3 -m compileall src/
```

## 分发说明

### 给其他用户

**方式1：分发.app**
```bash
# 压缩应用
zip -r iMusic.zip iMusic.app

# 或创建DMG
./create_dmg.sh
```

**方式2：上传到网盘**
- 上传 `iMusic-1.0.1.dmg`
- 提供下载链接

**方式3：GitHub Release**
```bash
# 创建release
gh release create v1.0.1 iMusic-1.0.1.dmg
```

### 用户安装步骤

1. 下载 `iMusic-1.0.1.dmg`
2. 双击打开DMG
3. 将 iMusic.app 拖到 Applications 文件夹
4. 打开 Applications 文件夹
5. 右键点击 iMusic > 打开
6. 点击"打开"确认

## 测试清单

打包后需要测试：

- [ ] 应用可以启动
- [ ] 可以添加音乐文件夹
- [ ] 可以播放音乐
- [ ] 可以显示歌词
- [ ] 可以搜索在线歌词
- [ ] 应用图标正确显示
- [ ] 专辑封面正确显示
- [ ] 播放控制正常工作
- [ ] 音量控制正常工作
- [ ] 播放模式切换正常
- [ ] 排序功能正常
- [ ] 数据库正常工作
- [ ] 应用可以正常退出

## 版本信息

- 应用名称：iMusic
- 版本号：1.0.1
- Bundle ID：com.ijackey.iMusic
- 最小系统：macOS 10.13

## 相关文档

- [README.md](README.md) - 项目说明
- [QUICKSTART.md](QUICKSTART.md) - 快速开始
- [修复VLC错误和编码问题.md](修复VLC错误和编码问题.md) - 问题修复

---

更新时间：2026-03-05
状态：✅ 可用
