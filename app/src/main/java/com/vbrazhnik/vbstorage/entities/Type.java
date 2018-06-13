package com.vbrazhnik.vbstorage.entities;

public enum Type {

    TEXT    (1),
    IMAGE   (2),
    WEB_PAGE(3),
    AUDIO   (4);

    private int code;

    Type(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
