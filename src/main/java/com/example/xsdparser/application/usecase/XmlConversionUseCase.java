package com.example.xsdparser.application.usecase;

import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.model.XmlDocument;
import com.example.xsdparser.core.service.XmlProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Application use case for XML conversion operations.
 * Provides a high-level API for common XML processing tasks.
 */
public class XmlConversionUseCase {

    private static final Logger logger = LoggerFactory.getLogger(XmlConversionUseCase.class);

    private final XmlProcessingService processingService;

    public XmlConversionUseCase(XmlProcessingService processingService) {
        this.processingService = Objects.requireNonNull(processingService, "Processing service cannot be null");
    }

    /**
     * Converts an XML string to a Java object of the specified type.
     *
     * @param xml   the XML string
     * @param clazz the target class
     * @param <T>   the type to convert to
     * @return the converted object
     */
    public <T> T fromXml(String xml, Class<T> clazz) {
        logger.info("Converting XML to {}", clazz.getSimpleName());
        return processingService.parseXml(xml, clazz);
    }

    /**
     * Converts a Java object to XML string.
     *
     * @param object the object to convert
     * @param <T>    the type of the object
     * @return the XML string
     */
    public <T> String toXml(T object) {
        logger.info("Converting {} to XML", object.getClass().getSimpleName());
        return processingService.toXml(object);
    }

    /**
     * Converts a Java object to XML and validates against the specified schema.
     *
     * @param object      the object to convert
     * @param messageType the ISO 20022 message type (e.g., "pacs.002")
     * @param <T>         the type of the object
     * @return the conversion result containing XML and validation status
     */
    public <T> ConversionResult<T> toXmlWithValidation(T object, String messageType) {
        logger.info("Converting {} to XML with validation against {}", 
                object.getClass().getSimpleName(), messageType);
        
        String xml = processingService.toXml(object);
        ValidationResult validationResult = processingService.validateXml(xml, messageType);
        
        return new ConversionResult<>(object, xml, validationResult);
    }

    /**
     * Converts XML to a document wrapper with metadata.
     *
     * @param xml   the XML string
     * @param clazz the target class for the root element
     * @param <T>   the type of the root element
     * @return the XmlDocument containing the parsed content
     */
    public <T> XmlDocument<T> toDocument(String xml, Class<T> clazz) {
        logger.info("Converting XML to document with root type {}", clazz.getSimpleName());
        return processingService.parseToDocument(xml, clazz);
    }

    /**
     * Performs a round-trip conversion to verify serialization integrity.
     *
     * @param object the original object
     * @param clazz  the class for deserialization
     * @param <T>    the type
     * @return the result of the round-trip
     */
    public <T> RoundTripResult<T> roundTrip(T object, Class<T> clazz) {
        logger.info("Performing round-trip conversion for {}", clazz.getSimpleName());
        
        String xml = processingService.toXml(object);
        T deserialized = processingService.parseXml(xml, clazz);
        boolean matches = object.equals(deserialized);
        
        return new RoundTripResult<>(object, xml, deserialized, matches);
    }

    /**
     * Result of a conversion with validation.
     *
     * @param <T> the type of the converted object
     */
    public record ConversionResult<T>(
            T object,
            String xml,
            ValidationResult validationResult
    ) {
        public boolean isValid() {
            return validationResult.isValid();
        }
    }

    /**
     * Result of a round-trip conversion.
     *
     * @param <T> the type of the converted object
     */
    public record RoundTripResult<T>(
            T original,
            String xml,
            T deserialized,
            boolean matches
    ) {}
}
