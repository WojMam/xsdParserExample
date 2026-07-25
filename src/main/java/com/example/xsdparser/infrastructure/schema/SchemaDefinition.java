package com.example.xsdparser.infrastructure.schema;

import java.util.Objects;

/**
 * Definition for an XSD schema that can be registered in the schema registry.
 * Used for configuring schemas programmatically or via external configuration.
 */
public final class SchemaDefinition {

    private final String messageType;
    private final String version;
    private final String description;
    private final String namespacePrefix;

    private SchemaDefinition(Builder builder) {
        this.messageType = Objects.requireNonNull(builder.messageType, "Message type is required");
        this.version = Objects.requireNonNull(builder.version, "Version is required");
        this.description = builder.description;
        this.namespacePrefix = builder.namespacePrefix != null 
                ? builder.namespacePrefix 
                : "urn:iso:std:iso:20022:tech:xsd:";
    }

    public String getMessageType() {
        return messageType;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public String getFullIdentifier() {
        return messageType + "." + version;
    }

    public String getNamespace() {
        return namespacePrefix + getFullIdentifier();
    }

    public String getSchemaResourcePath() {
        return "xsd/" + getFullIdentifier() + ".xsd";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder pacs(String number, String version) {
        return new Builder()
                .messageType("pacs." + number)
                .version(version);
    }

    public static Builder pain(String number, String version) {
        return new Builder()
                .messageType("pain." + number)
                .version(version);
    }

    public static Builder camt(String number, String version) {
        return new Builder()
                .messageType("camt." + number)
                .version(version);
    }

    public static class Builder {
        private String messageType;
        private String version;
        private String description;
        private String namespacePrefix;

        public Builder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder namespacePrefix(String namespacePrefix) {
            this.namespacePrefix = namespacePrefix;
            return this;
        }

        public SchemaDefinition build() {
            return new SchemaDefinition(this);
        }
    }

    @Override
    public String toString() {
        return "SchemaDefinition{" + getFullIdentifier() + "}";
    }
}
