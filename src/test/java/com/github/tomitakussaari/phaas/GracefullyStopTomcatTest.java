package com.github.tomitakussaari.phaas;

import com.github.tomitakussaari.phaas.GracefullyStopTomcat.GracefulShutdown;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;

public class GracefullyStopTomcatTest {

    private Connector connector = Mockito.mock(Connector.class);
    private ProtocolHandler protocolHandler = Mockito.mock(ProtocolHandler.class);
    private ThreadPoolExecutor threadPoolExecutor = Mockito.mock(ThreadPoolExecutor.class);

    @Test
    public void shutsDownTomcatGracefully() throws InterruptedException {
        when(connector.getProtocolHandler()).thenReturn(protocolHandler);
        when(protocolHandler.getExecutor()).thenReturn(threadPoolExecutor);

        GracefulShutdown gracefulShutdown = new GracefulShutdown();
        gracefulShutdown.customize(connector);

        gracefulShutdown.onApplicationEvent(null);
        Mockito.verify(threadPoolExecutor).awaitTermination(30, TimeUnit.SECONDS);
        Mockito.verify(threadPoolExecutor).shutdown();
    }

}