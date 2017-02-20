package com.github.tomitakussaari.phaas;

import com.github.tomitakussaari.phaas.GracefullyStopTomcat.GracefulShutdown;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.junit.*;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class GracefullyStopTomcatTest {

    private Connector connector = Mockito.mock(Connector.class);
    private ProtocolHandler protocolHandler = Mockito.mock(ProtocolHandler.class);
    private ThreadPoolExecutor threadPoolExecutor = Mockito.mock(ThreadPoolExecutor.class);
    private GracefulShutdown gracefulShutdown = new GracefulShutdown();

    @Before
    @After
    public void clearThreadInterruptState() {
        Thread.interrupted();
    }

    @Before
    public void init() {
        when(connector.getProtocolHandler()).thenReturn(protocolHandler);
        when(protocolHandler.getExecutor()).thenReturn(threadPoolExecutor);
        gracefulShutdown.customize(connector);
    }

    @Test
    public void shutsDownTomcatGracefully() throws InterruptedException {
        gracefulShutdown.onApplicationEvent(null);
        Mockito.verify(threadPoolExecutor).awaitTermination(30, TimeUnit.SECONDS);
        Mockito.verify(threadPoolExecutor).shutdown();

        assertThat(Thread.currentThread().isInterrupted()).isFalse();
    }

    @Test
    public void shutsDownTomcatGracefullyWhenAwaitTerminationThrowsException() throws InterruptedException {
        when(threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)).thenThrow(new InterruptedException());
        gracefulShutdown.onApplicationEvent(null);

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }

}
