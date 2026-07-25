package com.example.xsdparser.core;

/**
 * Constants for ISO 20022 messaging and XSD Parser operations.
 * Provides centralized definitions for namespaces, schema paths, and message types.
 */
public final class Iso20022Constants {

    private Iso20022Constants() {
    }

    /**
     * ISO 20022 namespace base URI.
     */
    public static final String ISO20022_NAMESPACE_BASE = "urn:iso:std:iso:20022:tech:xsd:";

    /**
     * XSD schema resource base path.
     */
    public static final String XSD_RESOURCE_PATH = "xsd/";

    /**
     * XML encoding used for all documents.
     */
    public static final String XML_ENCODING = "UTF-8";

    /**
     * PACS message type identifiers.
     */
    public static final class MessageTypes {
        public static final String PACS_002 = "pacs.002";
        public static final String PACS_003 = "pacs.003";
        public static final String PACS_004 = "pacs.004";
        public static final String PACS_007 = "pacs.007";
        public static final String PACS_008 = "pacs.008";
        public static final String PACS_009 = "pacs.009";
        public static final String PACS_010 = "pacs.010";
        public static final String PACS_028 = "pacs.028";
        public static final String PACS_029 = "pacs.029";

        private MessageTypes() {
        }
    }

    /**
     * Schema version identifiers.
     */
    public static final class SchemaVersions {
        public static final String PACS_002_VERSION = "001.16";
        public static final String PACS_003_VERSION = "001.12";
        public static final String PACS_004_VERSION = "001.15";
        public static final String PACS_007_VERSION = "001.14";
        public static final String PACS_008_VERSION = "001.14";
        public static final String PACS_009_VERSION = "001.13";
        public static final String PACS_010_VERSION = "001.06";
        public static final String PACS_028_VERSION = "001.07";
        public static final String PACS_029_VERSION = "001.02";

        private SchemaVersions() {
        }
    }

    /**
     * Payment status codes as defined in ISO 20022.
     */
    public static final class PaymentStatus {
        public static final String ACCEPTED_CUSTOMER_PROFILE = "ACCP";
        public static final String ACCEPTED_SETTLEMENT_COMPLETED = "ACSC";
        public static final String ACCEPTED_SETTLEMENT_IN_PROCESS = "ACSP";
        public static final String ACCEPTED_TECHNICAL_VALIDATION = "ACTC";
        public static final String ACCEPTED_WITH_CHANGE = "ACWC";
        public static final String PARTIALLY_ACCEPTED = "PART";
        public static final String PENDING = "PDNG";
        public static final String RECEIVED = "RCVD";
        public static final String REJECTED = "RJCT";

        private PaymentStatus() {
        }
    }

    /**
     * Common reject reason codes.
     */
    public static final class RejectReasons {
        public static final String INCORRECT_ACCOUNT_NUMBER = "AC01";
        public static final String CLOSED_ACCOUNT_NUMBER = "AC04";
        public static final String BLOCKED_ACCOUNT = "AC06";
        public static final String MISSING_CREDITOR_AGENT = "AG01";
        public static final String INVALID_AGENT = "AG02";
        public static final String ZERO_AMOUNT = "AM01";
        public static final String NOT_ALLOWED_AMOUNT = "AM02";
        public static final String NOT_ALLOWED_CURRENCY = "AM03";
        public static final String INCORRECT_AGENT = "BE01";
        public static final String MISSING_DEBTOR_NAME = "BE04";
        public static final String NO_MANDATE = "MD01";
        public static final String REFUND_REQUEST_BY_END_CUSTOMER = "MD02";
        public static final String REASON_NOT_SPECIFIED = "MS01";
        public static final String NOT_SPECIFIED_REASON_AGENT_GENERATED = "MS02";
        public static final String BANK_IDENTIFIER_INCORRECT = "RC01";
        public static final String CUT_OFF_TIME = "TM01";

        private RejectReasons() {
        }
    }

    /**
     * Common currency codes.
     */
    public static final class Currencies {
        public static final String EUR = "EUR";
        public static final String USD = "USD";
        public static final String GBP = "GBP";
        public static final String CHF = "CHF";
        public static final String JPY = "JPY";
        public static final String SEK = "SEK";
        public static final String NOK = "NOK";
        public static final String DKK = "DKK";
        public static final String PLN = "PLN";
        public static final String CZK = "CZK";

        private Currencies() {
        }
    }

    /**
     * Common country codes.
     */
    public static final class Countries {
        public static final String GERMANY = "DE";
        public static final String UNITED_KINGDOM = "GB";
        public static final String NETHERLANDS = "NL";
        public static final String FRANCE = "FR";
        public static final String SPAIN = "ES";
        public static final String ITALY = "IT";
        public static final String BELGIUM = "BE";
        public static final String AUSTRIA = "AT";
        public static final String SWEDEN = "SE";
        public static final String DENMARK = "DK";

        private Countries() {
        }
    }

    /**
     * Identifier constraints for ISO 20022.
     */
    public static final class IdentifierLimits {
        public static final int MESSAGE_ID_MAX_LENGTH = 35;
        public static final int INSTRUCTION_ID_MAX_LENGTH = 35;
        public static final int END_TO_END_ID_MAX_LENGTH = 35;
        public static final int TRANSACTION_ID_MAX_LENGTH = 35;
        public static final int IBAN_MIN_LENGTH = 15;
        public static final int IBAN_MAX_LENGTH = 34;
        public static final int BIC_MIN_LENGTH = 8;
        public static final int BIC_MAX_LENGTH = 11;
        public static final int LEI_LENGTH = 20;

        private IdentifierLimits() {
        }
    }

    /**
     * Builds the full namespace for a message type and version.
     *
     * @param messageType the message type (e.g., "pacs.002")
     * @param version     the version (e.g., "001.16")
     * @return the full namespace URI
     */
    public static String buildNamespace(String messageType, String version) {
        return ISO20022_NAMESPACE_BASE + messageType + "." + version;
    }

    /**
     * Builds the schema resource path for a message type and version.
     *
     * @param messageType the message type (e.g., "pacs.002")
     * @param version     the version (e.g., "001.16")
     * @return the schema resource path
     */
    public static String buildSchemaPath(String messageType, String version) {
        return XSD_RESOURCE_PATH + messageType + "." + version + ".xsd";
    }
}
