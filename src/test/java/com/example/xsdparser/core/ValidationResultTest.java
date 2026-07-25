package com.example.xsdparser.core;

import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.model.ValidationResult.ErrorSeverity;
import com.example.xsdparser.core.model.ValidationResult.ValidationError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the ValidationResult value object.
 */
@DisplayName("ValidationResult Tests")
class ValidationResultTest {

    @Test
    @DisplayName("should create valid result")
    void shouldCreateValidResult() {
        ValidationResult result = ValidationResult.valid();
        
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getErrorSummary()).isEqualTo("No errors");
    }

    @Test
    @DisplayName("should create invalid result with single error")
    void shouldCreateInvalidResultWithSingleError() {
        ValidationError error = ValidationError.of("Test error message");
        
        ValidationResult result = ValidationResult.invalid(error);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).isEqualTo("Test error message");
    }

    @Test
    @DisplayName("should create invalid result with multiple errors")
    void shouldCreateInvalidResultWithMultipleErrors() {
        List<ValidationError> errors = List.of(
                ValidationError.of("Error 1"),
                ValidationError.of("Error 2"),
                ValidationError.of("Error 3")
        );
        
        ValidationResult result = ValidationResult.invalid(errors);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(3);
        assertThat(result.getErrorSummary()).contains("Error 1", "Error 2", "Error 3");
    }

    @Test
    @DisplayName("should create validation error with line and column info")
    void shouldCreateValidationErrorWithLineInfo() {
        ValidationError error = ValidationError.of("Parse error at element", 10, 25);
        
        assertThat(error.getMessage()).isEqualTo("Parse error at element");
        assertThat(error.lineNumber()).isEqualTo(10);
        assertThat(error.columnNumber()).isEqualTo(25);
        assertThat(error.severity()).isEqualTo(ErrorSeverity.ERROR);
    }

    @Test
    @DisplayName("should create validation error with custom severity")
    void shouldCreateValidationErrorWithCustomSeverity() {
        ValidationError error = new ValidationError("Warning message", 5, 10, ErrorSeverity.WARNING);
        
        assertThat(error.severity()).isEqualTo(ErrorSeverity.WARNING);
    }

    @Test
    @DisplayName("should throw exception when creating invalid result with empty errors")
    void shouldThrowExceptionWhenInvalidWithEmptyErrors() {
        assertThatThrownBy(() -> ValidationResult.invalid(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must have at least one error");
    }

    @Test
    @DisplayName("should throw exception when creating error with null message")
    void shouldThrowExceptionWhenErrorHasNullMessage() {
        assertThatThrownBy(() -> ValidationError.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should support error severity levels")
    void shouldSupportErrorSeverityLevels() {
        assertThat(ErrorSeverity.values())
                .containsExactly(ErrorSeverity.WARNING, ErrorSeverity.ERROR, ErrorSeverity.FATAL);
    }
}
