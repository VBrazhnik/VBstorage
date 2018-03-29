package com.vbrazhnik.vbstorage;

import com.orm.SugarRecord;

public class Note extends SugarRecord {

    private int     type;
    private String  title;
    private String  text;
    private String  photoPath;
    private long    time;

    public Note() {
    }

    public Note(int type, String title, String text, String photoPath, long time) {
        this.type = type;
        this.title = title;
        this.text = text;
        this.photoPath = photoPath;
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
