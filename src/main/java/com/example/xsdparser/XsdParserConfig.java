package com.example.xsdparser;

import com.example.xsdparser.application.factory.TestDataFactory;
import com.example.xsdparser.application.usecase.SchemaValidationUseCase;
import com.example.xsdparser.application.usecase.XmlConversionUseCase;
import com.example.xsdparser.core.port.SchemaRegistry;
import com.example.xsdparser.core.port.XmlSerializer;
import com.example.xsdparser.core.port.XmlValidator;
import com.example.xsdparser.core.service.XmlProcessingService;
import com.example.xsdparser.infrastructure.schema.InMemorySchemaRegistry;
import com.example.xsdparser.infrastructure.schema.PacsSchemaInitializer;
import com.example.xsdparser.infrastructure.xml.JaxbXmlSerializer;
import com.example.xsdparser.infrastructure.xml.SchemaBasedXmlValidator;

/**
 * Configuration class for setting up the XSD Parser components.
 * Provides factory methods for creating properly configured instances.
 * 
 * This class serves as a simple dependency injection container without
 * requiring a DI framework.
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
        this.serializer = new JaxbXmlSerializer();
        this.validator = new SchemaBasedXmlValidator();
        this.schemaRegistry = new InMemorySchemaRegistry();
        
        new PacsSchemaInitializer(schemaRegistry).initializeSchemas();
        
        this.processingService = new XmlProcessingService(serializer, validator, schemaRegistry);
        this.conversionUseCase = new XmlConversionUseCase(processingService);
        this.validationUseCase = new SchemaValidationUseCase(processingService);
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
}
