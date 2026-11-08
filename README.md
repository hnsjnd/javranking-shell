# JavRanking Shell

轻量 Android WebView 外壳，打开即加载 [JavRanking 中文站](https://javranking.cc/zh-hans/)。

## v1.1.0 功能
- 深色工具栏 + 金色进度条，避免白屏闪烁
- 右上角菜单：刷新、首页、在浏览器打开、清除缓存、桌面版网站
- 返回键按网页历史回退
- 加载失败显示中文错误页并可重试
- Cookie / DomStorage 持久化，硬件加速
- 全新 JR + 星标自适应图标

## 下载
见 [Releases](https://github.com/hnsjnd/javranking-shell/releases) 中的 `JavRanking-Shell.apk`（debug 签名，需允许「未知来源」）。

## 构建
```bash
./gradlew assembleDebug
# 产物: app/build/outputs/apk/debug/app-debug.apk
```

## 权限
仅 `INTERNET` 与 `ACCESS_NETWORK_STATE`。
