package com.kakao.s2.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kakao.s2.Event;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author kevin.kang
 * Created by kevin.kang on 2016. 9. 7..
 */

public class EventsDataSource {
    private SQLiteDatabase database;
    private final EventsSqliteHelper databaseHelper;

    public EventsDataSource(Context context) {
        databaseHelper = new EventsSqliteHelper(context);
    }

    public void open() throws SQLException {
        if (database == null || !database.isOpen()) {
            database = databaseHelper.getWritableDatabase();
        }
    }

    public void close() {
        databaseHelper.close();
    }

    public long insertEvent(Event event) {
        ContentValues values = new ContentValues();
        values.put(Event.TIMESTAMP, event.getTimestamp());
        values.put(EventsColumns.FROM, event.getFrom());
        values.put(EventsColumns.TO, event.getTo());
        values.put(EventsColumns.ACTION, event.getAction());
        values.put(Event.ADID_ENABLED, event.getAdidEnabled());

        try {
            values.put(Event.PROPS, event.propsToJson().toString());
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "There was an error translating event's props into json object.");
        }
        return database.insert(EventsColumns.TABLE_NAME, null, values);
    }

    public int deleteAllEvents() {
        return database.delete(EventsSqliteHelper.TABLE_EVENTS, "1", null);
    }

    /**
     * 앱 구동 시에 Sqlite에 저장되어 있던 이벤트들을 리스트 형태로 반환해준다.
     * @return Sqlite에 저장되어 있는 이벤트 리스트
     */
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<Event>();
        Cursor cursor = database.query(EventsColumns.TABLE_NAME, EventsColumns.ALL_COLUMNS, null, null, null, null, null);

        if (cursor == null) {
            return events;
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Map<String, Object> props = new HashMap<String, Object>();

            try {
                JSONObject propsJson = new JSONObject(cursor.getString(4));
                Iterator<String> iter = propsJson.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    try {
                        props.put(key, new JSONObject(propsJson.get(key).toString()));
                    } catch (JSONException e) {
                        props.put(key, propsJson.get(key));
                    }

                }
            } catch (JSONException e) {
                Log.e(this.getClass().getName(), e.toString());
            }

            try {
                Event event = new Event.Builder().setTimestamp(cursor.getLong(0)).setFrom(cursor.getString(1))
                        .setTo(cursor.getString(2)).setAction(cursor.getString(3)).setProps(props).build();
                events.add(event);
            } catch (KakaoException e) {
                Logger.e("There was an error fetching an event from sqlite.");
            }

            cursor.moveToNext();
        }

        cursor.close();
        return events;
    }
}
