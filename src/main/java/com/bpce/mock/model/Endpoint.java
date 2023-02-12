package com.bpce.mock.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Endpoint {
    private String path;
    private String method;
    private String mode;
    private int httpCode;
    private String responseJson;
}