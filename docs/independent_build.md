# 脱离 Android Studio 独立编译与模拟器调试指南

## 项目概览

| 项目属性 | 值 |
|---|---|
| 项目名称 | AircraftWar |
| 包名 | `edu.hitsz.aircraftwar` |
| Gradle 版本 | 9.3.1 |
| AGP (Android Gradle Plugin) 版本 | 9.1.0 |
| JDK 版本要求 | 21 |
| compileSdk | 36 |
| minSdk | 24 |
| targetSdk | 36 |
| 语言 | Kotlin |

---

## 一、环境准备

### 1.1 安装 JDK 21

项目要求 JDK 21（见 `gradle/gradle-daemon-jvm.properties` 中 `toolchainVersion=21`）。

**macOS (Homebrew):**
```bash
brew install openjdk@21
```

安装后设置环境变量：
```bash
# ~/.zshrc
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

验证：
```bash
java -version
# 应输出类似: openjdk version "21.x.x"
```

### 1.2 安装 Android SDK（命令行工具）

如果系统中已有 Android Studio 安装过的 SDK（路径通常为 `~/Library/Android/sdk`），可以直接复用。

**如果没有 Android Studio，手动安装 SDK：**

1. 从 [Android 命令行工具下载页](https://developer.android.com/studio#command-line-tools-only) 下载 **Command line tools only**。

2. 创建 SDK 目录并解压：
   ```bash
   mkdir -p ~/Library/Android/sdk/cmdline-tools
   # 将下载的 zip 解压到该目录，并重命名为 latest
   unzip commandlinetools-mac-*.zip -d ~/Library/Android/sdk/cmdline-tools/
   mv ~/Library/Android/sdk/cmdline-tools/cmdline-tools ~/Library/Android/sdk/cmdline-tools/latest
   ```

3. 设置环境变量：
   ```bash
   # ~/.zshrc
   export ANDROID_HOME=~/Library/Android/sdk
   export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
   ```

4. 使用 `sdkmanager` 安装必要组件：
   ```bash
   sdkmanager --update
   sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"
   ```

5. 接受许可协议：
   ```bash
   sdkmanager --licenses
   ```

### 1.3 配置 `local.properties`

项目根目录下的 `local.properties` 文件需要指向你的 Android SDK 路径：

```properties
sdk.dir=/Users/<你的用户名>/Library/Android/sdk
```

> **注意**：该文件不应提交到版本控制系统，每个开发者需要根据自己的环境配置。

---

## 二、命令行编译

### 2.1 使用 Gradle Wrapper 编译

项目自带 Gradle Wrapper（`gradlew`），无需全局安装 Gradle。首次运行时会自动下载 Gradle 9.3.1。

**进入项目根目录：**
```bash
cd /path/to/AircraftWar
```

**编译 Debug APK：**
```bash
./gradlew assembleDebug
```

**编译 Release APK：**
```bash
./gradlew assembleRelease
```

**清理并重新编译：**
```bash
./gradlew clean assembleDebug
```

### 2.2 APK 输出路径

编译成功后，APK 文件位于：

| 构建类型 | 路径 |
|---|---|
| Debug | `app/build/outputs/apk/debug/app-debug.apk` |
| Release | `app/build/outputs/apk/release/app-release-unsigned.apk` |

### 2.3 常用 Gradle 命令

```bash
# 查看所有可用任务
./gradlew tasks

# 仅编译不打包
./gradlew compileDebugSources

# 运行单元测试
./gradlew test

# 运行 instrumented 测试（需要连接设备或模拟器）
./gradlew connectedAndroidTest

# 查看项目依赖
./gradlew app:dependencies

# 检查构建信息
./gradlew buildEnvironment
```

### 2.4 加速编译的技巧

在 `gradle.properties` 中可以添加以下配置：
```properties
# 启用并行编译
org.gradle.parallel=true
# 启用构建缓存
org.gradle.caching=true
# 启用配置缓存
org.gradle.configuration-cache=true
```

---

## 三、模拟器管理与调试

### 3.1 安装模拟器组件

```bash
sdkmanager "emulator" "system-images;android-36;google_apis;arm64-v8a"
```

> **说明**：
> - Apple Silicon (M1/M2/M3/M4) Mac 请使用 `arm64-v8a` 架构的系统镜像。
> - Intel Mac 请使用 `x86_64` 架构：`system-images;android-36;google_apis;x86_64`。

### 3.2 创建 AVD（Android Virtual Device）

```bash
avdmanager create avd \
  --name "Pixel_7_API_36" \
  --package "system-images;android-36;google_apis;arm64-v8a" \
  --device "pixel_7"
