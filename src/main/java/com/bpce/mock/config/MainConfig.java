package com.bpce.mock.config;

import com.bpce.mock.model.Endpoint;
import com.bpce.mock.model.MockerMainEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Configuration
public class MainConfig {

    @Value("${mock.config.path}")
    private String path;

    @Bean
    public HashMap<String, Endpoint> getEndpointsMap() throws IOException {
        HashMap<String, Endpoint> endpoints = new HashMap<>();
        new ObjectMapper().readValue(new File(path + "endpoints.json"), MockerMainEntry.class).getEndpoints()
                .forEach(endpoint -> {
                    endpoints.put("/mock".concat(endpoint.getPath()), endpoint);
                });
        return endpoints;
    }

}
