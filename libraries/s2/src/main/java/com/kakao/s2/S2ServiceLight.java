package com.kakao.s2;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.network.tasks.KakaoResultTask;
import com.kakao.network.tasks.KakaoTaskQueue;
import com.kakao.common.KakaoContextService;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.SystemInfo;
import com.kakao.util.helper.log.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 유저의 특정 이벤트를 수집하는 API
 * @author kevin.kang
 * Created by kevin.kang on 2016. 8. 22..
 */
class S2ServiceLight {

    @SuppressLint("StaticFieldLeak")
    protected static S2Service mInstance;

    final Context context;
    protected KakaoContextService contextService;
    /**
     * 유저가 보내는 이벤트들을 batching하기 위해 메모리에 저장하고 있는 이벤트 리스트.
     */
    private final List<Event> events;


    protected int mBatchSize = 10; // default batch size = 10

    protected ResponseCallback<EventsLogResponse> mPublishCallback;
    protected ResponseCallback<Integer> mAddEventCallback;

    protected final Object INSTANCE_LOCK = new Object();

    /**
     * S2Service 싱글턴 인스턴스를 반환하는 메소드.
     * @return S2Service 싱글턴 인스턴스
     */
    public static S2Service getInstance() {
        if (mInstance == null) {
            throw new KakaoException(KakaoException.ErrorType.MISS_CONFIGURATION, "You should call S2Service.init() first.");
        }
        return mInstance;
    }

    @SuppressWarnings("WeakerAccess")
    S2ServiceLight(Context context, KakaoContextService contextService) {
        this.context = context;
        this.contextService = contextService;
        events = Collections.synchronizedList(new ArrayList<Event>());
    }

    /**
     * S2 이벤트 API를 사용할 수 있도록 다양한 초기화를 한다.
     *
     * 1. ActivityLifecycleCallback을 등록하여 앱이 foreground에 있을 때만 주기적 배칭을 하도록 한다.
     * 2. 인자로 받은 Application의 getApplicationContext()를 호출하여 추후에 사용할 수 있도록 저장한다.
     * 3. SystemInfo.initialize()를 호출하여 API call을 할 때 헤더 값이 잘 채워지도록 한다.
     *
     * @param application 커스텀 Application 클래스에서 S2Service.init()을 통해 직접 넘겨준 application 객체
     */
    public synchronized static void init(Application application, int batchSize) {
        if (application == null) {
            throw new KakaoException("You should provide Application instance to initialize S2 Service.");
        }

        KakaoContextService.getInstance().initialize(application);
        if (mInstance == null) {
            mInstance = new S2Service(application.getApplicationContext(), KakaoContextService.getInstance());
        }
        if (batchSize >= 0) {
            mInstance.setBatchSize(batchSize);
        }

        application.registerActivityLifecycleCallbacks(new ActivityLifecycleHandler());
    }

    /**
     * 이벤트 배치 사이즈를 조정한다. 이벤트 리스트의 길이가 이 배치 사이즈에 도달하면 서버에 이벤트들을 전송한다.
     * @param batchSize 배치된 이벤트들을 S2러 잔송할 리스트 사이즈 리밋
     */
    @SuppressWarnings("WeakerAccess")
    public void setBatchSize(int batchSize) {
        mBatchSize = batchSize;
    }

    /**
     * 이벤트를 보낼 때 default로 사용될 ResponseCallback
     * @param callback  ResponseCallback
     */
    public void setPublishCallback(final ResponseCallback<EventsLogResponse> callback) {
        mPublishCallback = callback;
    }

    /**
     * 이벤트를 batch에 추가할 때 default로 사용될 ResponseCallback
     * @param callback  ResponseCallback
     */
    public void setAddEventCallback(final ResponseCallback<Integer> callback) {
        mAddEventCallback = callback;
    }

    /**
     * 하나의 이벤트를 이벤트 batch에 추가한다.
     * @param event batch할 이벤트
     */
    @SuppressWarnings("unused") // will be used by sample app or third party apps.
    public void addEvent(final Event event) {
        addEvent(event, mAddEventCallback);
    }

    /**
     * 하나의 이벤트를 이벤트 batch에 추가하고 파라미터로 제공된 callback을 실행한다.
     * @param event batch할 이벤트
     * @param callback 이벤트 배치 요청 결과에 대한 handler
     */
    @SuppressWarnings("unused") // will be used by sample app or third party apps.
    public void addEvent(final Event event, final ResponseCallback<Integer> callback) {
        int eventsSize;
        try {
            if (event.getFrom() == null) {
                throw new KakaoException(KakaoException.ErrorType.ILLEGAL_ARGUMENT, "Event's from field cannot be null.");
            }
            events.add(event);
            eventsSize = events.size();
            if (events.size() >= mBatchSize) {
                publishEvents(events, mPublishCallback);
            }
            if (callback != null) {
                callback.onSuccess(eventsSize);
            }
        } catch (Exception e) {
            Logger.e(e.toString());
            if (callback != null) {
                callback.onFailure(new ErrorResult(e));
            }
        }

    }

    /**
     * batch된 이벤트들을 수동으로 S2 서버로 전송하고 setPublishCallback()을 통해 설정된 콜백이 있다면 실행한다.
     */
    public void publishEvents() {
        publishEvents(mPublishCallback);
    }

    /**
     * batch된 이벤트들을 수동으로 S2 서버로 전송하고 파라미터로 제공된 callback을 실행한다.
     * @param callback 이벤트 전송 요청 결과에 대한 handler
     */
    @SuppressWarnings("WeakerAccess")
    public void publishEvents(final ResponseCallback<EventsLogResponse> callback) {
        publishEvents(events, callback);
    }

    @SuppressWarnings("WeakerAccess")
    void publishEvents(final List<Event> events, final ResponseCallback<EventsLogResponse> callback) {
        final List<Event> copiedEvents;
        try {
            synchronized (INSTANCE_LOCK) {
                if (events.size() == 0) {
                    return;
                }
                copiedEvents = new ArrayList<Event>(events);
                events.clear();
            }

            KakaoTaskQueue.getInstance().addTask(new KakaoResultTask<EventsLogResponse>(callback) {
                @Override
                public EventsLogResponse call() throws Exception {
                    return S2Api.getInstance().requestPublishingEvents(contextService.getAppConfiguration(), null, copiedEvents);
                }
            });
        } catch (Exception e) {
            Logger.e(e.toString());
            if (callback != null) {
                callback.onFailure(new ErrorResult(e));
            }
        }

    }

    @SuppressWarnings("WeakerAccess")
    public static class AlreadyInitializedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
