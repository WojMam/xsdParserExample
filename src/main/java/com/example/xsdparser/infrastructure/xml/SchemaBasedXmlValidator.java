package com.example.xsdparser.infrastructure.xml;

import com.example.xsdparser.core.model.SchemaInfo;
import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.model.ValidationResult.ErrorSeverity;
import com.example.xsdparser.core.model.ValidationResult.ValidationError;
import com.example.xsdparser.core.port.XmlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Schema-based implementation of the XmlValidator port.
 * Validates XML content against XSD schemas using the Java Validation API.
 * 
 * Thread-safe: uses cached Schema instances which are thread-safe.
 */
public class SchemaBasedXmlValidator implements XmlValidator {

    private static final Logger logger = LoggerFactory.getLogger(SchemaBasedXmlValidator.class);

    private final Map<String, Schema> schemaCache = new ConcurrentHashMap<>();
    private final SchemaFactory schemaFactory;

    public SchemaBasedXmlValidator() {
        this.schemaFactory = createSecureSchemaFactory();
    }

    private SchemaFactory createSecureSchemaFactory() {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory;
        } catch (SAXException e) {
            logger.warn("Could not set security properties on SchemaFactory", e);
            return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        }
    }

    @Override
    public ValidationResult validate(String xml, SchemaInfo schemaInfo) {
        logger.debug("Validating XML against schema: {}", schemaInfo.getFullIdentifier());
        
        if (schemaInfo.getSchemaPath().isPresent()) {
            return validateAgainstSchema(xml, schemaInfo.getSchemaPath().get());
        }
        
        String resourcePath = "xsd/" + schemaInfo.getFullIdentifier() + ".xsd";
        return validateAgainstSchema(xml, resourcePath);
    }

    @Override
    public ValidationResult validateAgainstSchema(String xml, String schemaResourcePath) {
        logger.debug("Validating XML against schema resource: {}", schemaResourcePath);
        
        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(schemaResourcePath)) {
            if (schemaStream == null) {
                return ValidationResult.invalid(
                        ValidationError.of("Schema not found: " + schemaResourcePath)
                );
            }
            
            Schema schema = getOrCreateSchema(schemaResourcePath, new StreamSource(schemaStream));
            return doValidate(xml, schema);
        } catch (IOException e) {
            logger.error("Error loading schema resource: {}", schemaResourcePath, e);
            return ValidationResult.invalid(
                    ValidationError.of("Error loading schema: " + e.getMessage())
            );
        }
    }

    @Override
    public ValidationResult validateAgainstSchema(String xml, Path schemaPath) {
        logger.debug("Validating XML against schema file: {}", schemaPath);
        
        if (!Files.exists(schemaPath)) {
            return ValidationResult.invalid(
                    ValidationError.of("Schema file not found: " + schemaPath)
            );
        }
        
        try {
            Schema schema = getOrCreateSchema(schemaPath.toString(), new StreamSource(schemaPath.toFile()));
            return doValidate(xml, schema);
        } catch (Exception e) {
            logger.error("Error loading schema file: {}", schemaPath, e);
            return ValidationResult.invalid(
                    ValidationError.of("Error loading schema: " + e.getMessage())
            );
        }
    }

    @Override
    public ValidationResult validateAgainstSchema(InputStream xmlStream, Path schemaPath) {
        logger.debug("Validating XML stream against schema file: {}", schemaPath);
        
        if (!Files.exists(schemaPath)) {
            return ValidationResult.invalid(
                    ValidationError.of("Schema file not found: " + schemaPath)
            );
        }
        
        try {
            Schema schema = getOrCreateSchema(schemaPath.toString(), new StreamSource(schemaPath.toFile()));
            return doValidate(new StreamSource(xmlStream), schema);
        } catch (Exception e) {
            logger.error("Error validating XML stream against schema: {}", schemaPath, e);
            return ValidationResult.invalid(
                    ValidationError.of("Validation error: " + e.getMessage())
            );
        }
    }

    @Override
    public boolean supportsSchema(SchemaInfo schemaInfo) {
        return schemaInfo.getNamespace() != null &&
               schemaInfo.getNamespace().startsWith("urn:iso:std:iso:20022:tech:xsd:");
    }

    private ValidationResult doValidate(String xml, Schema schema) {
        return doValidate(new StreamSource(new StringReader(xml)), schema);
    }

    private ValidationResult doValidate(Source xmlSource, Schema schema) {
        List<ValidationError> errors = new ArrayList<>();
        
        try {
            Validator validator = schema.newValidator();
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            
            validator.setErrorHandler(new CollectingErrorHandler(errors));
            validator.validate(xmlSource);
            
            if (errors.isEmpty()) {
                logger.debug("XML validation successful");
                return ValidationResult.valid();
            } else {
                logger.debug("XML validation found {} errors", errors.size());
                return ValidationResult.invalid(errors);
            }
        } catch (SAXException e) {
            logger.error("SAX exception during validation", e);
            errors.add(ValidationError.of("Fatal validation error: " + e.getMessage()));
            return ValidationResult.invalid(errors);
        } catch (IOException e) {
            logger.error("IO exception during validation", e);
            return ValidationResult.invalid(
                    ValidationError.of("Error reading XML: " + e.getMessage())
            );
        }
    }

    private synchronized Schema getOrCreateSchema(String key, Source schemaSource) {
        return schemaCache.computeIfAbsent(key, k -> {
            try {
                logger.debug("Creating Schema for: {}", key);
                return schemaFactory.newSchema(schemaSource);
            } catch (SAXException e) {
                throw new RuntimeException("Failed to create schema: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Clears the schema cache.
     */
    public void clearCache() {
        schemaCache.clear();
        logger.debug("Schema cache cleared");
    }

    /**
     * Error handler that collects all validation errors.
     */
    private static class CollectingErrorHandler implements ErrorHandler {
        private final List<ValidationError> errors;

        CollectingErrorHandler(List<ValidationError> errors) {
            this.errors = errors;
        }

        @Override
        public void warning(SAXParseException e) {
            errors.add(new ValidationError(
                    e.getMessage(),
                    e.getLineNumber(),
                    e.getColumnNumber(),
                    ErrorSeverity.WARNING
            ));
        }

        @Override
        public void error(SAXParseException e) {
            errors.add(new ValidationError(
                    e.getMessage(),
                    e.getLineNumber(),
                    e.getColumnNumber(),
                    ErrorSeverity.ERROR
            ));
        }

        @Override
        public void fatalError(SAXParseException e) {
            errors.add(new ValidationError(
                    e.getMessage(),
                    e.getLineNumber(),
                    e.getColumnNumber(),
                    ErrorSeverity.FATAL
            ));
        }
    }
}
