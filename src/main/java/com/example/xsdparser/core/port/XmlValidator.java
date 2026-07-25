package com.example.xsdparser.core.port;

import com.example.xsdparser.core.model.SchemaInfo;
import com.example.xsdparser.core.model.ValidationResult;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Port interface for XML validation operations against XSD schemas.
 * Defines the contract for validating XML content.
 */
public interface XmlValidator {

    /**
     * Validates an XML string against the schema identified by the schema info.
     *
     * @param xml        the XML string to validate
     * @param schemaInfo the schema information for validation
     * @return the validation result containing status and any errors
     */
    ValidationResult validate(String xml, SchemaInfo schemaInfo);

    /**
     * Validates an XML string against a schema loaded from classpath.
     *
     * @param xml                the XML string to validate
     * @param schemaResourcePath the classpath path to the XSD schema
     * @return the validation result containing status and any errors
     */
    ValidationResult validateAgainstSchema(String xml, String schemaResourcePath);

    /**
     * Validates an XML string against a schema file.
     *
     * @param xml        the XML string to validate
     * @param schemaPath the file system path to the XSD schema
     * @return the validation result containing status and any errors
     */
    ValidationResult validateAgainstSchema(String xml, Path schemaPath);

    /**
     * Validates an XML input stream against a schema.
     *
     * @param xmlStream  the XML input stream to validate
     * @param schemaPath the file system path to the XSD schema
     * @return the validation result containing status and any errors
     */
    ValidationResult validateAgainstSchema(InputStream xmlStream, Path schemaPath);

    /**
     * Checks if the validator can handle the specified schema.
     *
     * @param schemaInfo the schema to check
     * @return true if this validator supports the schema
     */
    boolean supportsSchema(SchemaInfo schemaInfo);
}
