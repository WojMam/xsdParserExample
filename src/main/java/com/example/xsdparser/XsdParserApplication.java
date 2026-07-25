package com.example.xsdparser;

import com.example.xsdparser.application.factory.Iso20022TestDataFactory;
import com.example.xsdparser.application.factory.TestDataFactory;
import com.example.xsdparser.application.usecase.SchemaValidationUseCase;
import com.example.xsdparser.application.usecase.XmlConversionUseCase;
import com.example.xsdparser.core.model.SchemaInfo;
import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.port.SchemaRegistry;
import com.example.xsdparser.core.port.XmlSerializer;
import com.example.xsdparser.core.port.XmlValidator;
import com.example.xsdparser.core.service.XmlProcessingService;
import com.example.xsdparser.infrastructure.schema.InMemorySchemaRegistry;
import com.example.xsdparser.infrastructure.schema.PacsSchemaInitializer;
import com.example.xsdparser.infrastructure.xml.JaxbXmlSerializer;
import com.example.xsdparser.infrastructure.xml.SchemaBasedXmlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application entry point demonstrating XSD to Java model generation.
 * 
 * This application shows how to:
 * 1. Generate Java models from XSD files (done at build time via JAXB)
 * 2. Create test data dynamically without static files
 * 3. Serialize/deserialize XML using JAXB
 * 4. Validate XML against XSD schemas
 * 
 * Run with: mvn clean compile exec:java -Dexec.mainClass="com.example.xsdparser.XsdParserApplication"
 */
public class XsdParserApplication {

    private static final Logger logger = LoggerFactory.getLogger(XsdParserApplication.class);

    private final XmlProcessingService processingService;
    private final XmlConversionUseCase conversionUseCase;
    private final SchemaValidationUseCase validationUseCase;
    private final TestDataFactory testDataFactory;

    public XsdParserApplication() {
        XmlSerializer serializer = new JaxbXmlSerializer();
        XmlValidator validator = new SchemaBasedXmlValidator();
        SchemaRegistry registry = new InMemorySchemaRegistry();
        
        new PacsSchemaInitializer(registry).initializeSchemas();
        
        this.processingService = new XmlProcessingService(serializer, validator, registry);
        this.conversionUseCase = new XmlConversionUseCase(processingService);
        this.validationUseCase = new SchemaValidationUseCase(processingService);
        this.testDataFactory = new TestDataFactory();
    }

    public static void main(String[] args) {
        logger.info("Starting XSD Parser Example Application");
        logger.info("=".repeat(60));
        
        XsdParserApplication app = new XsdParserApplication();
        
        app.demonstrateSchemaRegistry();
        app.demonstrateTestDataGeneration();
        
        logger.info("=".repeat(60));
        logger.info("Application completed successfully");
    }

    private void demonstrateSchemaRegistry() {
        logger.info("\n--- Schema Registry Demo ---");
        
        logger.info("Available schemas:");
        for (SchemaInfo schema : validationUseCase.listAvailableSchemas()) {
            logger.info("  - {} ({})", schema.getFullIdentifier(), 
                    schema.getDescription().orElse("No description"));
        }
    }

    private void demonstrateTestDataGeneration() {
        logger.info("\n--- Test Data Generation Demo ---");
        
        logger.info("Generated Test Data:");
        logger.info("  Message ID: {}", Iso20022TestDataFactory.generateMessageId());
        logger.info("  Instruction ID: {}", Iso20022TestDataFactory.generateInstructionId());
        logger.info("  End-to-End ID: {}", Iso20022TestDataFactory.generateEndToEndId());
        logger.info("  Transaction ID: {}", Iso20022TestDataFactory.generateTransactionId());
        logger.info("  IBAN (DE): {}", Iso20022TestDataFactory.generateIban("DE"));
        logger.info("  IBAN (GB): {}", Iso20022TestDataFactory.generateIban("GB"));
        logger.info("  BIC: {}", Iso20022TestDataFactory.generateBic());
        logger.info("  LEI: {}", Iso20022TestDataFactory.generateLei());
        logger.info("  Amount: {} {}", Iso20022TestDataFactory.generateAmount(), 
                Iso20022TestDataFactory.generateCurrencyCode());
        logger.info("  Payment Reference: {}", Iso20022TestDataFactory.generatePaymentReference());
        logger.info("  Status Code: {}", Iso20022TestDataFactory.generatePaymentStatusCode());
        logger.info("  Company: {}", Iso20022TestDataFactory.generateCompanyName());
        logger.info("  Address: {}, {}, {}", 
                Iso20022TestDataFactory.generateStreetAddress(),
                Iso20022TestDataFactory.generateCity(),
                Iso20022TestDataFactory.generateCountryCode());
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

    public TestDataFactory getTestDataFactory() {
        return testDataFactory;
    }
}
