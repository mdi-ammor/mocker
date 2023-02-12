package com.bpce.mock.service;

import com.bpce.mock.exception.EndpointsSamePathException;
import com.bpce.mock.model.Endpoint;
import com.bpce.mock.tools.MockerConstant;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Service
public class EndpointMatcherSrv {

    private final HashMap<String, Endpoint> endpoints;

    public EndpointMatcherSrv(HashMap<String, Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public Endpoint getEndpoint(String uri, String method) throws EndpointsSamePathException {
        Endpoint endpoint = getExactEndpoint(uri, method);

        if(Objects.isNull(endpoint))
            endpoint = getEndpointWithPlaceHolder(uri);

        return endpoint;
    }

    private Endpoint getExactEndpoint(String uri, String method) throws EndpointsSamePathException {
        List<Endpoint> foundEndpoints = endpoints.entrySet().stream()
                .filter(ep -> uri.equals(ep.getKey()) && method.equals(ep.getValue().getMethod()))
                .collect(Collectors.toMap(ep -> ep.getKey(), ep -> ep.getValue()))
                .values()
                .stream()
                .collect(Collectors.toList());

        if(foundEndpoints.isEmpty()) return null;
        else if (foundEndpoints.size() > 1) throw new EndpointsSamePathException();
        else return foundEndpoints.get(0);
    }

    private Endpoint getEndpointWithPlaceHolder(String uri) throws EndpointsSamePathException {
        int uriPartsLength = uri.split(MockerConstant.PATH_DELIMITER).length;
        List<Endpoint> foundEndpoints = endpoints.entrySet().stream()
                .filter(ep -> ep.getKey().contains(MockerConstant.PLACEHOLDER) && ep.getKey().split(MockerConstant.PATH_DELIMITER).length == uriPartsLength && checkPlaceholders(ep.getKey(), uri))
                .collect(Collectors.toMap(epEntry -> epEntry.getKey(), epEntry -> epEntry.getValue()))
                .values()
                .stream()
                .collect(Collectors.toList());

        if(foundEndpoints.isEmpty()) return null;
        else if (foundEndpoints.size() > 1) throw new EndpointsSamePathException();
        else return foundEndpoints.get(0);
    }

    private boolean checkPlaceholders(String confUrl, String givenUrl) {
        String[] confUrlParts = confUrl.split(MockerConstant.PATH_DELIMITER);
        String[] givenUrlParts = givenUrl.split(MockerConstant.PATH_DELIMITER);
        int index = -1;
        for(String s : confUrlParts) {
            index++;
            if(MockerConstant.PLACEHOLDER.equals(s)) givenUrlParts[index] = MockerConstant.PLACEHOLDER;
        }
        StringJoiner givenUrlWithPlaceholders = new StringJoiner(MockerConstant.PATH_DELIMITER);
        for(String s:givenUrlParts) givenUrlWithPlaceholders.add(s);

        return confUrl.equals(givenUrlWithPlaceholders.toString());
    }

}
