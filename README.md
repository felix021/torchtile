# torchtile

带亮度调节的 Android 手电筒快速设置磁贴（单击开关 / 长按调档）。

> **这是一个凑合的临时方案（workaround）**，用于应对作者某台设备上的特殊 case：OnePlus 15
> （ColorOS 16 / Android 16）的系统手电筒磁贴点击无任何反应（点击被 SystemUI 内部分发链路
> 静默吞掉，日志不可见），而相机闪光灯、torch 硬件链路本身完全正常。在定位不到系统侧
> 根因的情况下，用一个普通 TileService app 绕过系统磁贴。**它不修复任何系统问题**，系统
> 磁贴恢复正常后可以直接卸载。

## 功能

- **单击磁贴**：开关手电筒，开灯时按上次记忆的亮度点亮
- **长按磁贴**：弹出竖直滑块面板，拖到想要的档位松手即生效（灯灭时松手会直接开灯）
- 档位范围自动读取设备的 `FLASH_INFO_STRENGTH_MAXIMUM_LEVEL`（本机为 1~4 档），磁贴副标题实时显示当前档位
- 不支持亮度分级的设备自动退化为纯开关（面板不可用）
- 无需 root，无需任何权限

## 原理

- 标准 [`TileService`](https://developer.android.com/reference/android/service/quicksettings/TileService)
  + `CameraManager.setTorchMode()` 实现开关
- 亮度用 API 33 的 `CameraManager.turnOnTorchWithStrengthLevel(cameraId, level)`（注意公开 API
  里没有 `setTorchModeStrengthLevel`，调档即调用带强度的开灯接口）
- TileService 没有长按回调；长按弹面板利用了 AOSP `CustomTile.getLongClickIntent()` 的标准
  行为——长按自定义磁贴时，SystemUI 会解析 app 内声明
  `android.service.quicksettings.action.QS_TILE_PREFERENCES` 的 Activity 并打开它，把滑块
  面板做成这个 Activity 即可

## 要求

- Android 13+（API 33）且相机支持 torch strength 分级才能用亮度功能；更低版本只有开关
- 安装后需手动把磁贴加进快速设置面板

## 构建

标准 Android 工程（AGP 8.x + Kotlin）。本仓库产物用于个人设备，未上传签名配置；
自行构建时在 `app/build.gradle.kts` 里配好 `signingConfigs` 或直接用 debug 签名即可。

```bash
./gradlew assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

## 卸载

```bash
adb uninstall com.felix021.torchtile
```
