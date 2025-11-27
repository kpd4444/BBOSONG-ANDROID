package com.cookandroid.ai_landaury.kakaomap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cookandroid.ai_landaury.R;

public class MapWebViewActivity extends AppCompatActivity {

    private static final String TAG = "WV";
    private static final int REQ_LOCATION = 1001;
    private static final String MAP_URL = "http://10.0.2.2:8080/map.html"; // 고정 상수

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_webview);

        webView = findViewById(R.id.webview);

        // --- WebView Settings ---
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setGeolocationEnabled(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);

        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cm.setAcceptThirdPartyCookies(webView, true);
        }

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        WebView.setWebContentsDebuggingEnabled(true);

        // 단일 WebChromeClient: 콘솔/권한/지오로케이션 처리
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage msg) {
                Log.d(TAG, msg.message() + " @" + msg.sourceId() + ":" + msg.lineNumber());
                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }

            // 변경점 1) WebView 내부 지오로케이션 프롬프트는 무조건 허용
            // (앱 권한은 onCreate에서 선행 확보)
            @Override
            public void onGeolocationPermissionsShowPrompt(
                    String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    view.evaluateJavascript("if(window.map){try{map.relayout();}catch(e){}}", null);
                }
            }
        });

        // 네트워크/타일 로드 에러 추적
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                Log.e(TAG, "onReceivedError: " + (req != null ? req.getUrl() : "null")
                        + " / " + (err != null ? err.getDescription() : "null"));
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest req, WebResourceResponse resp) {
                Log.e(TAG, "onReceivedHttpError: " + (req != null ? req.getUrl() : "null")
                        + " / " + (resp != null ? resp.getStatusCode() : -1));
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                Uri u = (req != null) ? req.getUrl() : null;
                if (u != null) {
                    String host = u.getHost() == null ? "" : u.getHost();
                    if (host.contains("daumcdn.net") || host.contains("kakaocdn.net")) {
                        Log.d(TAG, "tile request: " + u);
                    }
                }
                return super.shouldInterceptRequest(view, req);
            }
        });

        // 개발 편의
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearSslPreferences();

        // 변경점 2) 페이지 로드는 "권한 확보 후" 진행
        requestLocationIfNeededOrLoad();
    }

    // FINE 권한 보유 여부
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // 권한 없으면 요청, 있으면 즉시 로드
    private void requestLocationIfNeededOrLoad() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQ_LOCATION
            );
        } else {
            webView.loadUrl(MAP_URL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            // 허용/거절과 무관하게 동작은 해야 하므로 최초 로드 수행
            webView.loadUrl(MAP_URL);
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
