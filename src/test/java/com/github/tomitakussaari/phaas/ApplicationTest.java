package com.github.tomitakussaari.phaas;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class ApplicationTest {

    @Test
    public void parameterNamesCompilerOptionIsActive() throws NoSuchMethodException {
        Method mainMethod = Application.class.getMethod("main", String[].class);
        assertEquals("You need to enable -parameters compiler option", "arguments", mainMethod.getParameters()[0].getName());
    }

}