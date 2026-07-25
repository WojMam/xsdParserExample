# XSD Parser Example

A demonstration project showing how to use XSD files to dynamically generate Java models for ISO 20022 PACS (Payments Clearing and Settlement) messages. This project implements clean architecture principles with proper separation of concerns.

## Features

- **XSD to Java Generation**: Automatic generation of Java classes from ISO 20022 XSD schemas at build time using JAXB
- **Clean Architecture**: Implements ports and adapters pattern with dependency inversion
- **ISO 20022 Support**: Complete support for 9 PACS message types
- **Test Data Generation**: Dynamic test data factories for creating ISO 20022 compliant identifiers
- **XML Validation**: Schema-based validation of XML documents against XSD schemas
- **Thread-Safe Caching**: Optimized JAXB context and schema caching

## Supported PACS Message Types

| Message Type | Version | Description |
|-------------|---------|-------------|
| pacs.002 | 001.16 | FIToFI Payment Status Report |
| pacs.003 | 001.12 | FIToFI Customer Direct Debit |
| pacs.004 | 001.15 | Payment Return |
| pacs.007 | 001.14 | FIToFI Payment Reversal |
| pacs.008 | 001.14 | FIToFI Customer Credit Transfer |
| pacs.009 | 001.13 | Financial Institution Credit Transfer |
| pacs.010 | 001.06 | Financial Institution Direct Debit |
| pacs.028 | 001.07 | FIToFI Payment Status Request |
| pacs.029 | 001.02 | FIToFI Payment Status Report Acknowledgement |

## Project Structure

```
src/
├── main/
│   ├── java/com/example/xsdparser/
│   │   ├── core/                    # Core domain layer
│   │   │   ├── model/               # Domain models (SchemaInfo, XmlDocument, ValidationResult)
│   │   │   ├── port/                # Port interfaces (XmlSerializer, XmlValidator, SchemaRegistry)
│   │   │   └── service/             # Domain services (XmlProcessingService)
│   │   ├── application/             # Application layer
│   │   │   ├── factory/             # Test data factories
│   │   │   └── usecase/             # Use cases (XmlConversionUseCase, SchemaValidationUseCase)
│   │   ├── infrastructure/          # Infrastructure layer
│   │   │   ├── schema/              # Schema registry implementation
│   │   │   └── xml/                 # JAXB serializer and validator implementations
│   │   ├── XsdParserApplication.java
│   │   └── XsdParserConfig.java     # Dependency injection configuration
│   └── resources/
│       └── xsd/                     # ISO 20022 XSD schema files
└── test/
    └── java/com/example/xsdparser/
        ├── application/             # Application layer tests
        ├── core/                    # Core domain tests
        ├── infrastructure/          # Infrastructure tests
        └── integration/             # Integration tests
```

## Prerequisites

- Java 17 or higher
- Maven 3.8+

## Building the Project

```bash
# Compile and generate JAXB classes from XSD files
mvn clean compile

# Run all tests
mvn test

# Build with test coverage report
mvn clean verify

# Package the application
mvn package
```

## Usage

### Configuration

Use `XsdParserConfig` to get configured instances of all components:

```java
// Get singleton configuration
XsdParserConfig config = XsdParserConfig.getInstance();

// Or create isolated instance for testing
XsdParserConfig testConfig = XsdParserConfig.createIsolated();

// Access components
XmlSerializer serializer = config.getSerializer();
XmlValidator validator = config.getValidator();
SchemaRegistry registry = config.getSchemaRegistry();
```

### Creating ISO 20022 Messages

