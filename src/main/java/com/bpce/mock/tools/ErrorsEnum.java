package com.bpce.mock.tools;

public enum ErrorsEnum {
    NO_SUCH_MODE(" Unsupported mode error, only 'SIMPLE' and 'CUSTOM' modes are available (mode in question : %s)."),
    NO_SUCH_ENDPOINT("No endpoint could be found for %s."),
    MULTIPLE_ENDPOINTS_FOR_SAME_PATH("Multiple endpoints may not be deployed to the same path"),
    NO_SUCH_RESPONSE_FILE("Response file missing (file in question : %s)"),
    NO_SUCH_TYPE("Invalid type for the attribute %s (type in question : %s)."),
    PARSING_ERROR("JSON parsing/mapping exception, check your response file format.");
    private String message;

    ErrorsEnum(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
