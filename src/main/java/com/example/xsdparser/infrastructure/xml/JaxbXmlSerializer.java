package com.example.xsdparser.infrastructure.xml;

import com.example.xsdparser.core.model.XmlDocument;
import com.example.xsdparser.core.port.XmlSerializer;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JAXB-based implementation of the XmlSerializer port.
 * Handles XML serialization and deserialization using Jakarta XML Binding.
 * 
 * Thread-safe: uses cached JAXBContext instances which are thread-safe.
 */
public class JaxbXmlSerializer implements XmlSerializer {

    private static final Logger logger = LoggerFactory.getLogger(JaxbXmlSerializer.class);
    
    private final Map<Class<?>, JAXBContext> contextCache = new ConcurrentHashMap<>();

    @Override
    public <T> String serialize(T object) throws XmlSerializationException {
        return doSerialize(object, false);
    }

    @Override
    public <T> String serializeFormatted(T object) throws XmlSerializationException {
        return doSerialize(object, true);
    }

    @Override
    public <T> String serialize(XmlDocument<T> document) throws XmlSerializationException {
        return serialize(document.getRootElement());
    }

    @Override
    public <T> T deserialize(String xml, Class<T> clazz) throws XmlSerializationException {
        try {
            logger.debug("Deserializing XML to type: {}", clazz.getSimpleName());
            JAXBContext context = getOrCreateContext(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            @SuppressWarnings("unchecked")
            T result = (T) unmarshaller.unmarshal(new StringReader(xml));
            return result;
        } catch (JAXBException e) {
            logger.error("Failed to deserialize XML to type: {}", clazz.getSimpleName(), e);
            throw new XmlSerializationException("Failed to deserialize XML: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> XmlDocument<T> deserializeToDocument(String xml, Class<T> clazz) throws XmlSerializationException {
        T rootElement = deserialize(xml, clazz);
        String namespace = extractNamespace(xml);
        return XmlDocument.of(rootElement, namespace, clazz);
    }

    private <T> String doSerialize(T object, boolean formatted) throws XmlSerializationException {
        try {
            Class<?> contextClass = getContextClass(object);
            logger.debug("Serializing object of type: {} (formatted: {})", 
                    contextClass.getSimpleName(), formatted);
            
            JAXBContext context = getOrCreateContext(contextClass);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            logger.error("Failed to serialize object of type: {}", object.getClass().getSimpleName(), e);
            throw new XmlSerializationException("Failed to serialize object: " + e.getMessage(), e);
        }
    }

    private Class<?> getContextClass(Object object) {
        if (object instanceof JAXBElement<?> jaxbElement) {
            Class<?> declaredType = jaxbElement.getDeclaredType();
            String packageName = declaredType.getPackageName();
            try {
                return Class.forName(packageName + ".ObjectFactory");
            } catch (ClassNotFoundException e) {
                logger.debug("ObjectFactory not found for package {}, using declared type", packageName);
                return declaredType;
            }
        }
        return object.getClass();
    }

    private JAXBContext getOrCreateContext(Class<?> clazz) throws JAXBException {
        return contextCache.computeIfAbsent(clazz, key -> {
            try {
                logger.debug("Creating JAXBContext for type: {}", key.getSimpleName());
                return JAXBContext.newInstance(key);
            } catch (JAXBException e) {
                throw new RuntimeException("Failed to create JAXBContext for: " + key.getSimpleName(), e);
            }
        });
    }

    private String extractNamespace(String xml) {
        int nsStart = xml.indexOf("xmlns=");
        if (nsStart == -1) {
            nsStart = xml.indexOf("xmlns:");
        }
        if (nsStart == -1) {
            return null;
        }
        
        int quoteStart = xml.indexOf('"', nsStart);
        if (quoteStart == -1) {
            quoteStart = xml.indexOf('\'', nsStart);
        }
        if (quoteStart == -1) {
            return null;
        }
        
        char quoteChar = xml.charAt(quoteStart);
        int quoteEnd = xml.indexOf(quoteChar, quoteStart + 1);
        if (quoteEnd == -1) {
            return null;
        }
        
        return xml.substring(quoteStart + 1, quoteEnd);
    }

    /**
     * Clears the internal JAXB context cache.
     * Useful for testing or when class definitions change at runtime.
     */
    public void clearCache() {
        contextCache.clear();
        logger.debug("JAXBContext cache cleared");
    }

    /**
     * Pre-loads a JAXB context for the specified class.
     * Useful for warming up the cache during application startup.
     *
     * @param clazz the class to pre-load context for
     */
    public void preloadContext(Class<?> clazz) {
        try {
            getOrCreateContext(clazz);
            logger.info("Preloaded JAXBContext for: {}", clazz.getSimpleName());
        } catch (JAXBException e) {
            logger.warn("Failed to preload JAXBContext for: {}", clazz.getSimpleName(), e);
        }
    }
}
