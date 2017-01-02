package com.github.tomitakussaari.phaas;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationTest {

    @Test
    public void parameterNamesCompilerOptionIsActive() throws NoSuchMethodException {
        Method mainMethod = Application.class.getMethod("main", String[].class);
        assertThat(mainMethod.getParameters()[0].getName()).as("You need to enable -parameters compiler option").isEqualTo("arguments");
    }

}
