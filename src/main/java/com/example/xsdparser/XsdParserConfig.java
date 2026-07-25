package com.example.xsdparser;

import com.example.xsdparser.application.factory.TestDataFactory;
import com.example.xsdparser.application.usecase.SchemaValidationUseCase;
import com.example.xsdparser.application.usecase.XmlConversionUseCase;
import com.example.xsdparser.core.port.SchemaRegistry;
import com.example.xsdparser.core.port.XmlSerializer;
import com.example.xsdparser.core.port.XmlValidator;
import com.example.xsdparser.core.service.XmlProcessingService;
import com.example.xsdparser.infrastructure.schema.FlexibleSchemaInitializer;
import com.example.xsdparser.infrastructure.schema.InMemorySchemaRegistry;
import com.example.xsdparser.infrastructure.schema.PacsSchemaInitializer;
import com.example.xsdparser.infrastructure.schema.SchemaDefinition;
import com.example.xsdparser.infrastructure.xml.JaxbXmlSerializer;
import com.example.xsdparser.infrastructure.xml.SchemaBasedXmlValidator;

import java.util.Collection;
import java.util.List;

/**
 * Configuration class for setting up the XSD Parser components.
 * Provides factory methods for creating properly configured instances.
 * 
 * This class serves as a simple dependency injection container without
 * requiring a DI framework.
 * 
 * <h2>Adding New XSD Schemas</h2>
 * <p>The easiest way to add new schemas is using auto-discovery:</p>
 * <ol>
 *   <li>Add your XSD file to {@code src/main/resources/xsd/}</li>
 *   <li>Add JAXB generation configuration to {@code pom.xml}</li>
 *   <li>Use {@link #createWithAutoDiscovery()} - schemas are auto-detected</li>
 * </ol>
 * 
 * <p>Or register schemas programmatically:</p>
 * <pre>{@code
 * XsdParserConfig config = XsdParserConfig.builder()
 *     .withAutoDiscovery()
 *     .addSchema(SchemaDefinition.pain("001", "001.12")
 *         .description("Customer Credit Transfer Initiation")
 *         .build())
 *     .build();
 * }</pre>
 */
public final class XsdParserConfig {

    private static volatile XsdParserConfig instance;

    private final XmlSerializer serializer;
    private final XmlValidator validator;
    private final SchemaRegistry schemaRegistry;
    private final XmlProcessingService processingService;
    private final XmlConversionUseCase conversionUseCase;
    private final SchemaValidationUseCase validationUseCase;

    private XsdParserConfig() {
        this(true, List.of());
    }

    private XsdParserConfig(boolean autoDiscover, Collection<SchemaDefinition> additionalSchemas) {
        this.serializer = new JaxbXmlSerializer();
        this.validator = new SchemaBasedXmlValidator();
        this.schemaRegistry = new InMemorySchemaRegistry();
        
        if (autoDiscover) {
            FlexibleSchemaInitializer.builder(schemaRegistry)
                    .autoDiscover(true)
                    .addSchemas(additionalSchemas)
                    .build()
                    .initialize();
        } else {
            new PacsSchemaInitializer(schemaRegistry).initializeSchemas();
            for (SchemaDefinition def : additionalSchemas) {
                registerSchema(def);
            }
        }
        
        this.processingService = new XmlProcessingService(serializer, validator, schemaRegistry);
        this.conversionUseCase = new XmlConversionUseCase(processingService);
        this.validationUseCase = new SchemaValidationUseCase(processingService);
    }

    private void registerSchema(SchemaDefinition definition) {
        schemaRegistry.register(
                com.example.xsdparser.core.model.SchemaInfo.builder()
                        .messageType(definition.getMessageType())
                        .version(definition.getVersion())
                        .namespace(definition.getNamespace())
                        .schemaPath(java.nio.file.Paths.get(definition.getSchemaResourcePath()))
                        .description(definition.getDescription())
                        .build()
        );
    }

    /**
     * Returns the singleton configuration instance.
     *
     * @return the configuration instance
     */
    public static XsdParserConfig getInstance() {
        if (instance == null) {
            synchronized (XsdParserConfig.class) {
                if (instance == null) {
                    instance = new XsdParserConfig();
                }
            }
        }
        return instance;
    }

    /**
     * Creates a new isolated configuration for testing.
     * Each call returns a new independent instance.
     *
     * @return a new configuration instance
     */
    public static XsdParserConfig createIsolated() {
        return new XsdParserConfig();
    }

    /**
     * Creates a configuration with auto-discovery enabled.
     * Automatically detects XSD files in the xsd/ resource directory.
     *
     * @return a new configuration with auto-discovered schemas
     */
    public static XsdParserConfig createWithAutoDiscovery() {
        return new XsdParserConfig(true, List.of());
    }

    /**
     * Creates a builder for custom configuration.
     *
     * @return a new configuration builder
     */
    public static ConfigBuilder builder() {
        return new ConfigBuilder();
    }

    public XmlSerializer getSerializer() {
        return serializer;
    }

    public XmlValidator getValidator() {
        return validator;
    }