```java
import com.example.xsdparser.generated.pacs008.*;
import com.example.xsdparser.application.factory.Iso20022TestDataFactory;

// Create a PACS.008 Credit Transfer document
Document document = new Document();
FIToFICustomerCreditTransferV14 transfer = new FIToFICustomerCreditTransferV14();

// Build group header with generated test data
GroupHeader131 header = new GroupHeader131();
header.setMsgId(Iso20022TestDataFactory.generateMessageId());
header.setCreDtTm(Iso20022TestDataFactory.generateCreationDateTime());
header.setNbOfTxs("1");

transfer.setGrpHdr(header);
document.setFIToFICstmrCdtTrf(transfer);

// Serialize to XML using JAXBElement for proper namespace handling
ObjectFactory factory = new ObjectFactory();
JAXBElement<Document> jaxbElement = factory.createDocument(document);

XmlSerializer serializer = XsdParserConfig.getInstance().getSerializer();
String xml = serializer.serializeFormatted(jaxbElement);
```

### Validating XML Against Schema

```java
import com.example.xsdparser.application.usecase.SchemaValidationUseCase;
import com.example.xsdparser.core.model.ValidationResult;

SchemaValidationUseCase validationUseCase = config.getValidationUseCase();

// Validate against message type
ValidationResult result = validationUseCase.validate(xml, "pacs.008");

if (result.isValid()) {
    System.out.println("Document is valid");
} else {
    result.getErrors().forEach(error -> 
        System.out.println("Error at line " + error.lineNumber() + ": " + error.getMessage())
    );
}

// Or validate against specific schema resource
ValidationResult result = validationUseCase.validateAgainstResource(xml, "xsd/pacs.008.001.14.xsd");
```

### Generating ISO 20022 Test Data

```java
import com.example.xsdparser.application.factory.Iso20022TestDataFactory;

// Generate unique identifiers
String messageId = Iso20022TestDataFactory.generateMessageId();      // e.g., "2024012515300100001234"
String instructionId = Iso20022TestDataFactory.generateInstructionId(); // e.g., "INSTR1A2B3C4D5E6F7890"
String endToEndId = Iso20022TestDataFactory.generateEndToEndId();    // e.g., "E2E1A2B3C4D5E6F7890ABCDE"

// Generate financial identifiers
String iban = Iso20022TestDataFactory.generateIban("DE");  // Valid German IBAN with checksum
String bic = Iso20022TestDataFactory.generateBic();        // e.g., "DEUTDEFF"
String lei = Iso20022TestDataFactory.generateLei();        // 20-character LEI

// Generate amounts and currencies
BigDecimal amount = Iso20022TestDataFactory.generateAmount(100.00, 50000.00);
String currency = Iso20022TestDataFactory.generateCurrencyCode();  // e.g., "EUR", "USD"

// Generate dates
XMLGregorianCalendar now = Iso20022TestDataFactory.generateCreationDateTime();
XMLGregorianCalendar past = Iso20022TestDataFactory.generatePastDate(7);  // 7 days ago
XMLGregorianCalendar future = Iso20022TestDataFactory.generateFutureDate(30);  // 30 days from now
```

### Dynamic Test Object Creation

```java
import com.example.xsdparser.application.factory.TestDataFactory;

// Create objects with automatic field population
TestDataFactory factory = new TestDataFactory()
    .withMaxDepth(3)           // Control recursion depth
    .withOptionalFields()       // Include optional fields
    .withFixedValues();         // Use deterministic values

// Or use the builder pattern
Document doc = TestDataFactory.builder(Document.class)
    .maxDepth(2)
    .with("msgId", "CUSTOM-ID-12345")
    .withTypeProvider(BigDecimal.class, () -> BigDecimal.valueOf(1000.00))
    .build();
```

### Using the XML Conversion Use Case

```java
import com.example.xsdparser.application.usecase.XmlConversionUseCase;

XmlConversionUseCase conversionUseCase = config.getConversionUseCase();

// Convert object to XML with validation
XmlConversionUseCase.ConversionResult<Document> result = 
    conversionUseCase.toXmlWithValidation(document, "pacs.008");

if (result.isValid()) {
    String validXml = result.xml();
}

// Perform round-trip test
XmlConversionUseCase.RoundTripResult<Document> roundTrip = 
    conversionUseCase.roundTrip(document, Document.class);

if (roundTrip.matches()) {
    System.out.println("Serialization is consistent");
}
```

