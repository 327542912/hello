# Snake 贪吃蛇

一个用原生 Android View + Canvas 手绘实现的贪吃蛇小游戏，零第三方依赖（不依赖 androidx / appcompat / material），整个 APK 只有 25 KB 左右。

## 特性

- 纯 `android.app.Activity` + 自绘 `Canvas`，无任何 UI 框架依赖
- 支持**滑动手势**（`SwipeListener`）与方向键控制
- 网格化移动、吃食物加分、撞墙 / 撞自己结束
- minSdk 26，targetSdk 28

## 项目结构

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/snake/
│   ├── MainActivity.java     # 入口 Activity
│   ├── GameView.java         # 游戏主循环与 Canvas 绘制
│   └── SwipeListener.java    # 滑动手势识别
└── res/
    ├── layout/activity_main.xml
    ├── values/{colors,strings,themes}.xml
    └── mipmap-*/             # 各密度启动图标
```

## 构建

本项目使用**零 Gradle 手工工具链**（aapt2 + javac + d8 + zipalign + apksigner），构建脚本见 `app/.aifactory-build.sh`。

需要准备：

- Android SDK build-tools（含 `aapt2`、`zipalign`、`lib/d8.jar`、`lib/apksigner.jar`）
- `platforms/android-36/android.jar`
- JDK 8+（`javac`、`java`）
- 一个签名 keystore，密码通过环境变量 `AIF_KS_PASS` 传入

```bash
export AIF_KS_PASS='你的密码'
bash app/.aifactory-build.sh
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

> 注意：`aapt2 link` 的 `-R` 是 overlay 语义，传入自己编译的 res.zip 时必须加 `--auto-add-overlay`，否则报 `does not override an existing resource`。

## 安装

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 许可

MIT
