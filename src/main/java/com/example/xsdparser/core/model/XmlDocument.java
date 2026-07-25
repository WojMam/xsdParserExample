package com.example.xsdparser.core.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Value object representing a parsed XML document with its content and metadata.
 * This is a domain model used throughout the application independent of specific
 * XML implementation details.
 *
 * @param <T> the type of the root element
 */
public final class XmlDocument<T> {

    private final T rootElement;
    private final String namespace;
    private final String schemaLocation;
    private final Class<T> rootType;

    private XmlDocument(T rootElement, String namespace, String schemaLocation, Class<T> rootType) {
        this.rootElement = Objects.requireNonNull(rootElement, "Root element cannot be null");
        this.namespace = namespace;
        this.schemaLocation = schemaLocation;
        this.rootType = Objects.requireNonNull(rootType, "Root type cannot be null");
    }

    public static <T> XmlDocument<T> of(T rootElement, Class<T> rootType) {
        return new XmlDocument<>(rootElement, null, null, rootType);
    }

    public static <T> XmlDocument<T> of(T rootElement, String namespace, Class<T> rootType) {
        return new XmlDocument<>(rootElement, namespace, null, rootType);
    }

    public static <T> XmlDocument<T> of(T rootElement, String namespace, String schemaLocation, Class<T> rootType) {
        return new XmlDocument<>(rootElement, namespace, schemaLocation, rootType);
    }

    public T getRootElement() {
        return rootElement;
    }

    public Optional<String> getNamespace() {
        return Optional.ofNullable(namespace);
    }

    public Optional<String> getSchemaLocation() {
        return Optional.ofNullable(schemaLocation);
    }

    public Class<T> getRootType() {
        return rootType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XmlDocument<?> that = (XmlDocument<?>) o;
        return Objects.equals(rootElement, that.rootElement) &&
               Objects.equals(namespace, that.namespace) &&
               Objects.equals(schemaLocation, that.schemaLocation) &&
               Objects.equals(rootType, that.rootType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootElement, namespace, schemaLocation, rootType);
    }

    @Override
    public String toString() {
        return "XmlDocument{" +
               "rootType=" + rootType.getSimpleName() +
               ", namespace='" + namespace + '\'' +
               ", schemaLocation='" + schemaLocation + '\'' +
               '}';
    }
}
