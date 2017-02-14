package com.github.tomitakussaari.phaas.util;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

import static org.springframework.http.HttpMethod.GET;

@Service
public class PepperSource {

    private final String pepper;

    @Autowired
    public PepperSource(Environment environment) {
        this(loadPepperFrom(environment.getProperty("phaas.pepper.source", "string://")));
    }

    public PepperSource(String pepper) {
        this.pepper = pepper;
    }


    public String getPepper() {
        return pepper;
    }

    private static String loadPepperFrom(@NonNull String url) {
        for (Source source : Source.values()) {
            if (url.toLowerCase().startsWith(source.name().toLowerCase())) {
                return AsyncHelper.withName("pepper").doWithTimeout(() -> source.getPepperLoader().apply(url));
            }
        }
        throw new IllegalArgumentException("No protocolhandler for " + url);
    }

    @RequiredArgsConstructor
    @Getter
    private enum Source {
        STRING(source -> source.replaceAll("string://", "")),
        FILE(source -> {
            try {
                return new String(Files.readAllBytes(Paths.get(source.replaceAll("file://", ""))), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }),
        HTTP(source -> {
            String[] urlAndHeaders = source.split("\\|");
            String url = urlAndHeaders[0];
            String headersString = urlAndHeaders.length > 1 ? urlAndHeaders[1] : "";
            RestTemplate restTemplate = new RestTemplateBuilder().setConnectTimeout(500).setReadTimeout(2000).build();
            MultiValueMap<String, String> headerParams = UriComponentsBuilder.fromUriString("/?" + headersString).build().getQueryParams();
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(headerParams);
            headers.set("User-agent", "phaas");
            ResponseEntity<String> response = restTemplate.exchange(url, GET, new HttpEntity<String>(headers), String.class);
            Preconditions.checkState(response.getStatusCode() == HttpStatus.OK, "Expected http ok, but got: " + response.getStatusCode());
            return response.getBody();
        });

        private final Function<String, String> pepperLoader;
    }

}
