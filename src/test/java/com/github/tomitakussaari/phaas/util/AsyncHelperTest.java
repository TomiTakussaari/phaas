package com.github.tomitakussaari.phaas.util;

import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixEventType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncHelperTest {

    @After
    @Before
    public void cleanUp() {
        Hystrix.reset();
    }


    @Test
    public void getCauseGetsReturnsCauseIfPresent() {
        RuntimeException withoutCause = new RuntimeException();
        RuntimeException withCause = new RuntimeException(withoutCause);

        assertThat(AsyncHelper.getCause(withoutCause)).isSameAs(withoutCause);
        assertThat(AsyncHelper.getCause(withCause)).isSameAs(withoutCause);
    }

    @Test
    public void usesDifferentThreadToExecuteAsync() {
        String threadName = AsyncHelper.withName("testing").doWithTimeout(() -> Thread.currentThread().getName());
        assertThat(threadName).isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    public void usesDifferentThreadToFinishDeferredResult() throws InterruptedException {
        DeferredResult<String> threadNameDeferred = AsyncHelper.withName("testing").toDeferredResult(() -> Thread.currentThread().getName());
        Thread.sleep(100);
        assertThat(threadNameDeferred.getResult()).isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    public void illegalStateExceptionsDoNotCountAsHystrixFailures() {
        verifyExceptionIsConverted(IllegalStateException.class, HystrixEventType.BAD_REQUEST, HystrixEventType.FAILURE);
    }

    @Test
    public void illegalArgumentExceptionsDoNotCountAsHystrixFailures() {
        verifyExceptionIsConverted(IllegalArgumentException.class, HystrixEventType.BAD_REQUEST, HystrixEventType.FAILURE);
    }

    @Test
    public void genericRuntimeExceptionsAreCountedAsHystrixFailures() {
        verifyExceptionIsConverted(RuntimeException.class, HystrixEventType.FAILURE, HystrixEventType.BAD_REQUEST);
    }

    private void verifyExceptionIsConverted(Class<? extends Exception> clazz, HystrixEventType expectedType, HystrixEventType notExpectedType) {
        try {
            Supplier<?> exceptionSupplier = () -> {
                try {
                    throw (RuntimeException) clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
            AsyncHelper.withName("testing").doWithTimeout(exceptionSupplier);

            HystrixCommandMetrics metrics = HystrixCommandMetrics.getInstance(HystrixCommandKey.Factory.asKey("NamedCommand"));
            assertThat(metrics.getCumulativeCount(expectedType)).isEqualTo(1);
            assertThat(metrics.getCumulativeCount(notExpectedType)).isEqualTo(0);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(clazz);
        }
    }

}