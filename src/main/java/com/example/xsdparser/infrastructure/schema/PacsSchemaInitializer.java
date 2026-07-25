package com.example.xsdparser.infrastructure.schema;

import com.example.xsdparser.core.model.SchemaInfo;
import com.example.xsdparser.core.port.SchemaRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Initializes the schema registry with ISO 20022 PACS (Payments Clearing and Settlement) schemas.
 * This class is responsible for registering all available PACS message schemas.
 */
public class PacsSchemaInitializer {

    private static final Logger logger = LoggerFactory.getLogger(PacsSchemaInitializer.class);

    private static final String BASE_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:";
    private static final String XSD_RESOURCE_PATH = "xsd/";

    private final SchemaRegistry registry;

    public PacsSchemaInitializer(SchemaRegistry registry) {
        this.registry = registry;
    }

    /**
     * Registers all available PACS schemas in the registry.
     */
    public void initializeSchemas() {
        logger.info("Initializing PACS schemas...");

        registerSchema("pacs.002", "001.16", "Payment Status Report",
                "FIToFIPaymentStatusReport - Status information about a payment instruction");

        registerSchema("pacs.003", "001.12", "Direct Debit",
                "FIToFICustomerDirectDebit - Direct debit instruction between financial institutions");

        registerSchema("pacs.004", "001.15", "Payment Return",
                "PaymentReturn - Return of funds from a previous payment transaction");

        registerSchema("pacs.007", "001.14", "Payment Reversal",
                "FIToFIPaymentReversal - Request to reverse a payment");

        registerSchema("pacs.008", "001.14", "Credit Transfer",
                "FIToFICustomerCreditTransfer - Credit transfer instruction from one FI to another");

        registerSchema("pacs.009", "001.13", "Financial Institution Credit Transfer",
                "FinancialInstitutionCreditTransfer - Credit transfer between financial institutions");

        registerSchema("pacs.010", "001.06", "Financial Institution Direct Debit",
                "FinancialInstitutionDirectDebit - Direct debit between financial institutions");

        registerSchema("pacs.028", "001.07", "Payment Status Request",
                "FIToFIPaymentStatusRequest - Request for payment status information");

        registerSchema("pacs.029", "001.02", "Status Report Acknowledgement",
                "FIToFIPaymentStatusReportAcknowledgement - Acknowledgement of status report");

        logger.info("Registered {} PACS schemas", registry.getAllSchemas().size());
    }

    private void registerSchema(String messageType, String version, String shortDescription, String fullDescription) {
        String fullIdentifier = messageType + "." + version;
        String namespace = BASE_NAMESPACE + fullIdentifier;
        Path schemaPath = Paths.get(XSD_RESOURCE_PATH + fullIdentifier + ".xsd");

        SchemaInfo schemaInfo = SchemaInfo.builder()
                .messageType(messageType)
                .version(version)
                .namespace(namespace)
                .schemaPath(schemaPath)
                .description(fullDescription)
                .build();

        registry.register(schemaInfo);
        logger.debug("Registered schema: {} - {}", fullIdentifier, shortDescription);
    }

    /**
     * Gets information about the available schema types.
     */
    public static class SchemaTypes {
        public static final String PACS_002 = "pacs.002";
        public static final String PACS_003 = "pacs.003";
        public static final String PACS_004 = "pacs.004";
        public static final String PACS_007 = "pacs.007";
        public static final String PACS_008 = "pacs.008";
        public static final String PACS_009 = "pacs.009";
        public static final String PACS_010 = "pacs.010";
        public static final String PACS_028 = "pacs.028";
        public static final String PACS_029 = "pacs.029";

        private SchemaTypes() {}
    }
}
