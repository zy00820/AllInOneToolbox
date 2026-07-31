# 全能工具箱 (AllInOneToolbox) V1.0.9

一款纯本地离线安卓工具APP，全程无服务器、无联网、无广告、无后台请求。

## 功能特性

### 1. 首页 - 硬件检测
- CPU信息（型号、核心数、最高频率）
- GPU信息（型号、厂商）
- 屏幕参数（分辨率、密度、尺寸）
- 电池信息（电量、状态、技术）
- 系统版本信息
- 设备信息（品牌、型号、设备代码）
- 一键导出TXT硬件报告

### 2. 工具箱
- APK提取：提取已安装应用的APK文件
- 应用信息：查看已安装应用的详细信息
- 系统快捷跳转：原生设置、开发者选项、显示设置、WiFi、蓝牙、安全设置等

### 3. 固件查询
- 品牌筛选
- 机型版本搜索
- V1.0版本为本地UI预览

### 4. 我的
- 深色模式开关
- Shizuku权限状态检测
- 会员激活
- 检查更新
- 关于软件
- 了解开发者

### 5. 会员系统
- 50个激活码，每个仅支持使用1次
- 设备代码自动生成
- 离线验证，无需服务器
- 会员功能：APK提取、应用详细信息、Shizuku授权

## 技术栈
- 语言: Kotlin
- UI: Jetpack Compose + Material 3
- 架构: MVVM
- 构建: Gradle 8.5 + AGP 8.2.0
- 最低支持: Android 7.0 (API 24)
- 目标版本: Android 14 (API 34)

## 构建方法

### 环境要求
- JDK 17+
- Android SDK (compileSdk 34)
- Gradle 8.5+

### 构建步骤

1. 克隆或解压工程到本地
2. 修改 `local.properties` 指定SDK路径:
   ```
   sdk.dir=/path/to/android/sdk
   ```
3. 执行构建:
   ```bash
   ./gradlew assembleDebug   # Debug版本
   ./gradlew assembleRelease  # Release版本（需配置签名）
   ```
4. APK输出路径: `app/build/outputs/apk/debug/app-debug.apk`

### Release签名配置
在 `app/build.gradle` 中添加:
```gradle
android {
    signingConfigs {
        release {
            storeFile file("release.keystore")
            storePassword "your_password"
            keyAlias "your_key_alias"
            keyPassword "your_key_password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

## 开发者
- 张岳（总负责人）
- 黄松（UI设计）

## 版本日志

### V1.0.9
- 新增会员激活系统
- 新增了解开发者页面
- 新增检查更新功能
- 新增系统快捷跳转功能
- 优化硬件检测准确性

### V1.0.8
- 新增深色模式支持
- 优化用户界面

### V1.0.5
- 首个稳定版本发布
- 提供硬件检测基础功能
- 提供工具箱核心功能

## 合规声明
- 不申请多余权限
- 无隐私收集
- 无后台行为
- 完全离线可用
- 无广告、无追踪

## 注意事项
本应用完全在本地运行，不收集任何用户数据。
APK提取、应用信息查看等功能需要会员激活后使用。

## License
© 2024 全能工具箱团队. 保留所有权利.
