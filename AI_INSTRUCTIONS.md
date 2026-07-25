# AI Agent Instructions for XSD Parser Library

> **Purpose**: This file helps AI agents (Copilot, Claude, ChatGPT, etc.) quickly understand and integrate this library.

## Quick Reference

### What This Library Does
- Generates Java classes from ISO 20022 XSD schemas using JAXB
- Validates XML against XSD schemas
- Generates compliant test data for ISO 20022 messages
- Supports PACS messages (002, 003, 004, 007, 008, 009, 010, 028, 029)

### Key Classes to Know

| Class | Purpose | When to Use |
|-------|---------|-------------|
| `JaxbXmlSerializer` | XML serialization | Converting objects to/from XML |
| `SchemaBasedXmlValidator` | XML validation | Validating XML against XSD |
| `InMemorySchemaRegistry` | Schema storage | Managing available schemas |
| `Iso20022TestDataFactory` | Test data generation | Creating valid test identifiers |
| `XmlProcessingService` | Orchestration | Combining serialize + validate |

### Import Statements (Copy These)

```java
// Core
import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.model.SchemaInfo;
import com.example.xsdparser.core.service.XmlProcessingService;

// Ports
import com.example.xsdparser.core.port.XmlSerializer;
import com.example.xsdparser.core.port.XmlValidator;
import com.example.xsdparser.core.port.SchemaRegistry;

// Infrastructure
import com.example.xsdparser.infrastructure.xml.JaxbXmlSerializer;
import com.example.xsdparser.infrastructure.xml.SchemaBasedXmlValidator;
import com.example.xsdparser.infrastructure.schema.InMemorySchemaRegistry;
import com.example.xsdparser.infrastructure.schema.PacsSchemaInitializer;

// Factories
import com.example.xsdparser.application.factory.Iso20022TestDataFactory;
import com.example.xsdparser.application.factory.TestDataFactory;

// Generated PACS classes (example for pacs.008)
import com.example.xsdparser.generated.pacs008.*;
```

## Code Generation Templates

### Template: Basic Setup
```java
// Initialize components
XmlSerializer serializer = new JaxbXmlSerializer();
XmlValidator validator = new SchemaBasedXmlValidator();
SchemaRegistry registry = new InMemorySchemaRegistry();
new PacsSchemaInitializer(registry).initializeSchemas();
XmlProcessingService service = new XmlProcessingService(serializer, validator, registry);
```

### Template: Create PACS.008 Credit Transfer
```java
public Document createPacs008(String debtorIban, String creditorIban, BigDecimal amount) {
    Document doc = new Document();
    FIToFICustomerCreditTransferV14 transfer = new FIToFICustomerCreditTransferV14();
    
    // Header
    GroupHeader131 header = new GroupHeader131();
    header.setMsgId(Iso20022TestDataFactory.generateMessageId());
    header.setCreDtTm(Iso20022TestDataFactory.generateCreationDateTime());
    header.setNbOfTxs("1");
    
    SettlementInstruction15 settlement = new SettlementInstruction15();
    settlement.setSttlmMtd(SettlementMethod1Code.CLRG);
    header.setSttlmInf(settlement);
    transfer.setGrpHdr(header);
    
    // Transaction
    CreditTransferTransaction73 tx = new CreditTransferTransaction73();
    PaymentIdentification13 pmtId = new PaymentIdentification13();
    pmtId.setInstrId(Iso20022TestDataFactory.generateInstructionId());
    pmtId.setEndToEndId(Iso20022TestDataFactory.generateEndToEndId());
    pmtId.setTxId(Iso20022TestDataFactory.generateTransactionId());
    tx.setPmtId(pmtId);
    
    ActiveCurrencyAndAmount amt = new ActiveCurrencyAndAmount();
    amt.setValue(amount);
    amt.setCcy("EUR");
    tx.setIntrBkSttlmAmt(amt);
    
    transfer.getCdtTrfTxInf().add(tx);
    doc.setFIToFICstmrCdtTrf(transfer);
    return doc;
}
```

### Template: Create PACS.002 Status Report
```java
public Document createPacs002(String originalMsgId) {
    Document doc = new Document();
    FIToFIPaymentStatusReportV16 report = new FIToFIPaymentStatusReportV16();
    
    GroupHeader120 header = new GroupHeader120();
    header.setMsgId(Iso20022TestDataFactory.generateMessageId());
    header.setCreDtTm(Iso20022TestDataFactory.generateCreationDateTime());
    report.setGrpHdr(header);
    
    OriginalGroupHeader22 origGroup = new OriginalGroupHeader22();
    origGroup.setOrgnlMsgId(originalMsgId);
    origGroup.setOrgnlMsgNmId("pacs.008.001.14");
    origGroup.setOrgnlCreDtTm(Iso20022TestDataFactory.generatePastDate(1));
    report.getOrgnlGrpInfAndSts().add(origGroup);
    
    doc.setFIToFIPmtStsRpt(report);
    return doc;
}
```

### Template: Serialize to XML
```java
public String toXml(Document document) {
    ObjectFactory factory = new ObjectFactory();
    JAXBElement<Document> element = factory.createDocument(document);
    return new JaxbXmlSerializer().serializeFormatted(element);
}
```

### Template: Validate XML
```java
public ValidationResult validate(String xml, String messageType) {
    XmlValidator validator = new SchemaBasedXmlValidator();
    SchemaRegistry registry = new InMemorySchemaRegistry();
    new PacsSchemaInitializer(registry).initializeSchemas();
    
    SchemaInfo schema = registry.findByMessageType(messageType)
        .orElseThrow(() -> new IllegalArgumentException("Unknown message type: " + messageType));
    
    return validator.validate(xml, schema);
}
```

