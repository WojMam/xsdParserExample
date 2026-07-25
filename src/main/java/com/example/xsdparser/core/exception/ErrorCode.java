package com.example.xsdparser.core.exception;

/**
 * Error codes for categorizing exceptions in the XSD Parser application.
 * Each code represents a specific type of error for easier debugging and error handling.
 */
public enum ErrorCode {

    GENERAL_ERROR("GEN001", "General error occurred"),

    SERIALIZATION_ERROR("SER001", "XML serialization failed"),
    DESERIALIZATION_ERROR("SER002", "XML deserialization failed"),
    MARSHALLING_ERROR("SER003", "JAXB marshalling failed"),
    UNMARSHALLING_ERROR("SER004", "JAXB unmarshalling failed"),

    VALIDATION_ERROR("VAL001", "XML validation failed"),
    SCHEMA_NOT_FOUND("VAL002", "Schema not found"),
    SCHEMA_LOAD_ERROR("VAL003", "Failed to load schema"),
    INVALID_XML_STRUCTURE("VAL004", "Invalid XML structure"),

    REGISTRY_ERROR("REG001", "Schema registry error"),
    SCHEMA_ALREADY_REGISTERED("REG002", "Schema already registered"),
    SCHEMA_NOT_REGISTERED("REG003", "Schema not registered"),

    CONFIGURATION_ERROR("CFG001", "Configuration error"),
    INITIALIZATION_ERROR("CFG002", "Initialization error"),

    IO_ERROR("IO001", "I/O operation failed"),
    RESOURCE_NOT_FOUND("IO002", "Resource not found");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public String toString() {
        return code + " - " + defaultMessage;
    }
}
