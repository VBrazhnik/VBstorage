package com.vbrazhnik.vbstorage.browser;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.vbrazhnik.vbstorage.R;
import com.vbrazhnik.vbstorage.entities.Item;
import com.vbrazhnik.vbstorage.entities.ItemToTag;
import com.vbrazhnik.vbstorage.entities.ItemToTagDao;
import com.vbrazhnik.vbstorage.entities.Tag;
import com.vbrazhnik.vbstorage.tag.EditTagsActivity;
import com.vbrazhnik.vbstorage.tag.TagLayout;
import com.vbrazhnik.vbstorage.VBstorage;
import com.vbrazhnik.vbstorage.database.DirectoryHelper;
import com.vbrazhnik.vbstorage.entities.DaoSession;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WEBpageView extends AppCompatActivity {

    @BindView(R.id.toolbar)     Toolbar toolbar;
    @BindView(R.id.webview)     WebView webView;
    @BindView(R.id.checkboxes)  TagLayout checkboxes;

    private Item        item = null;
    private boolean     areTagsUpdated = true;

    private final int MEMORY_PERMISSION = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webpage);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.toolbar_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                onBackPressed();
            }
        });

        item = getAppDaoSession().getItemDao().load(getIntent().getLongExtra("id", -1));
        checkboxes.setChecked(item.getTags());

        toolbar.setTitle(item.getTitle());

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.loadUrl("file://" + DirectoryHelper.getWEBPagesDirectory() + item.getAttachPath());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(WEBpageView.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MEMORY_PERMISSION);
            }
        }

        if (areTagsUpdated) {
            areTagsUpdated = false;
            checkboxes.update(getAppDaoSession().getTagDao().queryBuilder().list());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.tags_icon) {
            checkboxes.setChecked(checkboxes.getCheckedTags());
            areTagsUpdated = true;
            Intent i = new Intent(WEBpageView.this, EditTagsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        item.setTags(checkboxes.getCheckedTags());
        item.update();
        deleteTags(item);

        for (Tag tag: item.getTags()) {
            ItemToTag save = new ItemToTag(null, item.getId(), tag.getId());
            getAppDaoSession().getItemToTagDao().insert(save);
        }

        finish();
    }

    private DaoSession getAppDaoSession() {
        return ((VBstorage)getApplication()).getDaoSession();
    }

    private void deleteTags(Item item) {
        ItemToTagDao itemToTagDao = getAppDaoSession().getItemToTagDao();
        List<ItemToTag> forDelete = itemToTagDao.queryBuilder()
                .where(ItemToTagDao.Properties.IdItem.eq(item.getId())).list();
        itemToTagDao.deleteInTx(forDelete);
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

}
