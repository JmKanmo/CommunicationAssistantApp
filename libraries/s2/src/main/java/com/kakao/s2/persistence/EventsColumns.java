package com.kakao.s2.persistence;

import android.provider.BaseColumns;

import com.kakao.s2.Event;

/**
 * @author kevin.kang
 * Created by kevin.kang on 2016. 9. 7..
 */

final class EventsColumns implements BaseColumns {

    private EventsColumns() {
        throw new UnsupportedOperationException("This class can't be instantiated");
    }

    static final String TABLE_NAME = "events";
    static final String TIMESTAMP = Event.TIMESTAMP;
    static final String FROM = "from_field";
    static final String TO = "to_url";
    static final String ACTION = Event.ACTION;
    static final String PROPS = Event.PROPS;
    static final String ADID_ENABLED = Event.ADID_ENABLED;

    static final String[] ALL_COLUMNS = { TIMESTAMP, FROM, TO, ACTION, PROPS, ADID_ENABLED };
}
