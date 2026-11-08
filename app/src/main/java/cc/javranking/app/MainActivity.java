package cc.javranking.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String HOME_URL = "https://javranking.cc/zh-hans/";
    private static final String DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private WebView webView;
    private ProgressBar progressBar;
    private boolean desktopUa;
    private String defaultUa;
    private boolean loadingError;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSystemBars();
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress);
        webView = findViewById(R.id.webview);
        ImageButton btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(this::showMenu);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }

        defaultUa = settings.getUserAgentString();
        desktopUa = getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("desktop_ua", false);
        applyUserAgent();

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setBackgroundColor(Color.parseColor("#121212"));
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (title != null && !title.isEmpty() && !title.startsWith("http")) {
                    // keep toolbar branded; optional subtitle not needed
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if (scheme == null) {
                    return false;
                }
                if ("http".equals(scheme) || "https".equals(scheme)) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ignored) {
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadingError = false;
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                CookieManager.getInstance().flush();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                if (request != null && request.isForMainFrame()) {
                    loadingError = true;
                    showErrorPage(error != null ? error.getDescription() : null);
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                                        String failingUrl) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    loadingError = true;
                    showErrorPage(description);
                }
            }
        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(HOME_URL);
        }
    }

    private void setupSystemBars() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#121212"));
        window.setNavigationBarColor(Color.parseColor("#121212"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = window.getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private void applyUserAgent() {
        WebSettings settings = webView.getSettings();
        if (desktopUa) {
            settings.setUserAgentString(DESKTOP_UA);
        } else {
            settings.setUserAgentString(defaultUa);
        }
    }

    private void showMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor, Gravity.END);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
        popup.getMenu().findItem(R.id.action_desktop_ua).setChecked(desktopUa);
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_refresh) {
                if (loadingError) {
                    webView.loadUrl(HOME_URL);
                } else {
                    webView.reload();
                }
                return true;
            }
            if (id == R.id.action_home) {
                webView.loadUrl(HOME_URL);
                return true;
            }
            if (id == R.id.action_open_browser) {
                String url = webView.getUrl();
                if (url == null || url.isEmpty()) {
                    url = HOME_URL;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    Toast.makeText(this, "无法打开浏览器", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            if (id == R.id.action_clear_cache) {
                webView.clearCache(true);
                webView.clearHistory();
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
                Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
                webView.loadUrl(HOME_URL);
                return true;
            }
            if (id == R.id.action_desktop_ua) {
                desktopUa = !desktopUa;
                item.setChecked(desktopUa);
                getSharedPreferences("prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("desktop_ua", desktopUa)
                        .apply();
                applyUserAgent();
                webView.reload();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showErrorPage(CharSequence detail) {
        String msg = detail == null ? "网络异常" : detail.toString();
        String html = "<!DOCTYPE html><html><head><meta charset='utf-8'/>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'/>"
                + "<style>"
                + "body{margin:0;background:#121212;color:#eee;font-family:sans-serif;"
                + "display:flex;align-items:center;justify-content:center;min-height:100vh;}"
                + ".box{text-align:center;padding:32px;max-width:360px;}"
                + "h1{font-size:20px;margin:0 0 12px;color:#F5BA34;}"
                + "p{font-size:14px;color:#aaa;line-height:1.5;}"
                + "a{display:inline-block;margin-top:20px;padding:12px 28px;"
                + "background:#F5BA34;color:#121212;text-decoration:none;"
                + "border-radius:8px;font-weight:bold;}"
                + "</style></head><body><div class='box'>"
                + "<h1>加载失败</h1>"
                + "<p>" + escapeHtml(msg) + "</p>"
                + "<p>请检查网络后重试</p>"
                + "<a href='" + HOME_URL + "'>重新加载</a>"
                + "</div></body></html>";
        webView.loadDataWithBaseURL(HOME_URL, html, "text/html", "UTF-8", null);
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            webView.saveState(outState);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
            CookieManager.getInstance().flush();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
