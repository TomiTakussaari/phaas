package com.github.tomitakussaari.phaas.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Service
public class PepperProvider {

    @RequiredArgsConstructor
    @Getter
    private enum Source {
        STRING("string", source -> {
            return source.replaceAll("string://", "");
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
