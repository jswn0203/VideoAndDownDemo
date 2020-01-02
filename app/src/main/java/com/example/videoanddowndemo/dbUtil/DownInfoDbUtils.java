package com.example.videoanddowndemo.dbUtil;

import com.example.videoanddowndemo.dao.DownInfoDao;
import com.example.videoanddowndemo.down.DownInfo;

import java.util.ArrayList;

public class DownInfoDbUtils {
    private DownInfoDao mDao;

    private static DownInfoDbUtils sInstance;

    public static DownInfoDbUtils getInstance() {
        if (sInstance == null) {
            sInstance = new DownInfoDbUtils();
        }
        return sInstance;
    }

    private DownInfoDbUtils() {
        if (mDao == null) {
            mDao = DatabaseManager.getInstance().getDaoSession().getDownInfoDao();
        }
    }

    public ArrayList<DownInfo> getAllFile(String userId) {
        ArrayList<DownInfo> data = (ArrayList<DownInfo>) mDao.queryBuilder().where(DownInfoDao.Properties.MUserId.eq(userId)).list();
        return data;
    }

    public void add(DownInfo info) {
        mDao.insertOrReplace(info);
    }

    public void up(DownInfo info) {
        mDao.update(info);
    }

    public void delete(DownInfo info) {
        mDao.delete(info);
    }

    public void deleteAll() {
        mDao.deleteAll();
    }

    public void deleteUserAll(String userId) {
        mDao.queryBuilder().where(DownInfoDao.Properties.MUserId.eq(userId))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }


    public DownInfo query(String userId, String fileName, String url) {
        return mDao.queryBuilder()
                .where(DownInfoDao.Properties.MUserId.eq(userId)
                        , DownInfoDao.Properties.FileName.eq(fileName)
                        , DownInfoDao.Properties.MUrlTag.eq(url))
                .unique();
    }
}
