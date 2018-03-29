package com.vbrazhnik.vbstorage;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.add_text) ImageView addText;
    @BindView(R.id.add_img) ImageView addImage;
    @BindView(R.id.add_drawing) ImageView addDrawing;
    @BindView(R.id.main_list) RecyclerView recyclerView;
    @BindView(R.id.toolbar) Toolbar toolbar;
   // @BindView(R.id.header_menu_icon) ImageView change;

    NotesAdapter adapter;
    List<Note> notes = new ArrayList<>();
    RecyclerView.ItemDecoration dividerItemDecoration;
    long initialCount;
    int modifyPos = -1;

    final int PICK_DRAW_REQUEST = 1;
    final int PICK_IMAGE_REQUEST = 2;

    boolean type = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        TagRadioGroup segmented = findViewById(R.id.segmented5);

        //segmented5.setOnCheckedChangeListener(this);
        segmented.clearCheck();
        segmented.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                /*RadioButton checkedRadioButton = group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked)
                {
                    Toast.makeText(MainActivity.this, checkedRadioButton.getText(), Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        addButton(segmented,"#all");
        addButton(segmented,"#birthdays");
        addButton(segmented,"#my_day");
        addButton(segmented,"#red");
        addButton(segmented,"#today");
        addButton(segmented,"#unit");

        segmented.check(segmented.getChildAt(0).getId());

        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.icon);

        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(this);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(gridLayoutManager);

        initialCount = Note.count(Note.class);

        if (savedInstanceState != null)
            modifyPos = savedInstanceState.getInt("modify");


        if (initialCount >= 0) {

            notes = Note.listAll(Note.class);

            adapter = new NotesAdapter(MainActivity.this, notes);
            recyclerView.setAdapter(adapter);
            dividerItemDecoration = new LinearDecoration(8);
            recyclerView.addItemDecoration(dividerItemDecoration);
            if (notes.isEmpty())
                Snackbar.make(recyclerView, "No notes added.", Snackbar.LENGTH_LONG).show();

        }

        addText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddNoteActivity.class);
                startActivity(i);
            }
        });



        // Handling swipe to delete
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //Remove swiped item from list and notify the RecyclerView

                final int position = viewHolder.getAdapterPosition();
                final Note note = notes.get(viewHolder.getAdapterPosition());
                notes.remove(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(position);

                note.delete();
                initialCount -= 1;

                Snackbar.make(recyclerView, "Note deleted", Snackbar.LENGTH_SHORT)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                note.save();
                                notes.add(position, note);
                                adapter.notifyItemInserted(position);
                                initialCount += 1;

                            }
                        })
                        .show();
            }

        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        if (adapter != null) {
            adapter.SetOnItemClickListener(new NotesAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {

                    Log.d("Main", "click");

                    Intent i = new Intent(MainActivity.this, AddNoteActivity.class);
                    i.putExtra("is_editing", true);
                    i.putExtra("type", notes.get(position).getType());
                    i.putExtra("title", notes.get(position).getTitle());
                    i.putExtra("text", notes.get(position).getText());
                    i.putExtra("time", notes.get(position).getTime());
                    i.putExtra("image_path", notes.get(position).getPhotoPath());
                    modifyPos = position;

                    startActivity(i);
                }
            });
        }

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
            }
        });

        addDrawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MainActivity1.class);
                startActivityForResult(i, PICK_DRAW_REQUEST);
            }
        });

    }


    private void addButton(TagRadioGroup group, String text) {
        RadioButton radioButton = (RadioButton) this.getLayoutInflater().inflate(R.layout.radio_button_item, null);
        radioButton.setText(text);


        //Typeface typeface = Typeface.createFromAsset(am,String.format(Locale.US, "fonts/%s", "abc.ttf"));


        Typeface custom_font = Typeface.createFromAsset(this.getApplicationContext().getAssets(), "font/Rubik/Rubik-Medium.ttf");


        radioButton.setTypeface(custom_font);

        group.addView(radioButton);
        group.updateBackground();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.layout_icon) {
            recyclerView.removeItemDecoration(dividerItemDecoration);
            if (type) {
                StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(gridLayoutManager);
                dividerItemDecoration = new StaggeredGridDecoration(8);
                //dividerItemDecoration = new EdgeDecorator(8, 1);
            }
            else
            {
                LinearLayoutManager gridLayoutManager = new LinearLayoutManager(getApplicationContext());
                gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(gridLayoutManager);
                dividerItemDecoration = new LinearDecoration(8);
            }
            recyclerView.addItemDecoration(dividerItemDecoration);
            type = !type;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("modify", modifyPos);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        modifyPos = savedInstanceState.getInt("modify");
    }

    @Override
    protected void onResume() {
        super.onResume();

        final long newCount = Note.count(Note.class);

        if (newCount > initialCount) {
            // A note is added
            Log.d("Main", "Adding new note");

            // Just load the last added note (new)
            Note note = Note.last(Note.class);

            notes.add(note);
            adapter.notifyItemInserted((int) newCount);

            initialCount = newCount;
        }

        if (modifyPos != -1) {
            notes.set(modifyPos, Note.listAll(Note.class).get(modifyPos));
            adapter.notifyItemChanged(modifyPos);
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_IMAGE_REQUEST) {
            if (data != null) {
                Uri selectedImage = data.getData();
                Intent i = new Intent(MainActivity.this, AddNoteActivity.class);
                i.putExtra("image_path", getRealPathFromURI(getApplicationContext(), selectedImage));
                startActivity(i);
            }
        }
        else if (requestCode == PICK_DRAW_REQUEST)
        {
            if (data != null) {
                Intent i = new Intent(MainActivity.this, AddNoteActivity.class);
                i.putExtra("image_path", data.getStringExtra("path"));
                startActivity(i);
            }
        }
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
}
