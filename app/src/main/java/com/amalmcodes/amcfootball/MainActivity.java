package com.amalmcodes.amcfootball;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.DownloadListener;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String GAME_URL = "https://amcfootball.netlify.app/";
    private static final int FILE_CHOOSER_CODE = 420;
    private WebView webView;
    private ValueCallback<Uri[]> fileChooserCallback;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(6,17,26));
        getWindow().setNavigationBarColor(Color.rgb(6,17,26));
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(6,17,26));
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setSafeBrowsingEnabled(true);
        webView.addJavascriptInterface(new JsonBackupBridge(), "AMCBackup");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileChooserCallback != null) fileChooserCallback.onReceiveValue(null);
                fileChooserCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, FILE_CHOOSER_CODE);
                    return true;
                } catch (ActivityNotFoundException e) {
                    fileChooserCallback = null;
                    return false;
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();
                if (host != null && (host.equals("amcfootball.netlify.app") || host.equals("themmdlyv.fun") || host.equals("www.themmdlyv.fun")) && "https".equals(uri.getScheme())) return false;
                if (request.isForMainFrame()) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) {}
                    return true;
                }
                return false;
            }
            @Override public void onPageFinished(WebView view, String url) {
                // Keep the user's JSON export available in the native APK.
                view.evaluateJavascript("(function(){if(window.__amcNativeSaveHook)return;window.__amcNativeSaveHook=true;document.addEventListener('click',function(e){var a=e.target.closest('a[download]');if(!a||!a.href.startsWith('blob:')||!String(a.download).toLowerCase().endsWith('.json'))return;e.preventDefault();fetch(a.href).then(function(r){return r.blob()}).then(function(b){if(b.size>5242880)throw Error('File too large');var reader=new FileReader();reader.onloadend=function(){var s=String(reader.result);window.AMCBackup.saveJson(s.substring(s.indexOf(',')+1),a.download)};reader.readAsDataURL(b)}).catch(function(e){console.error(e)});},true)})()", null);
            }
        });
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (!url.startsWith("https://")) return;
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception ignored) { }
        });
        if (savedInstanceState != null) webView.restoreState(savedInstanceState);
        else webView.loadUrl(GAME_URL);
    }

    private class JsonBackupBridge {
        @JavascriptInterface public void saveJson(String base64, String filename) {
            // This bridge writes only small JSON backups into public Downloads. No read or arbitrary file operations.
            if (base64 == null || base64.length() > 7_500_000) return;
            try {
                byte[] payload = Base64.decode(base64, Base64.DEFAULT);
                if (payload.length > 5_242_880) return;
                String name = filename == null ? "amc-football-save.json" : filename;
                name = name.replaceAll("[^A-Za-z0-9._-]", "_");
                if (!name.toLowerCase(Locale.ROOT).endsWith(".json")) name = "amc-football-save.json";
                if (name.length() > 90) name = "amc-football-save.json";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, name);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/");
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) return;
                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    if (out != null) out.write(payload);
                }
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Karyera Downloads qovluğunda saxlanıldı", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "JSON saxlama alınmadı", Toast.LENGTH_SHORT).show());
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == FILE_CHOOSER_CODE && fileChooserCallback != null) {
            Uri[] uris = (resultCode == Activity.RESULT_OK && data != null && data.getData()!=null) ? new Uri[]{data.getData()} : null;
            fileChooserCallback.onReceiveValue(uris);
            fileChooserCallback = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }
    @Override protected void onDestroy() {
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
