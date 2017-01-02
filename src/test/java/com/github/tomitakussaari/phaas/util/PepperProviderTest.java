package com.github.tomitakussaari.phaas.util;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

public class PepperProviderTest {

    private Environment environment = Mockito.mock(Environment.class);

    @Test
    public void returnsDefaultPepperWhenNotConfigured() {
        noPropertyConfigured();
        PepperProvider provider = new PepperProvider(environment);
        assertEquals("", provider.getPepper());
    }

    @Test
    public void returnsPepperFromString() {
        propertyConfigured("string://my-pepper");
        PepperProvider provider = new PepperProvider(environment);
        assertEquals("my-pepper", provider.getPepper());
    }

    private void noPropertyConfigured() {
        Mockito.when(environment.getProperty(eq("phaas.pepper.source"), any(String.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
    }

    private void propertyConfigured(String value) {
        Mockito.when(environment.getProperty(eq("phaas.pepper.source"), any(String.class))).thenReturn(value);
    }
}