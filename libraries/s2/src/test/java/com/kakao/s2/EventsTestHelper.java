package com.kakao.s2;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author kevin.kang
 * Created by kevin.kang on 2016. 8. 26..
 */

class EventsTestHelper {

    private static final String to = "http://www.kakaocorp.com";
    private static final String from = "adid";
    private static final String action = "click";

    static Event buildNormalEvent(final Map<String, Object> props) {
        return new Event.Builder().setTimestamp(new Date().getTime()).setTo(to).setAction(action)
                .setFrom(from).setProps(props).build();
    }

    static List<Event> buildLeafEvents() {
        LinkedList<Event> leafEvents =  new LinkedList<>();
        leafEvents.add(new Event.Builder().setTo(to).setAction(action).setTimestamp(new Date().getTime() + 1000).build());
        leafEvents.add(new Event.Builder().setTo(to).setAction(action).setTimestamp(new Date().getTime() + 2000).build());
        return leafEvents;
    }
}
