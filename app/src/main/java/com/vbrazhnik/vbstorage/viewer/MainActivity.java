package com.vbrazhnik.vbstorage.viewer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.vbrazhnik.vbstorage.audio.AudioView;
import com.vbrazhnik.vbstorage.browser.BrowserActivity;
import com.vbrazhnik.vbstorage.entities.ItemDao;
import com.vbrazhnik.vbstorage.entities.ItemToTag;
import com.vbrazhnik.vbstorage.entities.ItemToTagDao;
import com.vbrazhnik.vbstorage.tag.EditTagsActivity;
import com.vbrazhnik.vbstorage.tag.TagLayout;
import com.vbrazhnik.vbstorage.VBstorage;
import com.vbrazhnik.vbstorage.add.ItemActivity;
import com.vbrazhnik.vbstorage.audio.AudioRecorderActivity;
import com.vbrazhnik.vbstorage.SettingsDialog;
import com.vbrazhnik.vbstorage.browser.WEBpageView;
import com.vbrazhnik.vbstorage.database.CopyFile;
import com.vbrazhnik.vbstorage.database.DirectoryHelper;
import com.vbrazhnik.vbstorage.entities.DaoSession;
import com.vbrazhnik.vbstorage.entities.Item;
import com.vbrazhnik.vbstorage.entities.Tag;
import com.vbrazhnik.vbstorage.entities.Type;
import com.vbrazhnik.vbstorage.drawing.DrawingActivity;
import com.vbrazhnik.vbstorage.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)     Toolbar toolbar;
    @BindView(R.id.main_list)   RecyclerView recyclerView;
    @BindView(R.id.add_text)    ImageView addText;
    @BindView(R.id.add_image)   ImageView addImage;
    @BindView(R.id.add_drawing) ImageView addDrawing;
    @BindView(R.id.add_webpage) ImageView addWebPage;
    @BindView(R.id.add_audio)   ImageView addAudio;
    @BindView(R.id.checkboxes)  TagLayout checkboxes;

    private ItemsAdapter adapter;
    private List<Item> items;
    private ItemsAdapter.OnItemClickListener listener;

    private String url_camera;

    private final int DRAWING_REQUEST   = 1;
    private final int IMAGE_REQUEST     = 2;
    private final int PHOTO_REQUEST     = 3;

    private final int CAMERA_PERMISSION = 4;
    private final int MEMORY_PERMISSION = 5;

    private boolean type;

    SharedPreferences sharedPreferences;

    final String TWO_COLUMNS = "two_columns";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.toolbar_icon);

        sharedPreferences = getSharedPreferences("VBstorage", MODE_PRIVATE);
        type = sharedPreferences.getBoolean(TWO_COLUMNS, true);

        if (!type) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        }
        else {
            LinearLayoutManager gridLayoutManager = new LinearLayoutManager(getApplicationContext());
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(gridLayoutManager);
        }

        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        listener = new ItemsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Item    item = items.get(position);
                Intent  intent;

                if (item.getType() == Type.WEB_PAGE.getCode())
                    intent = new Intent(MainActivity.this, WEBpageView.class);
                else if (item.getType() == Type.AUDIO.getCode())
                    intent = new Intent(MainActivity.this, AudioView.class);
                else
                    intent = new Intent(MainActivity.this, ItemActivity.class);

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

                item.setDeleted(true);
                item.update();

                Snackbar snackbar = Snackbar.make(recyclerView, R.string.moved_to_trash, Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.WHITE)
                        .setAction(R.string.view, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(MainActivity.this, TrashActivity.class);
                                startActivity(i);
                            }
                        });
                snackbar.getView().setBackgroundColor(Color.parseColor("#AAAAAA"));
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        initFooterListeners();

        checkboxes.setListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateData(checkboxes.getCheckedTags());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search_icon)
        {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.settings_icon)
        {
            final SettingsDialog fragment = new SettingsDialog();

            fragment.setViewListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (type) {
                        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                    }
                    else
                    {
                        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(getApplicationContext());
                        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(gridLayoutManager);
                    }
                    adapter = new ItemsAdapter(MainActivity.this, items);
                    adapter.SetOnItemClickListener(listener);

                    recyclerView.setAdapter(adapter);
                    type = !type;
                    SharedPreferences.Editor ed = sharedPreferences.edit();
                    ed.putBoolean(TWO_COLUMNS, type);
                    ed.apply();
                    fragment.dismiss();
                }
            });
            fragment.setHashtagListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, EditTagsActivity.class);
                    startActivity(intent);
                }
            });
            fragment.setTrashListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, TrashActivity.class);
                    startActivity(intent);
                }
            });
            FragmentManager fm = getSupportFragmentManager();
            fragment.show(fm, "Settings");
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makePhoto();
                }
                break;
            }
            case MEMORY_PERMISSION: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    finish();
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MEMORY_PERMISSION);
            }
        }

        List <Tag> tags = getAppDaoSession().getTagDao().queryBuilder().list();
        List <Tag> checked = checkboxes.getCheckedTags();
        checkboxes.setChecked(checked);
        checkboxes.update(tags);

        if (tags.isEmpty())
            checkboxes.setVisibility(View.GONE);
        else
            checkboxes.setVisibility(View.VISIBLE);

        updateData(checked);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == IMAGE_REQUEST) {
            if (data != null) {
                Uri selectedImage = data.getData();
                Intent i = new Intent(MainActivity.this, ItemActivity.class);
                String path = DirectoryHelper.getRealPathFromURI(getApplicationContext(), selectedImage);

                String image_path = DirectoryHelper.createUniqueFilename() + ".png";
                new CopyFile(path, image_path).execute();
                i.putExtra("image_path", image_path);
                i.putExtra("source_path", path);
                startActivity(i);
            }
        }
        else if (requestCode == DRAWING_REQUEST)
        {
            if (data != null) {
                Intent i = new Intent(MainActivity.this, ItemActivity.class);
                i.putExtra("image_path", data.getStringExtra("path"));
                startActivity(i);
            }
        }
        else if (requestCode == PHOTO_REQUEST)
        {
            if (resultCode == RESULT_OK) {
                Intent i = new Intent(MainActivity.this, ItemActivity.class);
                i.putExtra("image_path", url_camera);
                startActivity(i);
            }
        }
    }

    private DaoSession getAppDaoSession() {
        return ((VBstorage)getApplication()).getDaoSession();
    }

    private void initFooterListeners()
    {
        addText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ItemActivity.class);
                startActivity(i);
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final CharSequence[] options = { getString(R.string.take_photo), getString(R.string.choose_from_gallery)};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals(getString(R.string.take_photo))) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    makePhoto();
                                } else {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                                }
                            } else {
                                makePhoto();
                            }
                        }
                        else if (options[item].equals(getString(R.string.choose_from_gallery)))
                        {
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent, IMAGE_REQUEST);
                        }
                    }

                });
                builder.show();
            }
        });

        addDrawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, DrawingActivity.class);
                startActivityForResult(i, DRAWING_REQUEST);
            }
        });

        addWebPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, BrowserActivity.class);
                startActivity(i);
            }
        });

        addAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordAudio();
            }
        });
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_from_bottom);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    private void updateData(List <Tag> checked)
    {
        if (!checked.isEmpty()) {
            items = new ArrayList<>();

            for (Tag tag : checked) {
                List<ItemToTag> connector = getAppDaoSession().getItemToTagDao().queryBuilder().where(ItemToTagDao.Properties.IdTag.eq(tag.getId())).list();
                for (ItemToTag i : connector) {
                    Item item = getAppDaoSession().getItemDao().load(i.getIdItem());
                    if (!(item.getDeleted() || items.contains(item)))
                        items.add(item);
                }
            }
        }
        else
            items = getAppDaoSession().getItemDao().queryBuilder().where(ItemDao.Properties.Deleted.eq(false)).list();

        if (!type)
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        adapter = new ItemsAdapter(MainActivity.this, items);
        adapter.SetOnItemClickListener(listener);
        recyclerView.setAdapter(adapter);
        runLayoutAnimation(recyclerView);
    }

    private void makePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        url_camera = DirectoryHelper.createUniqueFilename() + ".jpg";
        File file = new File(DirectoryHelper.getImagesDirectory() + url_camera);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(MainActivity.this,
                getString(R.string.file_provider_authority), file));
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    private void recordAudio() {
        Intent i = new Intent(MainActivity.this, AudioRecorderActivity.class);
        startActivity(i);
    }

}
