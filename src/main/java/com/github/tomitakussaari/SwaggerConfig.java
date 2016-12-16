package com.github.tomitakussaari;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final String DOCUMENTED_ENDPOINTS = "/passwords.*|/users.*|/encrypt.*";

    @Bean
    public Docket phaasDocumentation() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("phaas")
                .apiInfo(apiInfo())
                .globalOperationParameters(globalParameters())
                .select().paths(regex(DOCUMENTED_ENDPOINTS))
                .build();
    }

    private List<Parameter> globalParameters() {
        return Collections.singletonList(new ParameterBuilder()
                .name(AppConfig.AuditAndLoggingFilter.X_REQUEST_ID)
                .description("Request ID")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build()
        );
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("PHAAS")
                .description("Password Hashing Service")
                .version("1.0")
                .build();
    }
}
