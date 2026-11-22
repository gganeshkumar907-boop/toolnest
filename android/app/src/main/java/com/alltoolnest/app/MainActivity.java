package com.alltoolnest.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private WebView webView;
    private View splashView;
    private ValueCallback<Uri[]> filePathCallback;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView caption(String icon, String label) {
        TextView v = new TextView(this);
        v.setText(icon + "\n" + label);
        v.setTextColor(Color.rgb(28, 39, 77));
        v.setTextSize(12);
        v.setGravity(Gravity.CENTER);
        v.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        v.setPadding(dp(4), dp(8), dp(4), dp(8));
        return v;
    }

    private View createSplash() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER);
        page.setBackgroundColor(Color.WHITE);

        View saffron = new View(this);
        saffron.setBackgroundColor(Color.rgb(255, 153, 51));
        page.addView(saffron, new LinearLayout.LayoutParams(-1, dp(14)));

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER);
        center.setPadding(dp(18), dp(12), dp(18), dp(12));
        page.addView(center, new LinearLayout.LayoutParams(-1, 0, 1));

        ImageView logo = new ImageView(this);
        logo.setImageResource(com.alltoolnest.app.R.mipmap.ic_launcher);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(176), dp(176));
        logoParams.bottomMargin = dp(12);
        center.addView(logo, logoParams);

        TextView name = new TextView(this);
        name.setText("AllToolNest");
        name.setTextColor(Color.rgb(20, 40, 100));
        name.setTextSize(24);
        name.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        name.setGravity(Gravity.CENTER);
        center.addView(name);

        TextView tagline = new TextView(this);
        tagline.setText("Simple Tools. Less Hassle.");
        tagline.setTextColor(Color.rgb(75, 83, 110));
        tagline.setTextSize(13);
        tagline.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(-1, -2);
        tagParams.bottomMargin = dp(18);
        center.addView(tagline, tagParams);

        LinearLayout row1 = new LinearLayout(this);
        row1.setGravity(Gravity.CENTER);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(caption("🧮", "Calculator"), new LinearLayout.LayoutParams(0, -2, 1));
        row1.addView(caption("📄", "PDF"), new LinearLayout.LayoutParams(0, -2, 1));
        row1.addView(caption("🖼️", "Photo"), new LinearLayout.LayoutParams(0, -2, 1));
        center.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setGravity(Gravity.CENTER);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.addView(caption("📝", "Resume"), new LinearLayout.LayoutParams(0, -2, 1));
        row2.addView(caption("✨", "Name Maker"), new LinearLayout.LayoutParams(0, -2, 1));
        row2.addView(caption("🤖", "AI Helper"), new LinearLayout.LayoutParams(0, -2, 1));
        center.addView(row2);

        View green = new View(this);
        green.setBackgroundColor(Color.rgb(19, 136, 8));
        page.addView(green, new LinearLayout.LayoutParams(-1, dp(14)));
        return page;
    }

    private class AndroidPrintBridge {
        @JavascriptInterface
        public void printPage() {
            runOnUiThread(() -> {
                if (webView == null) return;
                PrintManager manager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                if (manager == null) return;
                manager.print("AllToolNest document",
                    webView.createPrintDocumentAdapter("AllToolNest"),
                    new PrintAttributes.Builder().build());
            });
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFFFF9933);
        getWindow().setNavigationBarColor(0xFF138808);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setLoadsImagesAutomatically(true);
        webView.addJavascriptInterface(new AndroidPrintBridge(), "AndroidPrint");

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
            @Override public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(
                    "window.print=function(){if(window.AndroidPrint){AndroidPrint.printPage();}};",
                    null);
                if (splashView != null) splashView.setVisibility(View.GONE);
                getWindow().setStatusBarColor(0xFF10152F);
                getWindow().setNavigationBarColor(0xFF10152F);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view,
                    ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception e) {
                    filePathCallback = null;
                    callback.onReceiveValue(null);
                    return false;
                }
            }
        });

        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        splashView = createSplash();
        root.addView(splashView, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        webView.loadUrl("https://gganeshkumar907-boop.github.io/toolnest/");
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
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
