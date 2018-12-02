package com.ttdn.apptrimvideo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "video_trimmer.db";
    private final static String TABLE_NAME = "tbl_video";
    private final static String ID = "_id";
    public final static String VIDEO_LINK = "video_link";
    private static int DB_VERSION = 1;

    private static final String CREATE_TABLE =
            "create table " + TABLE_NAME + " ("
                    + ID + " integer primary key autoincrement not null,"
                    + VIDEO_LINK + " text)";

    private SQLiteDatabase db;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    public void open() {
        try {
            db = getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            try {
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Cursor getAllVideoLink() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY _id DESC";
        return db.rawQuery(sql, null);
    }

    public int deleteVideoLink(String videoLink) {
        return db.delete(TABLE_NAME, "video_link=?", new String[]{videoLink});
    }

    public long insertVideoLink(ContentValues contentValues) {
        return db.insert(TABLE_NAME, null, contentValues);
    }
}