## Running the Application

```bash
mvn clean compile exec:java -Dexec.mainClass="com.example.xsdparser.XsdParserApplication"
```

## Architecture

This project follows **Clean Architecture** (Hexagonal Architecture) principles:

### Core Layer (`core/`)
Contains the domain model and business logic with no external dependencies:
- **Models**: `SchemaInfo`, `XmlDocument`, `ValidationResult` - immutable value objects
- **Ports**: `XmlSerializer`, `XmlValidator`, `SchemaRegistry` - interface contracts
- **Services**: `XmlProcessingService` - domain service orchestrating operations

### Application Layer (`application/`)
Contains application-specific business rules and use cases:
- **Use Cases**: `XmlConversionUseCase`, `SchemaValidationUseCase` - high-level operations
- **Factories**: `TestDataFactory`, `Iso20022TestDataFactory` - test data generation

### Infrastructure Layer (`infrastructure/`)
Contains implementations of ports and technical concerns:
- **XML**: `JaxbXmlSerializer`, `SchemaBasedXmlValidator` - JAXB/SAX implementations
- **Schema**: `InMemorySchemaRegistry`, `PacsSchemaInitializer` - schema management

## Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Jakarta XML Bind API | 4.0.2 | JAXB API for XML binding |
| JAXB Runtime | 4.0.5 | JAXB implementation |
| SLF4J + Logback | 2.0.12 / 1.5.3 | Logging |
| JUnit 5 | 5.10.2 | Testing framework |
| AssertJ | 3.25.3 | Fluent assertions |
| Mockito | 5.11.0 | Mocking framework |
| Instancio | 4.3.2 | Test data generation |

## Adding New Schemas

The library supports easy schema extension with auto-discovery:

```java
// 1. Add XSD file to src/main/resources/xsd/ (e.g., pain.001.001.12.xsd)
// 2. Add JAXB generation to pom.xml
// 3. Use auto-discovery - schemas are detected automatically!
XsdParserConfig config = XsdParserConfig.createWithAutoDiscovery();
```

Or register programmatically:

```java
XsdParserConfig config = XsdParserConfig.builder()
    .withAutoDiscovery()
    .addPainSchema("001", "001.12", "Customer Credit Transfer")
    .addCamtSchema("053", "001.11", "Bank to Customer Statement")
    .build();
```

See [ADDING_NEW_SCHEMAS.md](ADDING_NEW_SCHEMAS.md) for the complete guide.

## AI Agent Integration

This project includes special documentation for AI agents (Copilot, Claude, etc.) to quickly understand and integrate the library:

| Document | Purpose |
|----------|---------|
| [INTEGRATION.md](INTEGRATION.md) | Step-by-step integration guide with copy-paste ready code |
| [AI_INSTRUCTIONS.md](AI_INSTRUCTIONS.md) | Quick reference for AI agents with templates and patterns |
| [SNIPPETS.md](SNIPPETS.md) | Self-contained, ready-to-use code snippets |
| [ADDING_NEW_SCHEMAS.md](ADDING_NEW_SCHEMAS.md) | How to add new XSD schemas |

### Quick Integration for AI Agents

```java
// 1. Initialize (one-time setup)
XmlSerializer serializer = new JaxbXmlSerializer();
XmlValidator validator = new SchemaBasedXmlValidator();
SchemaRegistry registry = new InMemorySchemaRegistry();
new PacsSchemaInitializer(registry).initializeSchemas();
XmlProcessingService service = new XmlProcessingService(serializer, validator, registry);

// 2. Generate test data
String messageId = Iso20022TestDataFactory.generateMessageId();
String iban = Iso20022TestDataFactory.generateIban("DE");

// 3. Validate XML
ValidationResult result = service.validateXml(xml, "pacs.008");
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes and add tests
4. Ensure all tests pass (`mvn test`)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## License

This project is provided as an example/demonstration and is available for educational purposes.
