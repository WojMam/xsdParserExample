package com.example.xsdparser.core.port;

import com.example.xsdparser.core.model.XmlDocument;

/**
 * Port interface for XML serialization operations.
 * Defines the contract for converting between Java objects and XML strings.
 * 
 * Implementations should handle JAXB-generated classes from XSD schemas.
 */
public interface XmlSerializer {

    /**
     * Serializes a Java object to an XML string.
     *
     * @param object the object to serialize
     * @param <T>    the type of the object
     * @return the XML string representation
     * @throws XmlSerializationException if serialization fails
     */
    <T> String serialize(T object) throws XmlSerializationException;

    /**
     * Serializes a Java object to a formatted (pretty-printed) XML string.
     *
     * @param object the object to serialize
     * @param <T>    the type of the object
     * @return the formatted XML string representation
     * @throws XmlSerializationException if serialization fails
     */
    <T> String serializeFormatted(T object) throws XmlSerializationException;

    /**
     * Serializes an XmlDocument to an XML string with proper namespace handling.
     *
     * @param document the document to serialize
     * @param <T>      the type of the root element
     * @return the XML string representation
     * @throws XmlSerializationException if serialization fails
     */
    <T> String serialize(XmlDocument<T> document) throws XmlSerializationException;

    /**
     * Deserializes an XML string to a Java object of the specified type.
     *
     * @param xml   the XML string to deserialize
     * @param clazz the target class type
     * @param <T>   the type to deserialize to
     * @return the deserialized object
     * @throws XmlSerializationException if deserialization fails
     */
    <T> T deserialize(String xml, Class<T> clazz) throws XmlSerializationException;

    /**
     * Deserializes an XML string to an XmlDocument containing the root element.
     *
     * @param xml   the XML string to deserialize
     * @param clazz the target class type for the root element
     * @param <T>   the type of the root element
     * @return the XmlDocument containing the deserialized object
     * @throws XmlSerializationException if deserialization fails
     */
    <T> XmlDocument<T> deserializeToDocument(String xml, Class<T> clazz) throws XmlSerializationException;

    /**
     * Exception thrown when XML serialization or deserialization fails.
     */
    class XmlSerializationException extends RuntimeException {
        public XmlSerializationException(String message) {
            super(message);
        }

        public XmlSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
