package com.kakao.s2.persistence;

import com.kakao.s2.Event;
import com.kakao.test.common.KakaoTestCase;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLog;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.*;
/**
 * @author kevin.kang
 * Created by kevin.kang on 2016. 9. 7..
 */


public class EventsDataSourceTest extends KakaoTestCase {
    private EventsDataSource dataSource;
    private Event event;
    private Event complexEvent;

    @Before
    public void setup() {
        super.setup();
        dataSource = new EventsDataSource(RuntimeEnvironment.application);
        assertNotNull(dataSource);
        dataSource.open();

        // build a normal event object
        Map<String, Object> props = new HashMap<>();
        props.put("customkey1", "customvalue1");
        event = new Event.Builder().setFrom("adid").setTo("http://www.kakaocorp.com").setTimestamp(new Date().getTime()).setAction("click").setProps(props).build();


        // build an event object with nested props for test
        Map<String, Object> nestedProps = new HashMap<String, Object>();
        props.put("key1", "value1");
        props.put("key2", "value2");

        JSONObject jsonObject = new JSONObject();

        nestedProps.put("key1", "value1");
        nestedProps.put("key2", "value2");
        try {
            jsonObject.put("key3", "value3");
            jsonObject.put("key4", "value4");

            JSONObject nested = new JSONObject();
            nested.put("key5", "value5");

            jsonObject.put("nested", nested);
        } catch (JSONException e) {
            throw new IllegalArgumentException();
        }


        nestedProps.put("jsonobject", jsonObject);
        complexEvent = new Event.Builder().setFrom("adid").setTo("http://www.kakaocorp.com").setTimestamp(new Date().getTime()).setAction("click").setProps(nestedProps).build();
    }

    @After
    public void clean() {
        dataSource.close();
    }

    @Test
    public void testInsertEvent() {
        long insertId = dataSource.insertEvent(event);
        assertEquals(1, insertId);
    }

    @Test
    public void testInsertingNestedProps() {
        long insertId = dataSource.insertEvent(complexEvent);
        assertEquals(1, insertId);
    }

    @Test
    public void testDeleteAllEvents() {
        dataSource.insertEvent(event);

        int count = dataSource.deleteAllEvents();

        assertEquals(1, count);

        dataSource.insertEvent(event);
        dataSource.insertEvent(event);

        count = dataSource.deleteAllEvents();

        assertEquals(2, count);

    }

    @Test
    public void testGetAllEvents() {
        List<Event> events = dataSource.getAllEvents();
        assertThat(events, Matchers.<Event>empty());

        long insertId = dataSource.insertEvent(event);
        assertEquals(1, insertId);

        events = dataSource.getAllEvents();
        assertEquals(1, events.size());

        Event e = events.get(0);
        assertEquals(event.getTimestamp(), e.getTimestamp());
        assertEquals(event.getFrom(), e.getFrom());
        assertEquals(event.getTo(), e.getTo());
        assertEquals(event.getAction(), e.getAction());
        assertEquals(event.getAdidEnabled(), e.getAdidEnabled());

        assertTrue(e.getProps().entrySet().containsAll(event.getProps().entrySet()));
        assertEquals(event.getProps().keySet().size(), e.getProps().keySet().size());
    }

    @Test
    public void testGetAllEventsWithNestedProps() throws JSONException {
        long insertId = dataSource.insertEvent(complexEvent);
        assertEquals(1, insertId);

        List<Event> events = dataSource.getAllEvents();
        assertEquals(1, events.size());

        Event e = events.get(0);
        assertTrue(e.getProps().get("jsonobject") instanceof JSONObject);
        assertEquals("value1", e.getProps().get("key1"));
        assertEquals("value2", e.getProps().get("key2"));
        assertEquals("value3", ((JSONObject)e.getProps().get("jsonobject")).get("key3"));
        assertEquals("value4", ((JSONObject)e.getProps().get("jsonobject")).get("key4"));
        JSONObject nested = (JSONObject)((JSONObject) e.getProps().get("jsonobject")).get("nested");
        assertEquals("value5", nested.get("key5"));
    }

    @Test
    public void testGetAllMultipleEvents() {
        long insertId = dataSource.insertEvent(event);
        long insertId2 = dataSource.insertEvent(complexEvent);

        assertEquals(1, insertId);
        assertEquals(2, insertId2);

        List<Event> events = dataSource.getAllEvents();

        assertEquals(2, events.size());
    }
}