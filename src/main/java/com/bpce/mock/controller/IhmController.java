package com.bpce.mock.controller;

import com.bpce.mock.model.Endpoint;
import com.bpce.mock.model.IhmEndpoint;
import com.bpce.mock.model.MockerMainEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import static com.bpce.mock.tools.LambdaExceptionUtils.rethrowConsumer;

@Slf4j
@RestController
@RequestMapping("/ihm")
public class IhmController {

    @Value("${mock.config.path}")
    private String mockConfigPath;
    private final HashMap<String, Endpoint> endpoints;

    public IhmController(HashMap<String, Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    @GetMapping("/endpoints")
    public Set<String> getEndpoints() {
        return endpoints.keySet();
    }

    @GetMapping("/endpoints/{endpointKey}")
    public IhmEndpoint getEndpoint(@PathVariable String endpointKey) {
        try {
            String key = endpoints.keySet().stream().filter(k -> k.replaceAll("/","").equals(endpointKey)).findFirst().get();
            Endpoint endpoint = endpoints.get(key);
            return IhmEndpoint.builder()
                    .endpoint(endpoint)
                    .response(
                        new ObjectMapper().readTree(new File(mockConfigPath + "\\responses\\" + endpoint.getResponseJson())))
                    .build();
        } catch (IOException e) {
            log.error(String.format("Unable to get the endpoint %s", endpointKey));
            return null;
        }
    }

    @PostMapping("/endpoints/{endpointKey}")
    public void updateEndpoint(@PathVariable String endpointKey, @RequestBody IhmEndpoint data) {
        try {
            String key = endpoints.keySet().stream().filter(k -> k.replaceAll("/","").equals("mock"+endpointKey)).findFirst().get();
            // Modify endpoints json
            JsonNode root = new ObjectMapper().readTree(new File(mockConfigPath + "\\endpoints.json"));
            root.get("endpoints").forEach(jn -> {
                if("/mock".concat(jn.get("path").asText()).equals(key)) {
                    ((ObjectNode)jn).put("path", data.getEndpoint().getPath());
                    ((ObjectNode)jn).put("method", data.getEndpoint().getMethod());
                    ((ObjectNode)jn).put("mode", data.getEndpoint().getMode());
                }
            });

            // Write endpoints
            FileWriter fileWriter = new FileWriter(mockConfigPath + "\\endpoints.json");
            fileWriter.write(root.toString());
            fileWriter.close();
            // Write response
            fileWriter = new FileWriter(mockConfigPath + "\\responses\\" + data.getEndpoint().getResponseJson());
            fileWriter.write(data.getResponse().toString());
            fileWriter.close();

            refreshScope();
           // ((ObjectNode)root.get(key)).put("method", data.getEndpoint().getMethod());

            log.info(root.toString());
            // Modify response file
        } catch (IOException e) {
            log.error("Fuck");
        }
    }

    //TODO externalize me
    private ResponseEntity refreshScope() throws IOException {
        endpoints.clear();
        new ObjectMapper().readValue(new File(mockConfigPath + "endpoints.json"), MockerMainEntry.class).getEndpoints()
                .forEach(endpoint -> {
                    endpoints.put("/mock".concat(endpoint.getPath()), endpoint);
                });
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
