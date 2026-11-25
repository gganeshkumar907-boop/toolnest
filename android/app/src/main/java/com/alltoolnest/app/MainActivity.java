package com.alltoolnest.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.OutputStream;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;

    private class AndroidPrintBridge {
        @JavascriptInterface
        public void printHtml(String html) {
            runOnUiThread(() -> {
                WebView printView = new WebView(MainActivity.this);
                printView.setWebViewClient(new WebViewClient() {
                    @Override public void onPageFinished(WebView view, String url) {
                        PrintManager manager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                        if (manager != null) {
                            manager.print("AllToolNest document",
                                view.createPrintDocumentAdapter("AllToolNest"),
                                new PrintAttributes.Builder().build());
                        }
                    }
                });
                printView.loadDataWithBaseURL("file:///android_asset/public/", html,
                    "text/html", "UTF-8", null);
            });
        }
    }

    private class AndroidDownloadBridge {
        @JavascriptInterface
        public String saveImage(String dataUrl) {
            try {
                int comma = dataUrl.indexOf(',');
                if (comma < 0) return "";
                byte[] bytes = Base64.decode(dataUrl.substring(comma + 1), Base64.DEFAULT);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME,
                    "AllToolNest-photo-" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                if (Build.VERSION.SDK_INT >= 29) {
                    values.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/AllToolNest");
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);
                }
                Uri uri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) return "";
                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    if (out == null) return "";
                    out.write(bytes);
                }
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues done = new ContentValues();
                    done.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(uri, done, null, null);
                }
                return "<p>Photo saved in Pictures/AllToolNest.</p>";
            } catch (Exception e) {
                return "";
            }
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(8, 13, 37));
        getWindow().setNavigationBarColor(Color.rgb(8, 13, 37));
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setSupportMultipleWindows(false);
        webView.addJavascriptInterface(new AndroidPrintBridge(), "AndroidPrint");
        webView.addJavascriptInterface(new AndroidDownloadBridge(), "AndroidDownload");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !"file".equals(request.getUrl().getScheme());
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view,
                    ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                try {
                    startActivityForResult(params.createIntent(), FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception e) {
                    filePathCallback = null;
                    callback.onReceiveValue(null);
                    return false;
                }
            }
        });
        setContentView(webView);
        webView.loadUrl("file:///android_asset/public/index.html");
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] results = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null) {
            webView.evaluateJavascript(
                "(function(){var s=document.querySelector('.tool-screen.active');if(s){goHome();return 'tool';}return 'home';})()",
                value -> { if ("\\"home\\"".equals(value)) finish(); });
        } else super.onBackPressed();
    }
}