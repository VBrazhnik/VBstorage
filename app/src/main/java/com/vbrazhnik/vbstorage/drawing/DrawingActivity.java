package com.vbrazhnik.vbstorage.drawing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vbrazhnik.vbstorage.R;
import com.vbrazhnik.vbstorage.colorpicker.ColorListener;
import com.vbrazhnik.vbstorage.colorpicker.ColorPalette;
import com.vbrazhnik.vbstorage.database.DirectoryHelper;
import com.vbrazhnik.vbstorage.viewer.TrashActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DrawingActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)         Toolbar toolbar;
    @BindView(R.id.color_layout)    LinearLayout layout;
    @BindView(R.id.draw)            CanvasView canvasView;

    private List<Integer>   colors = new ArrayList<>();
    private List<ImageView> buttons = new ArrayList<>();

    private ColorListener colorListener;

    private final int MEMORY_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.toolbar_done);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCanvas();
            }
        });

        colorListener = new ColorListener() {
            @Override
            public void OnColorClick(View v, int color) {
                canvasView.setPathColor(color);
                updateColorCircle();

                Drawable t = getDrawable(R.drawable.drawing_done);
                if (t != null) {
                    t.setTint(Color.WHITE);
                }
                ((ImageView)v).setImageDrawable(t);
            }
        };

        for (ColorPalette color : ColorPalette.values()) {
            createColorCircle(color.getColor());
        }

        setListeners();

        canvasView.setPathColor(colors.get(0));
        buttons.get(0).performClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.layout_icon) {
            canvasView.clear();
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
                ActivityCompat.requestPermissions(DrawingActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MEMORY_PERMISSION);
            }
        }
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

    private void saveCanvas() {
        Bitmap bitmap = canvasView.getBitmap();

        String fileName = DirectoryHelper.createUniqueFilename() + ".png";
        File saveFile = new File(DirectoryHelper.getImagesDirectory(), fileName);

        try {
            FileOutputStream FOS = new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, FOS);
            FOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.putExtra("path", fileName);
        setResult(1, intent);
        finish();
    }

    private ImageView createCircle(int color) {

        ImageView colorCircle = new ImageView(getApplicationContext());

        final int scale = (int) getApplicationContext().getResources().getDisplayMetrics().density;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 5 * scale, 15 * scale , 5 * scale);

        layout.addView(colorCircle, params);

        colorCircle.getLayoutParams().height = 35 * scale;
        colorCircle.getLayoutParams().width = 35 * scale;

        final GradientDrawable selected = new GradientDrawable();
        selected.setShape(GradientDrawable.OVAL);
        selected.setColor(color);

        colorCircle.setBackground(selected);

        return colorCircle;
    }

    private void updateColorCircle()
    {
        for (ImageView button : buttons)
            button.setImageDrawable(null);
    }

    private void createColorCircle(int color) {
        colors.add(color);
        buttons.add(createCircle(color));
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (colorListener != null) colorListener.OnColorClick(v, (int) v.getTag());
        }
    };

    private void setListeners() {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setTag(colors.get(i));
            buttons.get(i).setOnClickListener(listener);
        }
    }
}
