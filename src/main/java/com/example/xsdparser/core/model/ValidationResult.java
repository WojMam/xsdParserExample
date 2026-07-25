package com.example.xsdparser.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value object representing the result of XML validation against a schema.
 * Contains information about validation status and any errors encountered.
 */
public final class ValidationResult {

    private final boolean valid;
    private final List<ValidationError> errors;

    private ValidationResult(boolean valid, List<ValidationError> errors) {
        this.valid = valid;
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public static ValidationResult valid() {
        return new ValidationResult(true, Collections.emptyList());
    }

    public static ValidationResult invalid(List<ValidationError> errors) {
        Objects.requireNonNull(errors, "Errors list cannot be null");
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid result must have at least one error");
        }
        return new ValidationResult(false, errors);
    }

    public static ValidationResult invalid(ValidationError error) {
        Objects.requireNonNull(error, "Error cannot be null");
        return new ValidationResult(false, List.of(error));
    }

    public boolean isValid() {
        return valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public String getErrorSummary() {
        if (errors.isEmpty()) {
            return "No errors";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) sb.append("; ");
            sb.append(errors.get(i).getMessage());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return valid == that.valid && Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, errors);
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
               "valid=" + valid +
               ", errors=" + errors +
               '}';
    }

    /**
     * Represents a single validation error with location information.
     */
    public record ValidationError(
            String message,
            int lineNumber,
            int columnNumber,
            ErrorSeverity severity
    ) {
        public ValidationError {
            Objects.requireNonNull(message, "Message cannot be null");
            Objects.requireNonNull(severity, "Severity cannot be null");
        }

        public static ValidationError of(String message) {
            return new ValidationError(message, -1, -1, ErrorSeverity.ERROR);
        }

        public static ValidationError of(String message, int lineNumber, int columnNumber) {
            return new ValidationError(message, lineNumber, columnNumber, ErrorSeverity.ERROR);
        }

        public String getMessage() {
            return message;
        }
    }

    public enum ErrorSeverity {
        WARNING,
        ERROR,
        FATAL
    }
}
