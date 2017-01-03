package com.github.tomitakussaari.phaas.util;

import com.google.common.io.Files;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.function.BiConsumer;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class PepperSourceTest {

    private Environment environment = Mockito.mock(Environment.class);

    @Test
    public void refuesNullUrlFromEnvironment() {
        when(environment.getProperty(eq("phaas.pepper.source"), any(String.class))).thenReturn(null);
        try {
            new PepperSource(environment);
            fail("should not have succeeded");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NullPointerException.class).hasMessage("url");
        }
    }

    @Test
    public void returnsDefaultPepperWhenNotConfigured() {
        noPropertyConfigured();
        PepperSource provider = new PepperSource(environment);
        assertThat(provider.getPepper()).isEqualTo("");
    }

    @Test
    public void returnsPepperFromString() {
        propertyConfigured("string://my-pepper");
        PepperSource provider = new PepperSource(environment);
        assertThat(provider.getPepper()).isEqualTo("my-pepper");
    }

    @Test
    public void returnsPepperFromFile() throws IOException {
        File pepperFile = File.createTempFile("pepper-provider-test", "test");
        pepperFile.deleteOnExit();
        Files.write("pepper-from-file", pepperFile, StandardCharsets.UTF_8);
        propertyConfigured("file://" + pepperFile.getAbsolutePath());
        PepperSource provider = new PepperSource(environment);
        assertEquals("pepper-from-file", provider.getPepper());
    }

    @Test
    public void failsIfPepperFileIsNotFound() {
        propertyConfigured("file://" + RandomStringUtils.randomAlphanumeric(10));
        try {
            new PepperSource(environment);
        } catch (IllegalStateException e) {
            assertThat(e).hasCauseInstanceOf(NoSuchFileException.class);
        }
    }

    @Test
    public void readsPepperFromHttpServerWithCustomHeaders() throws Exception {
        withHttpServer(200, (httpPort, handler) -> {
            String url = "http://localhost:" + httpPort + "/secret|header1=value1&header2=value2";
            propertyConfigured(url);
            assertThat(new PepperSource(environment).getPepper()).isEqualTo("This is the pepper");
            assertThat(handler.lastURI.toString()).isEqualTo("/secret");
            assertThat(handler.lastHeaders).containsEntry("Header1", singletonList("value1"));
            assertThat(handler.lastHeaders).containsEntry("Header2", singletonList("value2"));
            assertThat(handler.lastHeaders).containsEntry("User-agent", singletonList("phaas"));
        });
    }

    @Test
    public void readsPepperFromHttpServer() throws Exception {
        withHttpServer(200, (httpPort, handler) -> {
            String url = "http://localhost:" + httpPort + "/secret";
            propertyConfigured(url);
            assertThat(new PepperSource(environment).getPepper()).isEqualTo("This is the pepper");
            assertThat(handler.lastURI.toString()).isEqualTo("/secret");
            assertThat(handler.lastHeaders).containsEntry("User-agent", singletonList("phaas"));
        });
    }

    @Test
    public void refusesToUseHttpResponseIfStatusServerError() {
        try {
            withHttpServer(500, (httpPort, handler) -> {
                String url = "http://localhost:" + httpPort + "/secret";
                propertyConfigured(url);
                new PepperSource(environment);
            });
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(HttpServerErrorException.class);
        }
    }

    @Test
    public void refusesToUseHttpResponseIfStatusClientError() {
        try {
            withHttpServer(400, (httpPort, handler) -> {
                String url = "http://localhost:" + httpPort + "/secret";
                propertyConfigured(url);
                new PepperSource(environment);
            });
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(HttpClientErrorException.class);
        }
    }

    @Test
    public void refusesToUseHttpResponseIfStatusNoContent() {
        try {
            withHttpServer(204, (httpPort, handler) -> {
                String url = "http://localhost:" + httpPort + "/secret";
                propertyConfigured(url);
                new PepperSource(environment);
            });
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class).withFailMessage("Expected http ok, but got: 204");
        }
    }

    @Test(timeout = 5000)
    public void hasTimeOutsForHttp() {
        try {
            withHttpServer(200, (httpPort, handler) -> {
                String url = "http://localhost:" + httpPort + "/secret|X-be-slow=true";
                propertyConfigured(url);
                new PepperSource(environment);
            });
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ResourceAccessException.class).hasMessageContaining("Read timed out");
        }
    }

    private void withHttpServer(int statusCode, BiConsumer<Integer, RequestSavingPepperResponseHandler> testCase) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        RequestSavingPepperResponseHandler requestSavingPepperResponseHandler = new RequestSavingPepperResponseHandler(statusCode);
        try {
            server.createContext("/secret", requestSavingPepperResponseHandler);
            server.start();
            testCase.accept(server.getAddress().getPort(), requestSavingPepperResponseHandler);
        } finally {
            server.stop(1);
        }
    }

    private void noPropertyConfigured() {
        when(environment.getProperty(eq("phaas.pepper.source"), any(String.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
    }

    private void propertyConfigured(String value) {
        when(environment.getProperty(eq("phaas.pepper.source"), any(String.class))).thenReturn(value);
    }

    static class RequestSavingPepperResponseHandler implements HttpHandler {
        private final int statusCode;
        private Headers lastHeaders;
        private URI lastURI;

        public RequestSavingPepperResponseHandler(int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            lastHeaders = exchange.getRequestHeaders();
            lastURI = exchange.getRequestURI();
            String response = "This is the pepper";
            if (exchange.getRequestHeaders().containsKey("X-be-slow")) {
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
            }
            exchange.sendResponseHeaders(statusCode, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}