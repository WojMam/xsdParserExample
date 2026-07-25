package com.example.xsdparser.application;

import com.example.xsdparser.XsdParserConfig;
import com.example.xsdparser.application.usecase.SchemaValidationUseCase;
import com.example.xsdparser.core.model.SchemaInfo;
import com.example.xsdparser.core.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SchemaValidationUseCase.
 * Demonstrates how to validate XML against ISO 20022 schemas.
 */
@DisplayName("SchemaValidationUseCase Tests")
class SchemaValidationUseCaseTest {

    private SchemaValidationUseCase validationUseCase;

    @BeforeEach
    void setUp() {
        XsdParserConfig config = XsdParserConfig.createIsolated();
        validationUseCase = config.getValidationUseCase();
    }

    @Test
    @DisplayName("should list all available schemas")
    void shouldListAllAvailableSchemas() {
        Collection<SchemaInfo> schemas = validationUseCase.listAvailableSchemas();
        
        assertThat(schemas)
                .isNotEmpty()
                .hasSizeGreaterThanOrEqualTo(9);
        
        assertThat(schemas)
                .extracting(SchemaInfo::getMessageType)
                .contains("pacs.002", "pacs.008", "pacs.009");
    }

    @Test
    @DisplayName("should check schema availability")
    void shouldCheckSchemaAvailability() {
        assertThat(validationUseCase.isSchemaAvailable("pacs.002")).isTrue();
        assertThat(validationUseCase.isSchemaAvailable("pacs.008")).isTrue();
        assertThat(validationUseCase.isSchemaAvailable("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("should get schema info by message type")
    void shouldGetSchemaInfoByMessageType() {
        Optional<SchemaInfo> schemaInfo = validationUseCase.getSchemaInfo("pacs.002");
        
        assertThat(schemaInfo).isPresent();
        assertThat(schemaInfo.get().getMessageType()).isEqualTo("pacs.002");
        assertThat(schemaInfo.get().getNamespace()).contains("pacs.002");
    }

    @Test
    @DisplayName("should return invalid result for non-existent schema")
    void shouldReturnInvalidResultForNonExistentSchema() {
        String xml = "<test>content</test>";
        
        ValidationResult result = validationUseCase.validate(xml, "nonexistent");
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorSummary()).contains("not found");
    }

    @Test
    @DisplayName("should generate validation report")
    void shouldGenerateValidationReport() {
        String xml = "<test>content</test>";
        
        SchemaValidationUseCase.ValidationReport report = 
                validationUseCase.validateWithReport(xml, "pacs.002");
        
        assertThat(report.messageType()).isEqualTo("pacs.002");
        assertThat(report.schemaFound()).isTrue();
        assertThat(report.xmlLength()).isGreaterThan(0);
    }

    @Test
    @DisplayName("should provide summary in validation report")
    void shouldProvideSummaryInValidationReport() {
        String xml = "<test>content</test>";
        
        SchemaValidationUseCase.ValidationReport report = 
                validationUseCase.validateWithReport(xml, "nonexistent.schema");
        
        assertThat(report.getSummary()).contains("not found");
    }

    @Test
    @DisplayName("schema info should contain description")
    void schemaInfoShouldContainDescription() {
        Optional<SchemaInfo> schemaInfo = validationUseCase.getSchemaInfo("pacs.002");
        
        assertThat(schemaInfo).isPresent();
        assertThat(schemaInfo.get().getDescription())
                .isPresent()
                .hasValueSatisfying(desc -> assertThat(desc).isNotBlank());
    }
}
