package com.github.tomitakussaari.phaas.util;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableList.of;

public class AsyncHelper {

    private final String name;
    private static final int timeout = 10_000;
    private static final List<Class<? extends Exception>> badRequestExceptions = of(IllegalArgumentException.class, IllegalStateException.class);

    private AsyncHelper(String name) {
        this.name = name;
    }

    public static AsyncHelper withName(String name) {
        return new AsyncHelper(name);
    }

    public <T> DeferredResult<T> toDeferredResult(Supplier<T> operation) {
        DeferredResult<T>  deferred = new DeferredResult<>((long) timeout);
        NamedCommand<T> command = new NamedCommand<>(name, operation);
        command.observe().subscribe(deferred::setResult, error -> deferred.setErrorResult(getCause(error)));
        return deferred;
    }

    public <T> T doWithTimeout(Supplier<T> operation) {
        try {
            return new NamedCommand<>(name, operation).execute();
        } catch (RuntimeException e) {
            throw (RuntimeException) getCause(e);
        }
    }

    static Throwable getCause(Throwable error) {
        if(error.getCause() != null) {
            return error.getCause();
        }
        return error;
    }

    static class NamedCommand<T> extends HystrixCommand<T> {
        private final Supplier<T> operation;

        private NamedCommand(String name, Supplier<T> operation) {
            super(HystrixCommandGroupKey.Factory.asKey(name), timeout);
            this.operation = operation;
        }

        @Override
        protected T run() throws Exception {
            try {
                return operation.get();
            } catch(Exception e) {
                throw badRequestExceptions.stream()
                        .filter(badRequestException -> ExceptionUtils.indexOfType(e, badRequestException) > -1)
                        .map(clazz -> new HystrixBadRequestException(e.getMessage(), e))
                        .findAny().orElseThrow(() -> e);
            }

        }
    }

}
