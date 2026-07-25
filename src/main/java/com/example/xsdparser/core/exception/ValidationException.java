package com.example.xsdparser.core.exception;

import com.example.xsdparser.core.model.ValidationResult;

import java.util.List;

/**
 * Exception thrown when XML validation fails.
 * Contains detailed information about validation errors.
 */
public class ValidationException extends XsdParserException {

    private final ValidationResult validationResult;
    private final String schemaIdentifier;

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.validationResult = null;
        this.schemaIdentifier = null;
    }

    public ValidationException(String message, Throwable cause) {
        super(ErrorCode.VALIDATION_ERROR, message, cause);
        this.validationResult = null;
        this.schemaIdentifier = null;
    }

    public ValidationException(ErrorCode errorCode, String message, ValidationResult validationResult, 
                              String schemaIdentifier) {
        super(errorCode, message);
        this.validationResult = validationResult;
        this.schemaIdentifier = schemaIdentifier;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    public List<ValidationResult.ValidationError> getErrors() {
        return validationResult != null ? validationResult.getErrors() : List.of();
    }

    public static ValidationException validationFailed(ValidationResult result, String schemaIdentifier) {
        String errorSummary = result != null ? result.getErrorSummary() : "Unknown errors";
        return new ValidationException(
                ErrorCode.VALIDATION_ERROR,
                String.format("XML validation failed against schema '%s': %s", schemaIdentifier, errorSummary),
                result,
                schemaIdentifier
        );
    }

    public static ValidationException invalidXmlStructure(String details) {
        return new ValidationException(
                ErrorCode.INVALID_XML_STRUCTURE,
                String.format("Invalid XML structure: %s", details),
                null,
                null
        );
    }
}
