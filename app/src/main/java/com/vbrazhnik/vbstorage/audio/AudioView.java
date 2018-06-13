package com.vbrazhnik.vbstorage.audio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vbrazhnik.vbstorage.R;
import com.vbrazhnik.vbstorage.entities.ItemToTag;
import com.vbrazhnik.vbstorage.entities.ItemToTagDao;
import com.vbrazhnik.vbstorage.tag.TagLayout;
import com.vbrazhnik.vbstorage.Util;
import com.vbrazhnik.vbstorage.VBstorage;
import com.vbrazhnik.vbstorage.database.DirectoryHelper;
import com.vbrazhnik.vbstorage.tag.EditTagsActivity;
import com.vbrazhnik.vbstorage.entities.DaoSession;
import com.vbrazhnik.vbstorage.entities.Item;
import com.vbrazhnik.vbstorage.entities.Tag;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AudioView extends AppCompatActivity
        implements MediaPlayer.OnCompletionListener {

    @BindView(R.id.toolbar)     Toolbar toolbar;
    @BindView(R.id.status)      TextView statusView;
    @BindView(R.id.timer)       TextView timerView;
    @BindView(R.id.play)        ImageButton playView;
    @BindView(R.id.item_title)  EditText titlePlace;
    @BindView(R.id.item_text)   EditText textPlace;
    @BindView(R.id.checkboxes)  TagLayout checkboxes;

    private String filePath;

    private MediaPlayer player;
    private Timer       timer;

    private int playerSecondsElapsed;

    private Item item;

    private final int MEMORY_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_view);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.toolbar_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                onBackPressed();
            }
        });

        long id = getIntent().getLongExtra("id", -1);
        item = getAppDaoSession().getItemDao().load(id);
        titlePlace.setText(item.getTitle());
        textPlace.setText(item.getText());

        checkboxes.setChecked(item.getTags());

        filePath = item.getAttachPath();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        checkboxes.update(getAppDaoSession().getTagDao().queryBuilder().list());
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(AudioView.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MEMORY_PERMISSION);
            }
        }

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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_CANCELED);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.tags_icon) {
            Intent i = new Intent(AudioView.this, EditTagsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopPlaying();
    }

    @Override
    public void onBackPressed() {
        item.setTitle(titlePlace.getText().toString());
        item.setText(textPlace.getText().toString());
        item.setTime(System.currentTimeMillis());
        item.setTags(checkboxes.getCheckedTags());
        item.update();
        deleteTags(item);

        for (Tag tag: item.getTags()) {
            ItemToTag save = new ItemToTag(null, item.getId(), tag.getId());
            getAppDaoSession().getItemToTagDao().insert(save);
        }
        stopPlaying();
        finish();
    }

    public void togglePlaying(View v){
        Util.wait(100, new Runnable() {
            @Override
            public void run() {
                if(isPlaying())
                    stopPlaying();
                else
                    startPlaying();
            }
        });
    }

    private void startPlaying(){
        try {
            player = new MediaPlayer();
            player.setDataSource(DirectoryHelper.getAudioDirectory() + filePath);
            player.prepare();
            player.start();

            player.setOnCompletionListener(AudioView.this);

            timerView.setText(R.string.start_time);
            statusView.setText(R.string.player_playing);
            statusView.setVisibility(View.VISIBLE);
            playView.setImageResource(R.drawable.player_stop);

            playerSecondsElapsed = 0;
            startTimer();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void stopPlaying(){
        statusView.setText("");
        statusView.setVisibility(View.INVISIBLE);
        playView.setImageResource(R.drawable.player_play);

        if(player != null){
            try {
                player.stop();
                player.reset();
            } catch (Exception e){ }
        }

        stopTimer();
    }

    private boolean isPlaying(){
        try {
            return player != null && player.isPlaying();
        } catch (Exception e){
            return false;
        }
    }

    private void startTimer(){
        stopTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    private void stopTimer(){
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private void updateTimer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isPlaying()){
                    playerSecondsElapsed++;
                    timerView.setText(Util.formatSeconds(playerSecondsElapsed));
                }
            }
        });
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
