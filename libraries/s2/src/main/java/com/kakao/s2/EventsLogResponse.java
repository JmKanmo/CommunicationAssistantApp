package com.kakao.s2;

import com.kakao.network.response.ApiResponseStatusError;
import com.kakao.network.response.JSONObjectResponse;
import com.kakao.network.response.ResponseBody;
import com.kakao.network.response.ResponseData;
import com.kakao.network.response.ResponseStringConverter;

/**
 * @author kevin.kang
 * Created by kevin.kang on 2016. 8. 22..
 */
public class EventsLogResponse extends JSONObjectResponse {
    private int successCount = 0;

    @Deprecated
    EventsLogResponse(ResponseData responseData) throws ResponseBody.ResponseBodyException, ApiResponseStatusError {
        this(responseData.getStringData());
    }

    EventsLogResponse(String stringData) throws ApiResponseStatusError, ResponseBody.ResponseBodyException {
        super(stringData);
        successCount = getBody().getInt(StringSet.count);
    }

    public int getSuccessCount() {
        return successCount;
    }

    public static final ResponseStringConverter<EventsLogResponse> CONVERTER = new ResponseStringConverter<EventsLogResponse>() {
        @Override
        public EventsLogResponse convert(String o) throws ResponseBody.ResponseBodyException {
            return new EventsLogResponse(o);
        }
    };
}