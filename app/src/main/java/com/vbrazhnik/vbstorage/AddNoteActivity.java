package com.vbrazhnik.vbstorage;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddNoteActivity extends AppCompatActivity {

    @BindView(R.id.header_icon) ImageView icon;
    @BindView(R.id.addnote_title) EditText titlePlace;
    @BindView(R.id.addnote_text) EditText textPlace;
    @BindView(R.id.addnote_image) ImageView imagePlace;

    int     type = -1;
    String  title;
    String  text;
    String  imagePath;
    long    time;

    boolean editingNote;

    final int PICK_DRAW_REQUEST = 1;
    final int PICK_IMAGE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        ButterKnife.bind(this);
        icon.setImageResource(R.drawable.ic_add_24dp);

        editingNote = getIntent().getBooleanExtra("is_editing", false);

        if (editingNote) {
            type = getIntent().getIntExtra("type", Type.TEXT.getCode());
            title = getIntent().getStringExtra("title");
            text = getIntent().getStringExtra("text");
            time = getIntent().getLongExtra("time", 0);
            imagePath = getIntent().getStringExtra("image_path");

            titlePlace.setText(title);
            textPlace.setText(text);

            if (imagePath != null) {
                File imgFile = new File(imagePath);

                if (imgFile.exists()) {
                    Uri e = Uri.fromFile(imgFile);
                    Picasso.get()
                            .load(e)
                            .placeholder(R.color.transparent)
                            .error(R.drawable.ic_done_24dp)
                            .into(imagePlace);
                }
            }
        }

        imagePath = getIntent().getStringExtra("image_path");
        if (imagePath != null) {
            File imgFile = new File(imagePath);

            if (imgFile.exists()) {
                Uri e = Uri.fromFile(imgFile);
                Picasso.get()
                        .load(e)
                        .placeholder(R.color.transparent)
                        .error(R.drawable.ic_done_24dp)
                        .into(imagePlace);
            }
        }

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE_REQUEST) {

            Uri selectedImage = data.getData();
            imagePath = getRealPathFromURI(getApplicationContext(), selectedImage);
            File imgFile = new  File(imagePath);

            if(imgFile.exists()){
                Uri e = Uri.fromFile(imgFile);
                Picasso.get().
                        load(e)
                        .placeholder(R.color.transparent)
                        .error(R.drawable.ic_done_24dp)
                        .into(imagePlace);
            }
        }
        else if (requestCode == PICK_DRAW_REQUEST)
        {
            if (data != null) {
                imagePath = data.getStringExtra("drawing_path");
                File imgFile = new File(imagePath);

                if (imgFile.exists()) {
                    Uri e = Uri.fromFile(imgFile);
                    Picasso.get().
                            load(e)
                            .placeholder(R.color.transparent)
                            .error(R.drawable.ic_done_24dp)
                            .into(imagePlace);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                break;
        }
        return true;
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onBackPressed() {
        String newTitle = titlePlace.getText().toString();
        String newText = textPlace.getText().toString();
        long newTime = System.currentTimeMillis();

        if (!editingNote) {
            if (imagePath == null)
            {
                type = Type.TEXT.getCode();
            }
            else
            {
                type = Type.IMAGE.getCode();
            }
            Note note = new Note(type, newTitle, newText, imagePath, newTime);
            note.save();
        } else
        {
            List<Note> notes = Note.find(Note.class, "title = ?", title);
            if (notes.size() > 0) {

                Note note = notes.get(0);
                note.setType(type);
                note.setTitle(newTitle);
                note.setText(newText);
                note.setPhotoPath(imagePath);
                note.setTime(newTime);
                note.save();
            }
        }

        finish();
    }

}
