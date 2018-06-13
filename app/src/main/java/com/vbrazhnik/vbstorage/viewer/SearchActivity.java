package com.vbrazhnik.vbstorage.viewer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vbrazhnik.vbstorage.R;
import com.vbrazhnik.vbstorage.Util;
import com.vbrazhnik.vbstorage.VBstorage;
import com.vbrazhnik.vbstorage.add.ItemActivity;
import com.vbrazhnik.vbstorage.audio.AudioView;
import com.vbrazhnik.vbstorage.browser.WEBpageView;
import com.vbrazhnik.vbstorage.entities.DaoSession;
import com.vbrazhnik.vbstorage.entities.Item;
import com.vbrazhnik.vbstorage.entities.ItemDao;
import com.vbrazhnik.vbstorage.entities.Tag;
import com.vbrazhnik.vbstorage.entities.Type;
import com.vbrazhnik.vbstorage.tag.TagLayout;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity{

    @BindView(R.id.search_close)    ImageView searchClose;
    @BindView(R.id.search_query)    EditText searchQuery;
    @BindView(R.id.main_list)       RecyclerView recyclerView;
    @BindView(R.id.checkboxes)      TagLayout checkboxes;

    private ItemsAdapter adapter;
    private List<Item> items;
    private ItemsAdapter.OnItemClickListener listener;

    final String TWO_COLUMNS = "two_columns";
    SharedPreferences sharedPreferences;
    String query = null;

    private final int MEMORY_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        searchClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sharedPreferences = getSharedPreferences("VBstorage", MODE_PRIVATE);
        boolean type = sharedPreferences.getBoolean(TWO_COLUMNS, true);

        if (!type)
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        else {
            LinearLayoutManager gridLayoutManager = new LinearLayoutManager(getApplicationContext());
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(gridLayoutManager);
        }

        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        checkboxes.setListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                search();
            }
        });

        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag(1L,"text"));
        tags.add(new Tag(2L,"image"));
        tags.add(new Tag(3L,"webpage"));
        tags.add(new Tag(4L,"audio"));
        checkboxes.update(tags);

        listener = new ItemsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Item item = items.get(position);
                Intent intent;

                if (item.getType() == Type.WEB_PAGE.getCode())
                    intent = new Intent(SearchActivity.this, WEBpageView.class);
                else if (item.getType() == Type.AUDIO.getCode())
                    intent = new Intent(SearchActivity.this, AudioView.class);
                else
                    intent = new Intent(SearchActivity.this, ItemActivity.class);

                intent.putExtra("id", items.get(position).getId());
                startActivity(intent);
            }
        };

        if (!type)
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        searchQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_SEARCH)) {
                    search();
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(SearchActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MEMORY_PERMISSION);
            }
        }

        if (query != null) {
            items = getAppDaoSession().getItemDao().queryBuilder()
                    .whereOr(ItemDao.Properties.Title.like("%" + query + "%"),
                            ItemDao.Properties.Text.like("%" + query + "%"))
                    .where(ItemDao.Properties.Deleted.eq(false))
                    .list();
            adapter = new ItemsAdapter(SearchActivity.this, items);
            adapter.SetOnItemClickListener(listener);
            recyclerView.setAdapter(adapter);
        }
    }

    private void search() {
        Util.hideSoftKeyboard(recyclerView);
        query = searchQuery.getText().toString();
        QueryBuilder<Item> qb = getAppDaoSession().getItemDao().queryBuilder();

        qb.whereOr(ItemDao.Properties.Title.like("%" + query + "%"),
                ItemDao.Properties.Text.like("%" + query + "%"));
        qb.where(ItemDao.Properties.Deleted.eq(false));

        List<Tag> checked = checkboxes.getCheckedTags();
        if (checked.isEmpty())
            items = qb.list();
        else {
            items = new ArrayList<>();
            QueryBuilder<Item> subqb;

            for (Tag tag: checked) {
                subqb = qb;
                long id = tag.getId();
                if (id == 1L) {
                    subqb.where(ItemDao.Properties.Type.eq(Type.TEXT.getCode()));
                    items.addAll(subqb.list());
                }
                else if (id == 2L) {
                    subqb.where(ItemDao.Properties.Type.eq(Type.IMAGE.getCode()));
                    items.addAll(subqb.list());
                }
                else if (id == 3L) {
                    subqb.where(ItemDao.Properties.Type.eq(Type.WEB_PAGE.getCode()));
                    items.addAll(subqb.list());
                }
                else if (id == 4L) {
                    subqb.where(ItemDao.Properties.Type.eq(Type.AUDIO.getCode()));
                    items.addAll(subqb.list());
                }
            }
        }

        adapter = new ItemsAdapter(SearchActivity.this, items);
        adapter.SetOnItemClickListener(listener);
        recyclerView.setAdapter(adapter);
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

    private DaoSession getAppDaoSession() {
        return ((VBstorage)getApplication()).getDaoSession();
    }

}
