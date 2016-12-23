package com.github.tomitakussaari.phaas.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.IOException;

public abstract class JsonHelper {

    public static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule())
            .registerModule(new AfterburnerModule());

    public static String serialize(Object input) {
        return objectMapSafely(() -> objectMapper.writeValueAsString(input));
    }

    public static <T> T objectMapSafely(ObjectMapperOperation<T> objectMapperOperation) {
        try {
            return objectMapperOperation.get();
        } catch (IOException e) {
            throw new UnsupportedOperationException("JSON serialization does not work ?", e);
        }
    }

    public interface ObjectMapperOperation<T> {
        T get() throws IOException;
    }
}
