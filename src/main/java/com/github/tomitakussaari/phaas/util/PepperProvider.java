package com.github.tomitakussaari.phaas.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

@Service
public class PepperProvider {

    @RequiredArgsConstructor
    @Getter
    private enum Source {
        STRING("string", source -> {
            return source.replaceAll("string://", "");
        }),
        FILE("file", source -> {
            try {
                return new String(Files.readAllBytes(Paths.get(source.replaceAll("file://", ""))), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }),
        HTTP("http", source -> {
            try {
                URL url = new URL(source);
                url.getAuthority();
                return null;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });

        private final String protocol;
        private final Function<String, String> pepperLoader;
    }

    private final String pepper;

    @Autowired
    public PepperProvider(Environment environment) {
        this(loadPepperFrom(environment.getProperty("phaas.pepper.source", "string://")));
    }

    public PepperProvider(String pepper) {
        this.pepper = pepper;
    }

    public String getPepper() {
        return pepper;
    }

    static String loadPepperFrom(String url) {
        for(Source source: Source.values()) {
            if(url.startsWith(source.protocol)) {
                return source.getPepperLoader().apply(url);
            }
        }
        throw new IllegalArgumentException("No protocolhandler for "+url);
    }
}
