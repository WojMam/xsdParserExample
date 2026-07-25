/**
 * Exception hierarchy for XSD Parser domain errors.
 * 
 * <p>This package provides a structured exception hierarchy for categorizing
 * and handling errors throughout the application. All exceptions extend from
 * {@link com.example.xsdparser.core.exception.XsdParserException} and include
 * an {@link com.example.xsdparser.core.exception.ErrorCode} for programmatic handling.</p>
 * 
 * <h2>Exception Hierarchy</h2>
 * <pre>
 * XsdParserException (base)
 * ├── SchemaException (schema loading/registry errors)
 * ├── XmlProcessingException (serialization/deserialization errors)
 * └── ValidationException (XML validation errors)
 * </pre>
 * 
 * @see com.example.xsdparser.core.exception.XsdParserException
 * @see com.example.xsdparser.core.exception.ErrorCode
 */
package com.example.xsdparser.core.exception;
