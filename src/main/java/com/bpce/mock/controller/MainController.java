package com.bpce.mock.controller;

import com.bpce.mock.exception.EndpointsSamePathException;
import com.bpce.mock.exception.InvalidTypeException;
import com.bpce.mock.model.Endpoint;
import com.bpce.mock.model.MockerError;
import com.bpce.mock.model.MockerMainEntry;
import com.bpce.mock.service.EndpointMatcherSrv;
import com.bpce.mock.service.ResponseGeneratorSrv;
import com.bpce.mock.tools.MockerConstant;
import com.bpce.mock.tools.ErrorsEnum;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/mock")
public class MainController {

    @Value("${mock.config.path}")
    private String mockConfigPath;
    private final HashMap<String, Endpoint> endpoints;
    private final EndpointMatcherSrv endpointMatcherSrv;
    private final ResponseGeneratorSrv responseGeneratorSrv;

    public MainController(HashMap<String, Endpoint> endpoints, EndpointMatcherSrv endpointMatcherSrv, ResponseGeneratorSrv responseGeneratorSrv) {
        this.endpoints = endpoints;
        this.endpointMatcherSrv = endpointMatcherSrv;
        this.responseGeneratorSrv = responseGeneratorSrv;
    }

    @GetMapping("/**")
    public ResponseEntity get(HttpServletRequest request) throws IOException {
        return render(request.getRequestURI(), "GET");
    }

    @PostMapping ("/**")
    public ResponseEntity post(HttpServletRequest request) throws IOException {
        return render(request.getRequestURI(), "POST");
    }

    @PutMapping("/**")
    public ResponseEntity put(HttpServletRequest request) throws IOException {
        return render(request.getRequestURI(), "PUT");
    }

    @PostMapping("/refresh")
    public ResponseEntity refreshScope() throws IOException {
        endpoints.clear();
        new ObjectMapper().readValue(new File(mockConfigPath + "endpoints.json"), MockerMainEntry.class).getEndpoints()
                .forEach(endpoint -> {
                    endpoints.put("/mock".concat(endpoint.getPath()), endpoint);
                });
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private ResponseEntity render(String path, String method) throws IOException {
        try{
            Endpoint endpoint = endpointMatcherSrv.getEndpoint(path, method);
            if(Objects.nonNull(endpoint)) {
                ObjectMapper mapper = new ObjectMapper();
                File file = new File(mockConfigPath + "\\responses\\" + endpoint.getResponseJson());
                if(file.exists() && !file.isDirectory())
                    switch (endpoint.getMode()) {
                        case MockerConstant.SIMPLE_MODE : return new ResponseEntity<>(mapper.readValue(file, Object.class), HttpStatus.valueOf(endpoint.getHttpCode()));
                        case MockerConstant.CUSTOM_MODE : return new ResponseEntity<>(responseGeneratorSrv.generateNode(mapper.readTree(file)), HttpStatus.valueOf(endpoint.getHttpCode()));
                        default: return new ResponseEntity(buildError(ErrorsEnum.NO_SUCH_MODE, endpoint.getMode()), HttpStatus.NOT_IMPLEMENTED);
                    }
                return new ResponseEntity<>(buildError(ErrorsEnum.NO_SUCH_RESPONSE_FILE, endpoint.getResponseJson()),HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity(buildError(ErrorsEnum.NO_SUCH_ENDPOINT, path), HttpStatus.NOT_FOUND);
        } catch(EndpointsSamePathException ex) {
            return new ResponseEntity(buildError(ErrorsEnum.MULTIPLE_ENDPOINTS_FOR_SAME_PATH), HttpStatus.CONFLICT);
        } catch (InvalidTypeException e) {
            return new ResponseEntity(buildError(ErrorsEnum.NO_SUCH_TYPE, e.getField(), e.getType()), HttpStatus.NOT_IMPLEMENTED);
        } catch (JsonParseException | JsonMappingException e) {
            return new ResponseEntity(buildError(ErrorsEnum.PARSING_ERROR), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private MockerError buildError(ErrorsEnum error, String... parameters) {
        return MockerError.builder()
                .error(error.name())
                .description(String.format(error.message(), parameters))
                .build();
    }
}
