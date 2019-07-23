package com.kakao.s2.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author kevin.kang
 * Created by kevin.kang on 2016. 9. 6..
 */

class EventsSqliteHelper extends SQLiteOpenHelper {
    static final String TABLE_EVENTS = "events";
    static final String DB_NAME = "events.db";
    static final int DB_VERSION = 1;
    static final String DATABASE_CREATE = "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "%s INTEGER NOT NULL, " +
            "%s TEXT, " +
            "%s TEXT, " +
            "%s TEXT, " +
            "%s INTEGER, " +
            "%s TEXT);";

    EventsSqliteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(DATABASE_CREATE, EventsColumns.TABLE_NAME, EventsColumns._ID,
                EventsColumns.TIMESTAMP, EventsColumns.FROM, EventsColumns.TO, EventsColumns.ACTION,
                EventsColumns.ADID_ENABLED, EventsColumns.PROPS));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(this.getClass().getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data...");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }
}
