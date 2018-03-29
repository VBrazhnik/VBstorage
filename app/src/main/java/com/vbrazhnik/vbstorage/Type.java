package com.vbrazhnik.vbstorage;

public enum Type {
    TEXT(1),
    IMAGE(2),
    WEBPAGE(3);

    private int code;

    Type(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
