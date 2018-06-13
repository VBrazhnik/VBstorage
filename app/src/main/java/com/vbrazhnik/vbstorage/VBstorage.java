package com.vbrazhnik.vbstorage;

import android.app.Application;
import android.content.Context;

import com.vbrazhnik.vbstorage.database.DirectoryHelper;
import com.vbrazhnik.vbstorage.entities.DaoMaster;
import com.vbrazhnik.vbstorage.entities.DaoSession;

import org.greenrobot.greendao.database.Database;

public class VBstorage extends Application {

    private DaoSession daoSession;
    public static volatile Context CONTEXT;

    @Override
    public void onCreate() {
        super.onCreate();
        CONTEXT = getApplicationContext();
        DirectoryHelper.createDirectories();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "metadata-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