    public SchemaRegistry getSchemaRegistry() {
        return schemaRegistry;
    }

    public XmlProcessingService getProcessingService() {
        return processingService;
    }

    public XmlConversionUseCase getConversionUseCase() {
        return conversionUseCase;
    }

    public SchemaValidationUseCase getValidationUseCase() {
        return validationUseCase;
    }

    /**
     * Creates a new TestDataFactory instance.
     *
     * @return a new test data factory
     */
    public TestDataFactory createTestDataFactory() {
        return new TestDataFactory();
    }

    /**
     * Builder for creating custom configurations.
     */
    public static class Builder {
        private XmlSerializer serializer;
        private XmlValidator validator;
        private SchemaRegistry schemaRegistry;
        private boolean initializeSchemas = true;

        public Builder serializer(XmlSerializer serializer) {
            this.serializer = serializer;
            return this;
        }

        public Builder validator(XmlValidator validator) {
            this.validator = validator;
            return this;
        }

        public Builder schemaRegistry(SchemaRegistry schemaRegistry) {
            this.schemaRegistry = schemaRegistry;
            return this;
        }

        public Builder skipSchemaInitialization() {
            this.initializeSchemas = false;
            return this;
        }

        public CustomConfig build() {
            XmlSerializer ser = serializer != null ? serializer : new JaxbXmlSerializer();
            XmlValidator val = validator != null ? validator : new SchemaBasedXmlValidator();
            SchemaRegistry reg = schemaRegistry != null ? schemaRegistry : new InMemorySchemaRegistry();
            
            if (initializeSchemas) {
                new PacsSchemaInitializer(reg).initializeSchemas();
            }
            
            return new CustomConfig(ser, val, reg);
        }
    }

    /**
     * Custom configuration created by the Builder.
     */
    public static class CustomConfig {
        private final XmlSerializer serializer;
        private final XmlValidator validator;
        private final SchemaRegistry schemaRegistry;
        private final XmlProcessingService processingService;
        private final XmlConversionUseCase conversionUseCase;
        private final SchemaValidationUseCase validationUseCase;

        private CustomConfig(XmlSerializer serializer, XmlValidator validator, SchemaRegistry schemaRegistry) {
            this.serializer = serializer;
            this.validator = validator;
            this.schemaRegistry = schemaRegistry;
            this.processingService = new XmlProcessingService(serializer, validator, schemaRegistry);
            this.conversionUseCase = new XmlConversionUseCase(processingService);
            this.validationUseCase = new SchemaValidationUseCase(processingService);
        }

        public XmlSerializer getSerializer() { return serializer; }
        public XmlValidator getValidator() { return validator; }
        public SchemaRegistry getSchemaRegistry() { return schemaRegistry; }
        public XmlProcessingService getProcessingService() { return processingService; }
        public XmlConversionUseCase getConversionUseCase() { return conversionUseCase; }
        public SchemaValidationUseCase getValidationUseCase() { return validationUseCase; }
    }

    /**
     * Builder for creating configurations with custom schema definitions.
     * 
     * <p>Example usage:</p>
     * <pre>{@code
     * XsdParserConfig config = XsdParserConfig.builder()
     *     .withAutoDiscovery()
     *     .addPacsSchema("002", "001.16", "Payment Status Report")
     *     .addPainSchema("001", "001.12", "Customer Credit Transfer")
     *     .addSchema(SchemaDefinition.builder()
     *         .messageType("camt.053")
     *         .version("001.11")
     *         .description("Bank to Customer Statement")
     *         .build())
     *     .build();
     * }</pre>
     */
    public static class ConfigBuilder {
        private final java.util.List<SchemaDefinition> additionalSchemas = new java.util.ArrayList<>();
        private boolean autoDiscover = false;

        /**
         * Enables auto-discovery of XSD files from the classpath.
         */
        public ConfigBuilder withAutoDiscovery() {
            this.autoDiscover = true;
            return this;
        }

        /**
         * Adds a schema definition.
         */
        public ConfigBuilder addSchema(SchemaDefinition definition) {
            this.additionalSchemas.add(definition);
            return this;
        }

        /**
         * Adds a PACS schema (e.g., pacs.008).
         * 
         * @param number message number (e.g., "008")
         * @param version version (e.g., "001.14")
         * @param description human-readable description
         */
        public ConfigBuilder addPacsSchema(String number, String version, String description) {
            return addSchema(SchemaDefinition.pacs(number, version)
                    .description(description)
                    .build());
        }

        /**
         * Adds a PAIN schema (e.g., pain.001).
         */
        public ConfigBuilder addPainSchema(String number, String version, String description) {
            return addSchema(SchemaDefinition.pain(number, version)
                    .description(description)
                    .build());
        }

        /**
         * Adds a CAMT schema (e.g., camt.053).
         */
        public ConfigBuilder addCamtSchema(String number, String version, String description) {
            return addSchema(SchemaDefinition.camt(number, version)
                    .description(description)
                    .build());
        }

        /**
         * Builds the configuration.
         */
        public XsdParserConfig build() {
            return new XsdParserConfig(autoDiscover, additionalSchemas);
        }
    }
}
