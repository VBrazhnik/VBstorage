package com.vbrazhnik.vbstorage.add;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.vbrazhnik.vbstorage.R;
import com.vbrazhnik.vbstorage.VBstorage;
import com.vbrazhnik.vbstorage.database.DirectoryHelper;
import com.vbrazhnik.vbstorage.drawing.DrawingActivity;
import com.vbrazhnik.vbstorage.entities.DaoSession;
import com.vbrazhnik.vbstorage.entities.Item;
import com.vbrazhnik.vbstorage.entities.ItemToTag;
import com.vbrazhnik.vbstorage.entities.ItemToTagDao;
import com.vbrazhnik.vbstorage.entities.Tag;
import com.vbrazhnik.vbstorage.entities.Type;
import com.vbrazhnik.vbstorage.tag.EditTagsActivity;
import com.vbrazhnik.vbstorage.tag.TagLayout;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ItemActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)     Toolbar toolbar;
    @BindView(R.id.item_title)  EditText titlePlace;
    @BindView(R.id.item_text)   EditText textPlace;
    @BindView(R.id.item_image)  ImageView imagePlace;
    @BindView(R.id.checkboxes)  TagLayout checkboxes;

    private Item item;
    private boolean areTagsUpdated = true;

    private final int MEMORY_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_item);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.toolbar_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        String sourcePath = getIntent().getStringExtra("source_path");
        long id = getIntent().getLongExtra("id", -1);

        if (id == -1) {
            item = new Item();
            item.setId(null);
            item.setImagePath(getIntent().getStringExtra("image_path"));
        } else {
            item = getAppDaoSession().getItemDao().load(id);
            checkboxes.setChecked(item.getTags());
        }

        titlePlace.setText(item.getTitle());
        textPlace.setText(item.getText());

        if (sourcePath != null || item.getImagePath() != null) {
            File imageFile = new File((sourcePath != null) ? sourcePath : (DirectoryHelper.getImagesDirectory() + item.getImagePath()));

            if (imageFile.exists()) {
                Uri e = Uri.fromFile(imageFile);
                Glide.with(getApplicationContext())
                        .load(e)
                        .asBitmap()
                        .placeholder(R.color.transparent)
                        .error(R.color.white)
                        .into(imagePlace);
            }
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
            Intent i = new Intent(ItemActivity.this, EditTagsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(ItemActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MEMORY_PERMISSION);
            }
        }

        if (areTagsUpdated) {
            areTagsUpdated = false;
            checkboxes.update(getAppDaoSession().getTagDao().queryBuilder().list());
        }
    }

    @Override
    public void onBackPressed() {
        item.setTitle(titlePlace.getText().toString());
        item.setText(textPlace.getText().toString());
        item.setTime(System.currentTimeMillis());

        if (item.getId() == null) {
            if (item.getImagePath() == null) {
                item.setType(Type.TEXT.getCode());
                if (item.getTitle().isEmpty() && item.getText().isEmpty()) {
                    this.finish();
                    return;
                }
            } else
                item.setType(Type.IMAGE.getCode());
            item.setId(getAppDaoSession().getItemDao().insert(item));
            item.setTags(checkboxes.getCheckedTags());
        } else {
            item.setTags(checkboxes.getCheckedTags());
            item.update();
            deleteTags(item);
        }

        for (Tag tag : item.getTags()) {
            ItemToTag save = new ItemToTag(null, item.getId(), tag.getId());
            getAppDaoSession().getItemToTagDao().insert(save);
        }

        finish();
    }

    private DaoSession getAppDaoSession() {
        return ((VBstorage) getApplication()).getDaoSession();
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
