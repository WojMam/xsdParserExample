package com.example.xsdparser.application.usecase;

import com.example.xsdparser.core.model.SchemaInfo;
import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.service.XmlProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Application use case for schema validation operations.
 * Provides a high-level API for validating XML against ISO 20022 schemas.
 */
public class SchemaValidationUseCase {

    private static final Logger logger = LoggerFactory.getLogger(SchemaValidationUseCase.class);

    private final XmlProcessingService processingService;

    public SchemaValidationUseCase(XmlProcessingService processingService) {
        this.processingService = Objects.requireNonNull(processingService, "Processing service cannot be null");
    }

    /**
     * Validates XML content against a specific message type schema.
     *
     * @param xml         the XML content to validate
     * @param messageType the message type (e.g., "pacs.002")
     * @return the validation result
     */
    public ValidationResult validate(String xml, String messageType) {
        logger.info("Validating XML against schema: {}", messageType);
        return processingService.validateXml(xml, messageType);
    }

    /**
     * Validates XML content against a schema at the specified resource path.
     *
     * @param xml                the XML content to validate
     * @param schemaResourcePath the classpath resource path to the schema
     * @return the validation result
     */
    public ValidationResult validateAgainstResource(String xml, String schemaResourcePath) {
        logger.info("Validating XML against schema resource: {}", schemaResourcePath);
        return processingService.validateXmlAgainstSchema(xml, schemaResourcePath);
    }

    /**
     * Validates a Java object by first serializing it to XML.
     *
     * @param object      the object to validate
     * @param messageType the message type for validation
     * @param <T>         the type of the object
     * @return the validation result
     */
    public <T> ValidationResult validateObject(T object, String messageType) {
        logger.info("Validating object of type {} against schema: {}", 
                object.getClass().getSimpleName(), messageType);
        return processingService.serializeAndValidate(object, messageType);
    }

    /**
     * Gets schema information for a message type.
     *
     * @param messageType the message type to look up
     * @return the schema info if found
     */
    public Optional<SchemaInfo> getSchemaInfo(String messageType) {
        return processingService.getSchemaRegistry().findByMessageType(messageType);
    }

    /**
     * Lists all available schemas.
     *
     * @return collection of all registered schemas
     */
    public Collection<SchemaInfo> listAvailableSchemas() {
        return processingService.getSchemaRegistry().getAllSchemas();
    }

    /**
     * Checks if a schema is available for validation.
     *
     * @param messageType the message type to check
     * @return true if the schema is available
     */
    public boolean isSchemaAvailable(String messageType) {
        return processingService.getSchemaRegistry().findByMessageType(messageType).isPresent();
    }

    /**
     * Validates XML and provides a detailed report.
     *
     * @param xml         the XML to validate
     * @param messageType the message type
     * @return a detailed validation report
     */
    public ValidationReport validateWithReport(String xml, String messageType) {
        logger.info("Generating validation report for message type: {}", messageType);
        
        Optional<SchemaInfo> schemaInfoOpt = getSchemaInfo(messageType);
        ValidationResult result = validate(xml, messageType);
        
        return new ValidationReport(
                messageType,
                schemaInfoOpt.orElse(null),
                result,
                xml.length()
        );
    }

    /**
     * Detailed validation report with schema information.
     */
    public record ValidationReport(
            String messageType,
            SchemaInfo schemaInfo,
            ValidationResult validationResult,
            int xmlLength
    ) {
        public boolean isValid() {
            return validationResult.isValid();
        }

        public boolean schemaFound() {
            return schemaInfo != null;
        }

        public String getSummary() {
            if (!schemaFound()) {
                return "Schema not found for message type: " + messageType;
            }
            if (isValid()) {
                return "Valid " + messageType + " document (" + xmlLength + " bytes)";
            }
            return "Invalid " + messageType + " document: " + validationResult.getErrorSummary();
        }
    }
}
