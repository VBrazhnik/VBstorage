package com.vbrazhnik.vbstorage.browser;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vbrazhnik.vbstorage.R;
import com.vbrazhnik.vbstorage.Util;
import com.vbrazhnik.vbstorage.VBstorage;
import com.vbrazhnik.vbstorage.audio.AudioView;
import com.vbrazhnik.vbstorage.database.DirectoryHelper;
import com.vbrazhnik.vbstorage.entities.DaoSession;
import com.vbrazhnik.vbstorage.entities.Item;
import com.vbrazhnik.vbstorage.entities.ItemToTag;
import com.vbrazhnik.vbstorage.entities.Tag;
import com.vbrazhnik.vbstorage.entities.Type;
import com.vbrazhnik.vbstorage.tag.EditTagsActivity;
import com.vbrazhnik.vbstorage.tag.TagLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BrowserActivity extends AppCompatActivity {

    @BindView(R.id.webView)         WebView webView;
    @BindView(R.id.progressBar)     ProgressBar progressBar;
    @BindView(R.id.back)            ImageButton back;
    @BindView(R.id.next)            ImageButton next;
    @BindView(R.id.refresh)         ImageButton refresh;
    @BindView(R.id.save)            ImageButton save;
    @BindView(R.id.checkboxes)      TagLayout checkboxes;
    @BindView(R.id.browser_close)   ImageButton browserClose;
    @BindView(R.id.browser_hashtag) ImageButton browserHashtag;
    @BindView(R.id.url_address)     EditText urlAddress;

    private boolean areTagsUpdated = true;

    private final int MEMORY_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        ButterKnife.bind(this);

        initWebView();

        webView.loadUrl("http://www.google.com");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            forward();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.reload();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), R.string.saved, Toast.LENGTH_SHORT).show();

                Util.wait(2000, new Runnable() {
                    @Override
                    public void run() {
                        save.setImageResource(R.drawable.browser_save);
                    }
                });

                String url = DirectoryHelper.createUniqueFilename() + ".mht";
                webView.saveWebArchive(DirectoryHelper.getWEBPagesDirectory() + url);

                webView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(webView.getDrawingCache());
                webView.setDrawingCacheEnabled(false);

                String url_image = DirectoryHelper.createUniqueFilename() + ".png";
                File targetLocation = new File (DirectoryHelper.getImagesDirectory() + url_image);

                try {
                    targetLocation.createNewFile();
                    FileOutputStream outStream = new FileOutputStream(targetLocation);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Item item = new Item(null, Type.WEB_PAGE.getCode(), webView.getTitle(), null, url_image, url, System.currentTimeMillis());
                item.setTags(checkboxes.getCheckedTags());
                item.setId(getAppDaoSession().getItemDao().insert(item));

                for (Tag tag: item.getTags()) {
                    ItemToTag save = new ItemToTag(null, item.getId(), tag.getId());
                    getAppDaoSession().getItemToTagDao().insert(save);
                }
            }
        });

        browserClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        browserHashtag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                checkboxes.setChecked(checkboxes.getCheckedTags());
                areTagsUpdated = true;
                Intent i = new Intent(BrowserActivity.this, EditTagsActivity.class);
                startActivity(i);
            }
        });

        urlAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if  ((actionId == EditorInfo.IME_ACTION_GO)) {
                    String url = urlAddress.getText().toString();
                    if (BrowserUnit.isURL("http://" + url))
                        webView.loadUrl("http://" + url);
                    else if (BrowserUnit.isURL(url))
                        webView.loadUrl(url);
                    else
                        webView.loadUrl("http://www.google.com/toolbar_search?q=" + url);
                }

                return false;
            }
        });

        checkboxes.update(getAppDaoSession().getTagDao().queryBuilder().list());

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MEMORY_PERMISSION: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    finish();
                }
                break;
            }
        }
    }

    private void initWebView() {
        webView.setWebChromeClient(new MyWebChromeClient(this));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)) {
                    Intent intent = IntentUnit.getEmailIntent(MailTo.parse(url));
                    startActivity(intent);
                    view.reload();
                    return true;
                } else if (url.startsWith(BrowserUnit.URL_SCHEME_INTENT)) {
                    Intent intent;
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {}
                } else if (BrowserUnit.isURL("http://" + url))
                    webView.loadUrl("http://" + url);
                else if (BrowserUnit.isURL(url))
                    webView.loadUrl(url);
                else
                    webView.loadUrl("http://www.google.com/toolbar_search?q=" + url);

                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                invalidateOptionsMenu();
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.loadUrl("file:///android_asset/sample.html");
            }
        });

        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);
    }

    private void back() {
        if (webView.canGoBack()) {
            webView.goBack();
            WebBackForwardList webBackForwardList = webView.copyBackForwardList();
            if (webBackForwardList.getCurrentIndex() > 0)
                urlAddress.setText(webBackForwardList.getItemAtIndex(webBackForwardList.getCurrentIndex() - 1).getUrl());
        }
    }

    private void forward() {
        if (webView.canGoForward()) {
            webView.goForward();
            WebBackForwardList webBackForwardList = webView.copyBackForwardList();
            if (webBackForwardList.getCurrentIndex() > 0)
                urlAddress.setText(webBackForwardList.getItemAtIndex(webBackForwardList.getCurrentIndex() - 1).getUrl());
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        Context context;

        MyWebChromeClient(Context context) {
            super();
            this.context = context;
        }

        public void onProgressChanged(WebView view, int newProgress){

            progressBar.setProgress(newProgress);
            if (newProgress == 100){
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(BrowserActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MEMORY_PERMISSION);
            }
        }

        if (areTagsUpdated) {
            areTagsUpdated = false;
            checkboxes.update(getAppDaoSession().getTagDao().queryBuilder().list());
        }
    }

    private DaoSession getAppDaoSession() {
        return ((VBstorage)getApplication()).getDaoSession();
    }

}
