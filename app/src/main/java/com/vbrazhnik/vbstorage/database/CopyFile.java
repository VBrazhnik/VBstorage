package com.vbrazhnik.vbstorage.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CopyFile extends AsyncTask<Void, Void, Void> {

    private String source;
    private String target;

    public CopyFile(String source, String target) {
        this.source = source;
        this.target = target;
    }

    @Override
    protected Void doInBackground(Void... params) {
        File sourceLocation = new File (source);
        File targetLocation = new File (DirectoryHelper.getImagesDirectory() + target);
        Bitmap bitmap = BitmapFactory.decodeFile(sourceLocation.getAbsolutePath());
        try {
            targetLocation.createNewFile();
            FileOutputStream outStream = new FileOutputStream(targetLocation);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
