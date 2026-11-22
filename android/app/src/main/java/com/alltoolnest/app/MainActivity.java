package com.alltoolnest.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;

public class MainActivity extends Activity {
    private WebView webView;
    private View splashView;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFF10152F);
        getWindow().setNavigationBarColor(0xFF10152F);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF10152F);

        webView = new WebView(this);
        webView.setBackgroundColor(0xFF10152F);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                if (splashView != null) splashView.setVisibility(View.VISIBLE);
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (splashView != null) splashView.setVisibility(View.GONE);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        root.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        FrameLayout splash = new FrameLayout(this);
        splash.setBackgroundColor(0xFF10152F);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        logo.setPadding(pad, pad, pad, pad);
        FrameLayout.LayoutParams logoParams = new FrameLayout.LayoutParams(
            (int) (300 * getResources().getDisplayMetrics().density),
            (int) (300 * getResources().getDisplayMetrics().density),
            Gravity.CENTER);
        splash.addView(logo, logoParams);
        splashView = splash;
        root.addView(splashView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);
        webView.loadUrl("https://gganeshkumar907-boop.github.io/toolnest/");
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}