"""
py2app setup script for iMusic
使用py2app替代PyInstaller进行打包
"""

from setuptools import setup

APP = ['main.py']
DATA_FILES = [
    ('resources', ['src/resources/app_icon.png']),
]

OPTIONS = {
    'argv_emulation': False,
    'iconfile': 'src/resources/app_icon.png',
    'plist': {
        'CFBundleName': 'iMusic',
        'CFBundleDisplayName': 'iMusic',
        'CFBundleIdentifier': 'com.ijackey.iMusic',
        'CFBundleVersion': '1.0.1',
        'CFBundleShortVersionString': '1.0.1',
        'NSHighResolutionCapable': True,
        'LSMinimumSystemVersion': '10.13',
    },
    'packages': ['PyQt5', 'vlc', 'mutagen', 'requests', 'sqlite3'],
    'includes': [
        'PyQt5.QtCore',
        'PyQt5.QtGui', 
        'PyQt5.QtWidgets',
    ],
    'frameworks': [
        '/Applications/VLC.app/Contents/MacOS/lib/libvlc.dylib',
        '/Applications/VLC.app/Contents/MacOS/lib/libvlccore.dylib',
    ],
    'resources': ['src/resources/app_icon.png'],
}

setup(
    name='iMusic',
    app=APP,
    data_files=DATA_FILES,
    options={'py2app': OPTIONS},
    setup_requires=['py2app'],
)
