package com.example.xsdparser.core.exception;

/**
 * Base exception class for all XSD Parser related exceptions.
 * Provides a common exception hierarchy for the application.
 */
public class XsdParserException extends RuntimeException {

    private final ErrorCode errorCode;

    public XsdParserException(String message) {
        super(message);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }

    public XsdParserException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }

    public XsdParserException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public XsdParserException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]: %s", getClass().getSimpleName(), errorCode, getMessage());
    }
}
