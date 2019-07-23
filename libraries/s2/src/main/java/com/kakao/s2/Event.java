package com.kakao.s2;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.Utility;
import com.kakao.util.helper.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * S2 이벤트 모델.
 * @author kevin.kang
 * Created by kevin.kang on 2016. 8. 22..
 */
public final class Event implements Serializable, Parcelable {
    private static final long serialVersionUID = 1L;

    private Long timestamp;
    private String from;
    private String to;
    private String action;
    private HashMap<String, Object> props;
    private Boolean adidEnabled;

    public static final String TIMESTAMP = "timestamp";
    static final String FROM = "from";
    static final String ADID = "adid";
    static final String TO = "to";
    public static final String ACTION = "action";
    public static final String PROPS = "props";
    static final String EVENTS = "events";
    public static final String ADID_ENABLED = "adid_enabled";
    static final String AD_TRACKING_ENABLED = "ad_tracking_enabled";

    static final int MAX_BODY_SIZE = 90 * 1000;

    protected Event(Parcel in) {
        if (in.readByte() == 0) {
            timestamp = null;
        } else {
            timestamp = in.readLong();
        }
        from = in.readString();
        to = in.readString();
        action = in.readString();
        byte tmpAdidEnabled = in.readByte();
        adidEnabled = tmpAdidEnabled == 0 ? null : tmpAdidEnabled == 1;
        props = (HashMap<String, Object>) in.readSerializable();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    void setFrom(final String from) {
        this.from = from;
    }

    @SuppressWarnings("unused")
    void setAdidEnabled(boolean adidEnabled) {
        this.adidEnabled = adidEnabled;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getAction() {
        return action;
    }

    public Map<String, Object> getProps() {
        return props;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Boolean getAdidEnabled() {
        return adidEnabled;
    }

    Event(final String from, final Boolean adidEnabled) {
        this.from = from;
        this.adidEnabled = adidEnabled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (timestamp == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(timestamp);
        }
        parcel.writeString(from);
        parcel.writeString(to);
        parcel.writeString(action);
        parcel.writeByte((byte) (adidEnabled == null ? 0 : adidEnabled ? 1 : 2));
        parcel.writeSerializable(props);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;

        Event event = (Event) o;

        if (getTimestamp() != null ? !getTimestamp().equals(event.getTimestamp()) : event.getTimestamp() != null)
            return false;
        if (!TextUtils.equals(getFrom(), event.getFrom())) return false;
        if (!TextUtils.equals(getTo(), event.getTo())) return false;
        if (!TextUtils.equals(getAction(), event.getAction())) return false;
        if (getProps() != null ? !getProps().equals(event.getProps()) : event.getProps() != null)
            return false;
        return getAdidEnabled() != null ? getAdidEnabled().equals(event.getAdidEnabled()) : event.getAdidEnabled() == null;
    }

    /**
     * S2 이벤트 모델을 만들기 위한 Builder 클래스.
     */
    public static class Builder {
        private Long timestamp;
        private String from;
        private String to;
        private String action;
        private Map<String, Object> props;
        private Boolean adidEnabled;

        public Builder setTimestamp(final long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setFrom(final String from) {
            this.from = from;
            return this;
        }

        Builder setAdidEnabled(final boolean adidEnabled) {
            this.adidEnabled = adidEnabled;
            return this;
        }

        public Builder setTo(final String to) {
            this.to = to;
            return this;
        }

        public Builder setAction(final String action) {
            this.action = action;
            return this;
        }

        public Builder setProps(Map<String, Object> props) {
            if (props != null) {
                this.props = props;
            }
            return this;
        }

        public Event build() throws KakaoException {
            return new Event(this);
        }
    }

    private Event(Builder builder) throws KakaoException {
        this.timestamp = builder.timestamp;
        this.from = builder.from;
        this.to = builder.to;
        this.action = builder.action;
        if (builder.props != null) {
            this.props = new HashMap<>(builder.props);
        }
        this.adidEnabled = builder.adidEnabled;

        if (this.timestamp == null) {
            throw new KakaoException(KakaoException.ErrorType.ILLEGAL_ARGUMENT, "Event's timestamp field cannot be empty or null.");
        }
        if (Utility.isNullOrEmpty(this.to)) {
            throw new KakaoException(KakaoException.ErrorType.ILLEGAL_ARGUMENT, "Event's to field cannot be empty or null.");
        }
        String eventString = toString();
        if (eventString != null && eventString.length() > MAX_BODY_SIZE) {
            throw new KakaoException(KakaoException.ErrorType.ILLEGAL_ARGUMENT, "Event's length is over " + MAX_BODY_SIZE + " bytes.");
        }

        if (Utility.isNullOrEmpty(this.action)) {
            throw new KakaoException(KakaoException.ErrorType.ILLEGAL_ARGUMENT, "Event's action field cannot be empty or null.");
        }
    }

    public JSONObject propsToJson() throws JSONException {
        if (props == null) {
            return null;
        }
        JSONObject propsObject = new JSONObject();
        for (String key : this.props.keySet()) {
            propsObject.put(key, this.props.get(key).toString());
        }
        if (this.adidEnabled != null) {
            propsObject.put(ADID_ENABLED, this.adidEnabled ? 1 : 0);
        }
        return propsObject;
    }

    @Override
    public String toString() {
        JSONObject event = new JSONObject();
        try {
            event.put(TIMESTAMP, this.timestamp);
            event.put(FROM, this.from);
            event.put(TO, this.to);
            event.put(ACTION, this.action);
            event.put(PROPS, propsToJson());
            return event.toString();
        } catch (JSONException e) {
            Logger.e(e.toString());
            throw new KakaoException(KakaoException.ErrorType.ILLEGAL_ARGUMENT, "JSON parsing error for event.");
        }
    }
}
