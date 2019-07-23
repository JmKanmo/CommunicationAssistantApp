package com.kakao.auth.mocks;

import com.kakao.network.IRequest;
import com.kakao.network.NetworkService;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.network.response.ApiResponseStatusError;
import com.kakao.network.response.CustomErrorConverter;
import com.kakao.network.response.ResponseBody;
import com.kakao.network.response.ResponseStringConverter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author kevin.kang. Created on 2017. 12. 4..
 */

public class TestNetworkService implements NetworkService {
    @Override
    public <T> Future<T> request(IRequest request, ResponseStringConverter<T> converter, ResponseCallback<T> callback) {
        return null;
    }

    @Override
    public <T, E extends Exception> T request(IRequest request, ResponseStringConverter<T> converter, CustomErrorConverter<E> errorConverter) throws IOException, ResponseBody.ResponseBodyException, E {
        return null;
    }

    @Override
    public <T, E extends Exception> Future<T> request(IRequest request, ResponseStringConverter<T> converter, CustomErrorConverter<E> errorConverter, ResponseCallback<T> callback) {
        return null;
    }

    @Override
    public <T> T request(IRequest request, ResponseStringConverter<T> converter) throws IOException, ResponseBody.ResponseBodyException, ApiResponseStatusError {
        return null;
    }

    @Override
    public <T> Future<List<T>> requestList(IRequest request, ResponseStringConverter<T> converter, ResponseCallback<List<T>> callback) {
        return null;
    }

    @Override
    public <T> List<T> requestList(IRequest request, ResponseStringConverter<T> converter) throws IOException {
        return null;
    }
}
