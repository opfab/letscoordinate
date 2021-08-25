/*
 * Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
 * Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.backend.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import org.lfenergy.letscoordinate.backend.util.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.time.OffsetDateTime;

@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final LetscoProperties letscoProperties;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/letsco/api/**")
                        .allowedOrigins(letscoProperties.getSecurity().getAllowedOrigins())
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD")
                        .allowedHeaders("authorization", "content-type", "x-auth-token")
                        .allowCredentials(true);
            }
        };
    }

    @Autowired(required = true)
    public void configureJackson(ObjectMapper jackson2ObjectMapper) {
        LetscoProperties.InputFile.Validation inputFileValidationConfig = letscoProperties.getInputFile().getValidation();
        jackson2ObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jackson2ObjectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, inputFileValidationConfig.isAcceptPropertiesIgnoreCase());
        jackson2ObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, inputFileValidationConfig.isFailOnUnknownProperties());
        jackson2ObjectMapper.registerModule(new SimpleModule().addSerializer(Instant.class, new JacksonUtil.InstantSerializer())
                .addSerializer(OffsetDateTime.class, new JacksonUtil.OffsetDateTimeSerializer()));
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(8388607);
        return multipartResolver;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(); // will use the ObjectMapper configured with "WebConfig.configureJackson"
    }

    @Bean
    public RestTemplate restTemplateForOpfab() {
        return new RestTemplateBuilder()
                .additionalMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();
    }

}
