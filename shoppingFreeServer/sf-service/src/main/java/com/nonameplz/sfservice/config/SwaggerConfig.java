package com.nonameplz.sfservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SwaggerConfig {

    @Value("${knife4j.openapi.title}")
    private String sf_title;

    @Value("${knife4j.openapi.version}")
    private String sf_version;

    @Value("${knife4j.openapi.authName}")
    private String sf_authName;

    @Value("${knife4j.openapi.email}")
    private String sf_email;

    @Value("${knife4j.openapi.url}")
    private String sf_url;

    @Value("${knife4j.openapi.description}")
    private String sf_description;

    @Bean
    public OpenAPI customOpenAPI() {
        Contact sf_contact = new Contact();
        sf_contact.setName(sf_authName);
        sf_contact.setUrl(sf_email);
        sf_contact.setUrl(sf_url);

        return new OpenAPI()
                .info(new Info()
                        .title(sf_title)
                        .version(sf_version)
                        .contact(sf_contact)
                        .description(sf_description)
                );
    }
}
