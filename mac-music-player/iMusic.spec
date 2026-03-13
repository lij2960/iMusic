# -*- mode: python ; coding: utf-8 -*-

block_cipher = None

# VLC库路径
vlc_lib_path = '/Applications/VLC.app/Contents/MacOS/lib'

# 收集VLC库文件
vlc_binaries = [
    (vlc_lib_path + '/libvlc.dylib', 'lib'),
    (vlc_lib_path + '/libvlccore.dylib', 'lib'),
]

# 收集VLC插件
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
    runtime_hooks=['hooks/runtime_hook_qt.py'],
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
