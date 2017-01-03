package com.github.tomitakussaari.phaas.util;

import com.google.common.io.Files;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

public class PepperProviderTest {

    private Environment environment = Mockito.mock(Environment.class);

    @Test
    public void returnsDefaultPepperWhenNotConfigured() {
        noPropertyConfigured();
        PepperProvider provider = new PepperProvider(environment);
        assertThat(provider.getPepper()).isEqualTo("");
    }

    @Test
    public void returnsPepperFromString() {
        propertyConfigured("string://my-pepper");
        PepperProvider provider = new PepperProvider(environment);
        assertThat(provider.getPepper()).isEqualTo("my-pepper");
    }

    @Test
    public void returnsPepperFromFile() throws IOException {
        File pepperFile = File.createTempFile("pepper-provider-test", "test");
        pepperFile.deleteOnExit();
        Files.write("pepper-from-file", pepperFile, StandardCharsets.UTF_8);
        propertyConfigured("file://"+pepperFile.getAbsolutePath());
        PepperProvider provider = new PepperProvider(environment);
        assertEquals("pepper-from-file", provider.getPepper());
    }

    @Test
    public void failsIfPepperFileIsNotFound() {
        propertyConfigured("file://"+ RandomStringUtils.randomAlphanumeric(10));
        try {
            new PepperProvider(environment);
        } catch(IllegalStateException e) {
            assertThat(e).hasCauseInstanceOf(NoSuchFileException.class);
        }
    }

    private void noPropertyConfigured() {
        Mockito.when(environment.getProperty(eq("phaas.pepper.source"), any(String.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
    }

    private void propertyConfigured(String value) {
        Mockito.when(environment.getProperty(eq("phaas.pepper.source"), any(String.class))).thenReturn(value);
    }
}