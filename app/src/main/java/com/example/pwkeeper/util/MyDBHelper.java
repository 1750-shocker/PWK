package com.example.pwkeeper.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.pwkeeper.bean.PasswordInfo;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class MyDBHelper extends OrmLiteSqliteOpenHelper {

    private static MyDBHelper myDBHelper = null;
    private Dao<PasswordInfo, Integer> pwInfoDao = null;
    private final static String DataBase_NAME = "ormlite.db";
    private final static int DataBase_VERSION = 1;


    public MyDBHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory,
                      int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, PasswordInfo.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i,
                          int i1) {

    }

    public static MyDBHelper getInstance(Context context) {
        if (myDBHelper == null) {
            myDBHelper = new MyDBHelper(context, DataBase_NAME, null, DataBase_VERSION);
        }
        return  myDBHelper;
    }

    public Dao<PasswordInfo, Integer> getPwInfoDao() {
        if (pwInfoDao == null) {
            try {
                pwInfoDao = getDao(PasswordInfo.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return pwInfoDao;
    }

    @Override
    public void close() {
        super.close();
        pwInfoDao = null;
    }
}
