package com.vbrazhnik.vbstorage.audio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
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
import com.vbrazhnik.vbstorage.add.ItemActivity;
import com.vbrazhnik.vbstorage.tag.TagLayout;
import com.vbrazhnik.vbstorage.Util;
import com.vbrazhnik.vbstorage.VBstorage;
import com.vbrazhnik.vbstorage.database.DirectoryHelper;
import com.vbrazhnik.vbstorage.tag.EditTagsActivity;
import com.vbrazhnik.vbstorage.entities.DaoSession;
import com.vbrazhnik.vbstorage.entities.Item;
import com.vbrazhnik.vbstorage.entities.Type;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

public class AudioRecorderActivity extends AppCompatActivity
        implements PullTransport.OnAudioChunkPulledListener, MediaPlayer.OnCompletionListener {

    @BindView(R.id.toolbar)     Toolbar toolbar;
    @BindView(R.id.status)      TextView statusView;
    @BindView(R.id.timer)       TextView timerView;
    @BindView(R.id.restart)     ImageButton restartView;
    @BindView(R.id.record)      ImageButton recordView;
    @BindView(R.id.play)        ImageButton playView;
    @BindView(R.id.item_title)  EditText titlePlace;
    @BindView(R.id.item_text)   EditText textPlace;
    @BindView(R.id.checkboxes)  TagLayout checkboxes;

    private String filePath;

    private MediaPlayer player;
    private Recorder    recorder;
    private Timer       timer;

    private int recorderSecondsElapsed;
    private int playerSecondsElapsed;

    private boolean isRecording;

    private boolean canSave = false;

    private final int AUDIO_PERMISSION  = 1;
    private final int MEMORY_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.toolbar_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                onBackPressed();
            }
        });

        filePath = DirectoryHelper.createUniqueFilename() + ".wav";

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);

        checkboxes.update(getAppDaoSession().getTagDao().queryBuilder().list());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(AudioRecorderActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MEMORY_PERMISSION);
            }
            if (!(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(AudioRecorderActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION);
            }
        }

        checkboxes.update(getAppDaoSession().getTagDao().queryBuilder().list());
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MEMORY_PERMISSION:
            case AUDIO_PERMISSION: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    finish();
                }
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        restartRecording(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        restartRecording(null);
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
            Intent i = new Intent(AudioRecorderActivity.this, EditTagsActivity.class);
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
        int type;

        if (canSave) {
            isFinishing();
            stopRecording();
            type = Type.AUDIO.getCode();
        }
        else {
            type = Type.TEXT.getCode();
            filePath = null;
            if (titlePlace.getText().toString().isEmpty() && textPlace.getText().toString().isEmpty()) {
                finish();
                return;
            }
        }
        Item item = new Item(null, type, titlePlace.getText().toString(),
                textPlace.getText().toString(), null, filePath, System.currentTimeMillis());
        getAppDaoSession().getItemDao().insert(item);
        finish();
    }

    public void toggleRecording(View v) {
        stopPlaying();
        Util.wait(100, new Runnable() {
            @Override
            public void run() {
                if (isRecording)
                    pauseRecording();
                else
                    resumeRecording();
            }
        });
    }

    public void togglePlaying(View v){
        pauseRecording();
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

    public void restartRecording(View v){
        if(isRecording)
            stopRecording();
        else if(isPlaying())
            stopPlaying();
        canSave = false;
        statusView.setVisibility(View.INVISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.player_record);
        timerView.setText(R.string.start_time);
        recorderSecondsElapsed = 0;
        playerSecondsElapsed = 0;
    }

    private void resumeRecording() {
        isRecording = true;
        canSave = false;

        statusView.setText(R.string.player_recording);
        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.player_pause);
        playView.setImageResource(R.drawable.player_play);

        if(recorder == null) {
            timerView.setText(R.string.start_time);

            recorder = OmRecorder.wav(
                    new PullTransport.Default(new omrecorder.AudioSource.Smart(MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT, AudioFormat.CHANNEL_IN_MONO, 48000), AudioRecorderActivity.this),
                    new File(DirectoryHelper.getAudioDirectory() + filePath));
        }
        recorder.resumeRecording();

        startTimer();
    }

    private void pauseRecording() {
        isRecording = false;
        if(!isFinishing()) {
            canSave = true;
        }
        statusView.setText(R.string.player_paused);
        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.VISIBLE);
        playView.setVisibility(View.VISIBLE);
        recordView.setImageResource(R.drawable.player_record);
        playView.setImageResource(R.drawable.player_play);

        if (recorder != null) {
            recorder.pauseRecording();
        }

        stopTimer();
    }

    private void stopRecording(){

        recorderSecondsElapsed = 0;
        if (recorder != null) {
            recorder.stopRecording();
            recorder = null;
        }

        stopTimer();
    }

    private void startPlaying(){
        try {
            stopRecording();
            player = new MediaPlayer();
            player.setDataSource(DirectoryHelper.getAudioDirectory() + filePath);
            player.prepare();
            player.start();

            player.setOnCompletionListener(AudioRecorderActivity.this);

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
            return player != null && player.isPlaying() && !isRecording;
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
                if(isRecording) {
                    recorderSecondsElapsed++;
                    timerView.setText(Util.formatSeconds(recorderSecondsElapsed));
                } else if(isPlaying()){
                    playerSecondsElapsed++;
                    timerView.setText(Util.formatSeconds(playerSecondsElapsed));
                }
            }
        });
    }

    private DaoSession getAppDaoSession() {
        return ((VBstorage)getApplication()).getDaoSession();
    }

    @Override
    public void onAudioChunkPulled(AudioChunk audioChunk) {

    }

}