```

**查看已创建的 AVD 列表：**
```bash
avdmanager list avd
```

**删除 AVD：**
```bash
avdmanager delete avd --name "Pixel_7_API_36"
```

### 3.3 启动模拟器

```bash
emulator -avd Pixel_7_API_36
```

**常用启动参数：**
```bash
# 无窗口模式（适合 CI/CD）
emulator -avd Pixel_7_API_36 -no-window

# 指定内存大小
emulator -avd Pixel_7_API_36 -memory 2048

# 使用 GPU 加速
emulator -avd Pixel_7_API_36 -gpu host

# 后台启动
emulator -avd Pixel_7_API_36 &
```

### 3.4 安装 APK 到模拟器

确保模拟器已启动并运行，然后使用 `adb` 安装：

```bash
# 查看已连接的设备/模拟器
adb devices

# 安装 Debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 强制覆盖安装（已有旧版本时）
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 安装并替换已有应用（保留数据）
adb install -r -d app/build/outputs/apk/debug/app-debug.apk
```

### 3.5 一键编译并安装到设备

Gradle 提供了直接安装到设备的任务：

```bash
# 编译 Debug 版本并安装到已连接的设备/模拟器
./gradlew installDebug

# 安装后自动启动应用
adb shell am start -n edu.hitsz.aircraftwar/.MainActivity
```

**组合命令（编译 + 安装 + 启动）：**
```bash
./gradlew installDebug && adb shell am start -n edu.hitsz.aircraftwar/.MainActivity
```

---

## 四、调试技巧

### 4.1 查看应用日志

```bash
# 查看所有日志
adb logcat

# 按应用包名过滤日志
adb logcat --pid=$(adb shell pidof edu.hitsz.aircraftwar)

# 按标签过滤
adb logcat -s "MainActivity"

# 仅显示 Error 级别
adb logcat *:E

# 清除日志缓冲区
adb logcat -c
```

### 4.2 截图与录屏

```bash
# 截图
adb shell screencap /sdcard/screenshot.png
adb pull /sdcard/screenshot.png ./screenshot.png

# 录屏（最长 180 秒）
adb shell screenrecord /sdcard/recording.mp4
# Ctrl+C 停止录制
adb pull /sdcard/recording.mp4 ./recording.mp4
```

### 4.3 应用管理

```bash
# 卸载应用
adb uninstall edu.hitsz.aircraftwar

# 强制停止应用
adb shell am force-stop edu.hitsz.aircraftwar

# 清除应用数据
adb shell pm clear edu.hitsz.aircraftwar

# 查看应用信息
adb shell dumpsys package edu.hitsz.aircraftwar
```

### 4.4 文件传输

```bash
# 推送文件到设备
adb push local_file.txt /sdcard/

# 从设备拉取文件
adb pull /sdcard/remote_file.txt ./
```

---

## 五、完整工作流示例

以下是一个从零开始的完整操作流程：

```bash
# 1. 进入项目目录
cd /path/to/AircraftWar

# 2. 确认环境
java -version          # 确认 JDK 21
echo $ANDROID_HOME     # 确认 SDK 路径

# 3. 编译 Debug APK
./gradlew assembleDebug

# 4. 启动模拟器（新终端窗口）
emulator -avd Pixel_7_API_36 &

# 5. 等待模拟器完全启动
adb wait-for-device
adb shell getprop sys.boot_completed  # 输出 1 表示启动完成

# 6. 安装 APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 7. 启动应用
adb shell am start -n edu.hitsz.aircraftwar/.MainActivity

# 8. 查看日志
adb logcat --pid=$(adb shell pidof edu.hitsz.aircraftwar)
```

---

## 六、常见问题排查

### Q1: `./gradlew` 提示权限不足
```bash
chmod +x gradlew
```

### Q2: 找不到 Android SDK
确认 `local.properties` 中 `sdk.dir` 路径正确，或设置环境变量：
```bash
export ANDROID_HOME=~/Library/Android/sdk
```

### Q3: 编译时提示缺少 SDK 平台或构建工具
```bash
sdkmanager "platforms;android-36" "build-tools;36.0.0"
```

### Q4: 模拟器启动后黑屏
尝试使用软件渲染：
```bash
emulator -avd Pixel_7_API_36 -gpu swiftshader_indirect
```

### Q5: `adb devices` 看不到模拟器
```bash
# 重启 adb 服务
adb kill-server
adb start-server
adb devices
```

### Q6: Gradle 下载依赖缓慢
可以在 `gradle.properties` 中配置代理：
```properties
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7890
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7890
```

或者在 `settings.gradle.kts` 的 `repositories` 中添加国内镜像源（如阿里云）：
```kotlin
maven { url = uri("https://maven.aliyun.com/repository/google") }
maven { url = uri("https://maven.aliyun.com/repository/central") }
```
