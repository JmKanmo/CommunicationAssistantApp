package com.kakao.s2;

import android.content.Context;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.network.tasks.KakaoResultTask;
import com.kakao.network.tasks.KakaoTaskQueue;
import com.kakao.common.KakaoContextService;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 유저의 특정 이벤트를 수집하는 API
 * @author kevin.kang
 * Created by kevin.kang on 2016. 8. 22..
 */
public class S2Service extends S2ServiceLight {

    private final List<Event> adidEvents;

    @SuppressWarnings("WeakerAccess")
    S2Service(Context context, KakaoContextService contextService) {
        super(context, contextService);
        adidEvents = Collections.synchronizedList(new ArrayList<Event>());
    }

    /**
     * 하나의 이벤트를 ADID 정보를 채워 이벤트 batch에 추가한다.
     *
     * @param event batch할 이벤트
     */
    @SuppressWarnings("unused") // will be used by sample app or third party apps.
    public void addAdidEvent(final Event event) {
        addAdidEvent(event, mAddEventCallback);
    }

    /**
     * 하나의 이벤트를 ADID 정보를 채워 이벤트 batch에 추가하고 파라미터로 제공된 callback을 실행한다.
     *
     * @param event batch할 이벤트
     */
    public void addAdidEvent(final Event event, final ResponseCallback<Integer> callback) {
        int eventsSize;
        try {
            adidEvents.add(event);
            eventsSize = adidEvents.size();
            if (eventsSize >= mBatchSize) {
                publishEvents(mPublishCallback);
            }
            if (callback != null) {
                callback.onSuccess(eventsSize);
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onFailure(new ErrorResult(e));
            }
        }
    }

    @Override
    public void publishEvents(final ResponseCallback<EventsLogResponse> callback) {
        super.publishEvents(callback);
        final List<Event> copiedEvents;
        try {
            synchronized (INSTANCE_LOCK) {
                if (adidEvents.size() == 0) {
                    return;
                }
                copiedEvents = new ArrayList<>(adidEvents);
                adidEvents.clear();
            }

            KakaoTaskQueue.getInstance().addTask(new KakaoResultTask<EventsLogResponse>(callback) {
                @Override
                public EventsLogResponse call() throws Exception {
                    AdvertisingIdClient.Info idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    if (idInfo == null) {
                        throw new IllegalStateException("Failed to get ADID from the device.");
                    }
                    Event rootEvent = new Event(idInfo.getId(), !idInfo.isLimitAdTrackingEnabled());
                    return S2Api.getInstance().requestPublishingAdidEvents(contextService.getAppConfiguration(), rootEvent, copiedEvents);
                }
            });
        } catch (Exception e) {
            if (callback != null) {
                callback.onFailure(new ErrorResult(e));
            }
        }
    }
}
