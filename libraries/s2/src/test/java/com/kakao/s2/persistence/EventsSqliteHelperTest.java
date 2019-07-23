package com.kakao.s2.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kakao.test.common.KakaoTestCase;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLog;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang
 * Created by kevin.kang on 2016. 9. 7..
 */
public class EventsSqliteHelperTest extends KakaoTestCase {
    @Before
    public void setup() {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testIfTableIsCreatedCorrectly() {
        EventsSqliteHelper databaseHelper = new EventsSqliteHelper(RuntimeEnvironment.application);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        assertEquals(EventsSqliteHelper.DB_VERSION, database.getVersion());
        assertEquals(1, database.getAttachedDbs().size());

        Cursor cursor = database.rawQuery(String.format("SELECT * FROM sqlite_master WHERE type='table' AND name='%s'", EventsColumns.TABLE_NAME), null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                assertEquals(cursor.getString(4) + ";", String.format(EventsSqliteHelper.DATABASE_CREATE,
                        EventsColumns.TABLE_NAME, EventsColumns._ID, EventsColumns.TIMESTAMP,
                        EventsColumns.FROM, EventsColumns.TO, EventsColumns.ACTION,
                        EventsColumns.ADID_ENABLED, EventsColumns.PROPS));
                cursor.moveToNext();
            }
        }
        cursor.close();
    }
}
