# JavRanking Shell

简单的 Android WebView 外壳，打开即加载 https://javranking.cc/zh-hans/

## 功能
- 直接加载 JavRanking 中文站
- 支持返回键回退
- 无额外权限要求

## 预编译 APK
仓库中包含 `JavRanking-Shell.apk.b64`（Base64 编码的 APK）。

解码并安装：

```bash
base64 -d JavRanking-Shell.apk.b64 > JavRanking-Shell.apk
```

或者直接从对话中下载已生成的 APK 文件。

## 构建
使用 Android Studio 打开本项目，Build > Build Bundle(s) / APK(s) > Build APK(s)

## 安装
直接安装生成的 APK 即可。
