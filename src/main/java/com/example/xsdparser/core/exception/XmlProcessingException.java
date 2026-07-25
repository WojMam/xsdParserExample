package com.example.xsdparser.core.exception;

/**
 * Exception thrown when XML processing operations fail.
 * This includes serialization, deserialization, and transformation operations.
 */
public class XmlProcessingException extends XsdParserException {

    private final String xmlSnippet;
    private final Class<?> targetType;

    public XmlProcessingException(String message) {
        super(ErrorCode.SERIALIZATION_ERROR, message);
        this.xmlSnippet = null;
        this.targetType = null;
    }

    public XmlProcessingException(String message, Throwable cause) {
        super(ErrorCode.SERIALIZATION_ERROR, message, cause);
        this.xmlSnippet = null;
        this.targetType = null;
    }

    public XmlProcessingException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.xmlSnippet = null;
        this.targetType = null;
    }

    private XmlProcessingException(ErrorCode errorCode, String message, Throwable cause, 
                                   String xmlSnippet, Class<?> targetType) {
        super(errorCode, message, cause);
        this.xmlSnippet = xmlSnippet;
        this.targetType = targetType;
    }

    public String getXmlSnippet() {
        return xmlSnippet;
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public static XmlProcessingException serializationFailed(Object object, Throwable cause) {
        String typeName = object != null ? object.getClass().getSimpleName() : "null";
        return new XmlProcessingException(
                ErrorCode.SERIALIZATION_ERROR,
                String.format("Failed to serialize object of type '%s': %s", typeName, cause.getMessage()),
                cause,
                null,
                object != null ? object.getClass() : null
        );
    }

    public static XmlProcessingException deserializationFailed(String xml, Class<?> targetType, Throwable cause) {
        String snippet = xml != null && xml.length() > 100 ? xml.substring(0, 100) + "..." : xml;
        return new XmlProcessingException(
                ErrorCode.DESERIALIZATION_ERROR,
                String.format("Failed to deserialize XML to type '%s': %s", 
                        targetType != null ? targetType.getSimpleName() : "null", 
                        cause.getMessage()),
                cause,
                snippet,
                targetType
        );
    }

    public static XmlProcessingException marshallingFailed(Object object, Throwable cause) {
        String typeName = object != null ? object.getClass().getSimpleName() : "null";
        return new XmlProcessingException(
                ErrorCode.MARSHALLING_ERROR,
                String.format("JAXB marshalling failed for type '%s': %s", typeName, cause.getMessage()),
                cause,
                null,
                object != null ? object.getClass() : null
        );
    }

    public static XmlProcessingException unmarshallingFailed(Class<?> targetType, Throwable cause) {
        return new XmlProcessingException(
                ErrorCode.UNMARSHALLING_ERROR,
                String.format("JAXB unmarshalling failed for type '%s': %s", 
                        targetType != null ? targetType.getSimpleName() : "null", 
                        cause.getMessage()),
                cause,
                null,
                targetType
        );
    }
}
