package com.example.xsdparser.core.service;

import com.example.xsdparser.core.model.SchemaInfo;
import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.model.XmlDocument;
import com.example.xsdparser.core.port.SchemaRegistry;
import com.example.xsdparser.core.port.XmlSerializer;
import com.example.xsdparser.core.port.XmlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Core domain service for XML processing operations.
 * Orchestrates serialization, deserialization, and validation using the injected ports.
 * 
 * This service follows the Dependency Inversion Principle - it depends on abstractions
 * (ports) rather than concrete implementations.
 */
public class XmlProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(XmlProcessingService.class);

    private final XmlSerializer serializer;
    private final XmlValidator validator;
    private final SchemaRegistry schemaRegistry;

    public XmlProcessingService(XmlSerializer serializer, XmlValidator validator, SchemaRegistry schemaRegistry) {
        this.serializer = Objects.requireNonNull(serializer, "Serializer cannot be null");
        this.validator = Objects.requireNonNull(validator, "Validator cannot be null");
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry, "SchemaRegistry cannot be null");
    }

    /**
     * Converts a Java object to XML and validates it against the appropriate schema.
     *
     * @param object      the object to convert
     * @param messageType the ISO 20022 message type (e.g., "pacs.002")
     * @param <T>         the type of the object
     * @return the validation result
     */
    public <T> ValidationResult serializeAndValidate(T object, String messageType) {
        logger.debug("Serializing and validating object of type {} against schema {}", 
                object.getClass().getSimpleName(), messageType);

        Optional<SchemaInfo> schemaInfoOpt = schemaRegistry.findByMessageType(messageType);
        if (schemaInfoOpt.isEmpty()) {
            String availableSchemas = schemaRegistry.getAllSchemas().stream()
                    .map(s -> s.getMessageType())
                    .sorted()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("none");
            logger.warn("No schema found for message type '{}'. Available schemas: [{}]", messageType, availableSchemas);
            return ValidationResult.invalid(
                    ValidationResult.ValidationError.of(
                            String.format("Schema not found for message type '%s'. Available schemas: [%s]", 
                                    messageType, availableSchemas))
            );
        }

        String xml = serializer.serialize(object);
        return validator.validate(xml, schemaInfoOpt.get());
    }

    /**
     * Parses XML and deserializes it to the specified type.
     *
     * @param xml   the XML string
     * @param clazz the target class
     * @param <T>   the type to deserialize to
     * @return the deserialized object
     */
    public <T> T parseXml(String xml, Class<T> clazz) {
        logger.debug("Parsing XML to type: {}", clazz.getSimpleName());
        return serializer.deserialize(xml, clazz);
    }

    /**
     * Parses XML to an XmlDocument.
     *
     * @param xml   the XML string
     * @param clazz the target class for the root element
     * @param <T>   the type of the root element
     * @return the XmlDocument containing the parsed content
     */
    public <T> XmlDocument<T> parseToDocument(String xml, Class<T> clazz) {
        logger.debug("Parsing XML to document with root type: {}", clazz.getSimpleName());
        return serializer.deserializeToDocument(xml, clazz);
    }

    /**
     * Serializes an object to formatted XML.
     *
     * @param object the object to serialize
     * @param <T>    the type of the object
     * @return the formatted XML string
     */
    public <T> String toXml(T object) {
        logger.debug("Converting object to XML: {}", object.getClass().getSimpleName());
        return serializer.serializeFormatted(object);
    }

    /**
     * Validates XML content against a schema by message type.
     *
     * @param xml         the XML to validate
     * @param messageType the message type identifier
     * @return the validation result
     */
    public ValidationResult validateXml(String xml, String messageType) {
        logger.debug("Validating XML against schema: {}", messageType);

        Optional<SchemaInfo> schemaInfoOpt = schemaRegistry.findByMessageType(messageType);
        if (schemaInfoOpt.isEmpty()) {
            String availableSchemas = schemaRegistry.getAllSchemas().stream()
                    .map(s -> s.getMessageType())
                    .sorted()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("none");
            logger.warn("No schema found for message type '{}'. Available schemas: [{}]", messageType, availableSchemas);
            return ValidationResult.invalid(
                    ValidationResult.ValidationError.of(
                            String.format("Schema not found for message type '%s'. Available schemas: [%s]", 
                                    messageType, availableSchemas))
            );
        }

        return validator.validate(xml, schemaInfoOpt.get());
    }

    /**
     * Validates XML content directly against a schema resource path.
     *
     * @param xml                the XML to validate
     * @param schemaResourcePath the classpath resource path to the schema
     * @return the validation result
     */
    public ValidationResult validateXmlAgainstSchema(String xml, String schemaResourcePath) {
        logger.debug("Validating XML against schema resource: {}", schemaResourcePath);
        return validator.validateAgainstSchema(xml, schemaResourcePath);
    }

    /**
     * Performs a round-trip conversion: serialize object to XML, then deserialize back.
     * Useful for testing serialization consistency.
     *
     * @param object the object to convert
     * @param clazz  the target class for deserialization
     * @param <T>    the type of the object
     * @return the deserialized object
     */
    public <T> T roundTrip(T object, Class<T> clazz) {
        logger.debug("Performing round-trip conversion for: {}", clazz.getSimpleName());
        String xml = serializer.serialize(object);
        return serializer.deserialize(xml, clazz);
    }

    /**
     * Gets the schema registry for direct schema operations.
     *
     * @return the schema registry
     */
    public SchemaRegistry getSchemaRegistry() {
        return schemaRegistry;
    }
}
