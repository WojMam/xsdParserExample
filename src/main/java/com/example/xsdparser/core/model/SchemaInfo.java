package com.example.xsdparser.core.model;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Value object containing metadata about an XSD schema file.
 * Provides information for identifying and working with ISO 20022 PACS message schemas.
 */
public final class SchemaInfo {

    private final String messageType;
    private final String version;
    private final String namespace;
    private final Path schemaPath;
    private final String description;

    private SchemaInfo(Builder builder) {
        this.messageType = Objects.requireNonNull(builder.messageType, "Message type cannot be null");
        this.version = Objects.requireNonNull(builder.version, "Version cannot be null");
        this.namespace = Objects.requireNonNull(builder.namespace, "Namespace cannot be null");
        this.schemaPath = builder.schemaPath;
        this.description = builder.description;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getMessageType() {
        return messageType;
    }

    public String getVersion() {
        return version;
    }

    public String getNamespace() {
        return namespace;
    }

    public Optional<Path> getSchemaPath() {
        return Optional.ofNullable(schemaPath);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    /**
     * Returns the full schema identifier (e.g., "pacs.002.001.16").
     */
    public String getFullIdentifier() {
        return messageType + "." + version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaInfo that = (SchemaInfo) o;
        return Objects.equals(messageType, that.messageType) &&
               Objects.equals(version, that.version) &&
               Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType, version, namespace);
    }

    @Override
    public String toString() {
        return "SchemaInfo{" +
               "messageType='" + messageType + '\'' +
               ", version='" + version + '\'' +
               ", namespace='" + namespace + '\'' +
               ", description='" + description + '\'' +
               '}';
    }

    public static final class Builder {
        private String messageType;
        private String version;
        private String namespace;
        private Path schemaPath;
        private String description;

        private Builder() {}

        public Builder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder schemaPath(Path schemaPath) {
            this.schemaPath = schemaPath;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public SchemaInfo build() {
            return new SchemaInfo(this);
        }
    }
}
