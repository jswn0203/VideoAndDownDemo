package com.example.videoanddowndemo.dbUtil;

import android.database.sqlite.SQLiteDatabase;

import com.example.videoanddowndemo.MyApplication;
import com.example.videoanddowndemo.dao.DaoMaster;
import com.example.videoanddowndemo.dao.DaoSession;

/**
 * Created by 极速蜗牛 on 2017/8/10 0010.
 */

public class DatabaseManager {
    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    private DatabaseManager() {
    }

    private static final class Holder {
        private static final DatabaseManager INSTANCE = new DatabaseManager();
    }

    public static DatabaseManager getInstance() {
        return Holder.INSTANCE;
    }

    private void initDao() {
        mHelper = new DaoMaster.DevOpenHelper(MyApplication.getAppCxt(), "downdemo.db", null);
        db = mHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }


    public final DaoSession getDaoSession() {
        if (mDaoSession == null) {
            initDao();
        }

        return mDaoSession;
    }


    public SQLiteDatabase getDb() {
        if (db == null) {
            initDao();
        }
        return db;
    }
}
