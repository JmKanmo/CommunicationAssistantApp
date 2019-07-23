package com.kakao.util.helper;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author leoshin, created at 15. 7. 16..
 */
public final class ConstantFuture<T> implements Future<T> {
    private final T v;

    public ConstantFuture(T v) {
        this.v = v;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() {
        return v;
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        return v;
    }
}

