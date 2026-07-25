package com.example.xsdparser.core.exception;

/**
 * Exception thrown when schema-related operations fail.
 * This includes schema loading, validation, and registry operations.
 */
public class SchemaException extends XsdParserException {

    private final String schemaIdentifier;

    public SchemaException(String message) {
        super(ErrorCode.SCHEMA_NOT_FOUND, message);
        this.schemaIdentifier = null;
    }

    public SchemaException(String message, Throwable cause) {
        super(ErrorCode.SCHEMA_NOT_FOUND, message, cause);
        this.schemaIdentifier = null;
    }

    public SchemaException(ErrorCode errorCode, String message, String schemaIdentifier) {
        super(errorCode, message);
        this.schemaIdentifier = schemaIdentifier;
    }

    public SchemaException(ErrorCode errorCode, String message, String schemaIdentifier, Throwable cause) {
        super(errorCode, message, cause);
        this.schemaIdentifier = schemaIdentifier;
    }

    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    public static SchemaException notFound(String schemaIdentifier) {
        return new SchemaException(
                ErrorCode.SCHEMA_NOT_FOUND,
                String.format("Schema not found: '%s'", schemaIdentifier),
                schemaIdentifier
        );
    }

    public static SchemaException loadError(String schemaIdentifier, Throwable cause) {
        return new SchemaException(
                ErrorCode.SCHEMA_LOAD_ERROR,
                String.format("Failed to load schema: '%s' - %s", schemaIdentifier, cause.getMessage()),
                schemaIdentifier,
                cause
        );
    }

    public static SchemaException alreadyRegistered(String schemaIdentifier) {
        return new SchemaException(
                ErrorCode.SCHEMA_ALREADY_REGISTERED,
                String.format("Schema already registered: '%s'", schemaIdentifier),
                schemaIdentifier
        );
    }
}