### Template: Generate Test Data
```java
// Identifiers
String msgId = Iso20022TestDataFactory.generateMessageId();      // "2024012515300100001234"
String instrId = Iso20022TestDataFactory.generateInstructionId(); // "INSTR1A2B3C4D5E6F7890"
String e2eId = Iso20022TestDataFactory.generateEndToEndId();     // "E2E1A2B3C4D5E6F7890ABCDE"
String txId = Iso20022TestDataFactory.generateTransactionId();   // "TXN1234567890123456"

// Financial Identifiers
String iban = Iso20022TestDataFactory.generateIban("DE");        // "DE89370400440532013000"
String bic = Iso20022TestDataFactory.generateBic();              // "DEUTDEFF"
String lei = Iso20022TestDataFactory.generateLei();              // "5493001KJTIIGC8Y1R12"

// Amounts & Dates
BigDecimal amount = Iso20022TestDataFactory.generateAmount();    // 12345.67
String currency = Iso20022TestDataFactory.generateCurrencyCode(); // "EUR"
XMLGregorianCalendar now = Iso20022TestDataFactory.generateCreationDateTime();
XMLGregorianCalendar past = Iso20022TestDataFactory.generatePastDate(7);
XMLGregorianCalendar future = Iso20022TestDataFactory.generateFutureDate(30);

// Party Information
String name = Iso20022TestDataFactory.generateCompanyName();     // "Global Trading Ltd"
String address = Iso20022TestDataFactory.generateStreetAddress(); // "123 Main Street"
String city = Iso20022TestDataFactory.generateCity();            // "Frankfurt"
String country = Iso20022TestDataFactory.generateCountryCode();  // "DE"
```

## Common Patterns

### Pattern: Spring Boot Integration
```java
@Configuration
public class XsdParserConfig {
    @Bean
    public XmlSerializer xmlSerializer() {
        return new JaxbXmlSerializer();
    }
    
    @Bean
    public XmlValidator xmlValidator() {
        return new SchemaBasedXmlValidator();
    }
    
    @Bean
    public SchemaRegistry schemaRegistry() {
        InMemorySchemaRegistry registry = new InMemorySchemaRegistry();
        new PacsSchemaInitializer(registry).initializeSchemas();
        return registry;
    }
    
    @Bean
    public XmlProcessingService xmlProcessingService(
            XmlSerializer serializer, 
            XmlValidator validator, 
            SchemaRegistry registry) {
        return new XmlProcessingService(serializer, validator, registry);
    }
}
```

### Pattern: REST Controller
```java
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final XmlProcessingService processingService;
    private final JaxbXmlSerializer serializer;
    
    @PostMapping("/validate")
    public ResponseEntity<?> validatePayment(@RequestBody String xml, 
                                             @RequestParam String messageType) {
        ValidationResult result = processingService.validateXml(xml, messageType);
        if (result.isValid()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(result.getErrors());
    }
    
    @PostMapping("/create/pacs008")
    public String createCreditTransfer(@RequestBody CreditTransferRequest request) {
        Document doc = // ... build document
        ObjectFactory factory = new ObjectFactory();
        return serializer.serializeFormatted(factory.createDocument(doc));
    }
}
```

### Pattern: Unit Test
```java
@Test
void shouldCreateValidPacs008() {
    // Arrange
    Document doc = createPacs008WithTestData();
    ObjectFactory factory = new ObjectFactory();
    
    // Act
    String xml = serializer.serializeFormatted(factory.createDocument(doc));
    ValidationResult result = processingService.validateXml(xml, "pacs.008");
    
    // Assert
    assertThat(result.isValid()).isTrue();
    assertThat(xml).contains("FIToFICstmrCdtTrf");
}
```

## Error Handling

```java
// Check validation result
ValidationResult result = service.validateXml(xml, "pacs.008");
if (!result.isValid()) {
    for (ValidationResult.ValidationError error : result.getErrors()) {
        log.error("Validation error at line {}, column {}: {} [{}]",
            error.lineNumber(),
            error.columnNumber(), 
            error.getMessage(),
            error.severity());
    }
}

// Handle serialization errors
try {
    String xml = serializer.serialize(element);
} catch (XmlSerializer.XmlSerializationException e) {
    log.error("Serialization failed: {}", e.getMessage());
}
```

## File Locations

```
src/main/java/com/example/xsdparser/
├── core/
│   ├── model/          # SchemaInfo, XmlDocument, ValidationResult
│   ├── port/           # Interfaces: XmlSerializer, XmlValidator, SchemaRegistry
│   ├── service/        # XmlProcessingService
│   └── exception/      # Exception classes
├── application/
│   ├── usecase/        # XmlConversionUseCase, SchemaValidationUseCase
│   └── factory/        # TestDataFactory, Iso20022TestDataFactory
├── infrastructure/
│   ├── xml/            # JaxbXmlSerializer, SchemaBasedXmlValidator
│   └── schema/         # InMemorySchemaRegistry, PacsSchemaInitializer
└── generated/          # JAXB-generated classes (pacs002, pacs008, etc.)

src/main/resources/
└── xsd/                # ISO 20022 XSD schema files
```

## Quick Answers

**Q: How do I add a new XSD schema?**
1. Add XSD file to `src/main/resources/xsd/`
2. Add execution to `jaxb2-maven-plugin` in `pom.xml`
3. Register in `PacsSchemaInitializer`

**Q: How do I customize serialization?**
Use `JaxbXmlSerializer` methods: `serialize()`, `serializeFormatted()`, or extend the class.

**Q: How do I validate against a custom schema?**
```java
validator.validateAgainstSchema(xml, "path/to/schema.xsd");
```

**Q: How do I parse XML to objects?**
```java
Document doc = serializer.deserialize(xml, Document.class);
```
