package com.vbrazhnik.vbstorage.viewer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.vbrazhnik.vbstorage.R;
import com.vbrazhnik.vbstorage.VBstorage;
import com.vbrazhnik.vbstorage.add.ItemActivity;
import com.vbrazhnik.vbstorage.audio.AudioView;
import com.vbrazhnik.vbstorage.browser.WEBpageView;
import com.vbrazhnik.vbstorage.database.DirectoryHelper;
import com.vbrazhnik.vbstorage.entities.DaoSession;
import com.vbrazhnik.vbstorage.entities.Item;
import com.vbrazhnik.vbstorage.entities.ItemDao;
import com.vbrazhnik.vbstorage.entities.ItemToTag;
import com.vbrazhnik.vbstorage.entities.ItemToTagDao;
import com.vbrazhnik.vbstorage.entities.Type;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TrashActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)     Toolbar toolbar;
    @BindView(R.id.main_list)   RecyclerView recyclerView;

    private ItemsAdapter adapter;
    private List<Item> items;
    private ItemsAdapter.OnItemClickListener listener;

    SharedPreferences sharedPreferences;

    final String TWO_COLUMNS = "two_columns";

    private final int MEMORY_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trash);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.trash);
        toolbar.setNavigationIcon(R.drawable.toolbar_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        sharedPreferences = getSharedPreferences("VBstorage", MODE_PRIVATE);
        boolean type = sharedPreferences.getBoolean(TWO_COLUMNS, true);

        if (!type)
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        else
        {
            LinearLayoutManager gridLayoutManager = new LinearLayoutManager(getApplicationContext());
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(gridLayoutManager);
        }

        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        listener = new ItemsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Item item = items.get(position);
                Intent intent;

                if (item.getType() == Type.WEB_PAGE.getCode())
                    intent = new Intent(TrashActivity.this, WEBpageView.class);
                else if (item.getType() == Type.AUDIO.getCode())
                    intent = new Intent(TrashActivity.this, AudioView.class);
                else
                    intent = new Intent(TrashActivity.this, ItemActivity.class);

                intent.putExtra("id", items.get(position).getId());
                startActivity(intent);
            }
        };

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                final int position = viewHolder.getAdapterPosition();
                final Item item = items.get(viewHolder.getAdapterPosition());
                items.remove(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(position);

                item.setDeleted(false);
                item.update();

                Snackbar snackbar = Snackbar.make(recyclerView, R.string.restored, Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.WHITE);
                snackbar.getView().setBackgroundColor(Color.parseColor("#AAAAAA"));
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trash_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clean_icon)
        {
            for (Item i: items) {
                DirectoryHelper.deleteFile(i);
                deleteTags(i);
                i.delete();
            }

            final int size = items.size();
            items.clear();
            adapter.notifyItemRangeRemoved(0, size);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(TrashActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MEMORY_PERMISSION);
            }
        }

        items = getAppDaoSession().getItemDao().queryBuilder()
                .where(ItemDao.Properties.Deleted.eq(true))
                .list();
        adapter = new ItemsAdapter(TrashActivity.this, items);
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

    private void deleteTags(Item item) {
        ItemToTagDao itemToTagDao = getAppDaoSession().getItemToTagDao();
        List<ItemToTag> forDelete = itemToTagDao.queryBuilder()
                .where(ItemToTagDao.Properties.IdItem.eq(item.getId())).list();
        itemToTagDao.deleteInTx(forDelete);
    }
}
