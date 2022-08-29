package com.example.original.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.original.bean.PasswordInfo;

import java.util.ArrayList;
import java.util.List;

public class PasswordDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "mPassword.db";
    private static final int DB_VERSION = 1;
    private static PasswordDBHelper mHelper = null;
    private SQLiteDatabase mDB = null;
    public static final String TABLE_NAME = "password_info";

    private PasswordDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private PasswordDBHelper(Context context, int version) {
        super(context, DB_NAME, null, version);
    }

    public static PasswordDBHelper getInstance(Context context, int version) {
        if (version > 0 && mHelper == null) {
            mHelper = new PasswordDBHelper(context, version);
        } else if (mHelper == null) {
            mHelper = new PasswordDBHelper(context);
        }
        return mHelper;
    }

    public SQLiteDatabase openReadLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getReadableDatabase();
        }
        return mDB;
    }

    public SQLiteDatabase openWriteLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getWritableDatabase();
        }
        return mDB;
    }

    public void closeLink() {
        if (mDB != null && mDB.isOpen()) {
            mDB.close();
            mDB = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String drop_sql = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
        db.execSQL(drop_sql);
        String create_sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + "about VARCHAR(50) NOT NULL,"
                + "account VARCHAR(50) NOT NULL,"
                + "password VARCHAR(50) NOT NULL"
                + ");";
        db.execSQL(create_sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int delete(String condition) {
        return mDB.delete(TABLE_NAME, condition, null);
    }

    public int deleteAll() {
        return mDB.delete(TABLE_NAME, "1=1", null);
    }

    public long insert(PasswordInfo info) {
        List<PasswordInfo> infoList = new ArrayList<>();
        infoList.add(info);
        return insert(infoList);
    }

    public long insert(List<PasswordInfo> infoList) {
        long result = -1;
        for (int i = 0; i < infoList.size(); i++) {
            PasswordInfo passwordInfo = infoList.get(i);
            List<PasswordInfo> tempList = new ArrayList<>();
            if (passwordInfo.about != null && passwordInfo.about.length() > 0) {
                String condition = String.format("about='%s'", passwordInfo.about);
                tempList = query(condition);//找到所有about与当前传进来的passwordInfo.about相同的条目
                if (tempList.size() > 0) {
                    update(passwordInfo, condition);//讲这些about相同的条目更新
                    result = tempList.get(0).rowid;
                    continue;
                }
                //不存在about相同的条目则添加新条目
                ContentValues cv = new ContentValues();
                cv.put("about", passwordInfo.about);
                cv.put("account", passwordInfo.account);
                cv.put("password", passwordInfo.password);
                result = mDB.insert(TABLE_NAME, "", cv);
                if (result == -1) { // 添加成功则返回行号，添加失败则返回-1
                    return result;
                }
            }
        }
        return result;
    }

    public int update(PasswordInfo passwordInfo, String condition) {
        ContentValues cv = new ContentValues();
        cv.put("about", passwordInfo.about);
        cv.put("account", passwordInfo.account);
        cv.put("password", passwordInfo.password);
        Log.d("updatexx", "update: 数据库被更新");
        return mDB.update(TABLE_NAME, cv, condition, null);
    }

    public int update(PasswordInfo info) {
        Log.d("updataxx", "update: 已被执行");
        return update(info, "rowid=" + info.rowid);

    }

    public List<PasswordInfo> query(String condition) {
        String sql = String.format("select rowid,_id,about,account,password from %s where %s;",
                TABLE_NAME, condition);
        List<PasswordInfo> infoList = new ArrayList<>();
        Cursor cursor = mDB.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            PasswordInfo passwordInfo = new PasswordInfo();
            passwordInfo.rowid = cursor.getLong(0);
            passwordInfo.xuhao = cursor.getInt(1);
            passwordInfo.about = cursor.getString(2);
            passwordInfo.account = cursor.getString(3);
            passwordInfo.password = cursor.getString(4);
            infoList.add(passwordInfo);
        }
        cursor.close();

        return infoList;
    }

}
