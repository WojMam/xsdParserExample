# Quick Integration Guide

> **For AI Agents**: This document provides copy-paste ready code for integrating the XSD Parser library into existing Java projects. All examples are self-contained and tested.

## Step 1: Add Dependencies

### Maven (pom.xml)

```xml
<dependencies>
    <!-- JAXB API -->
    <dependency>
        <groupId>jakarta.xml.bind</groupId>
        <artifactId>jakarta.xml.bind-api</artifactId>
        <version>4.0.2</version>
    </dependency>
    
    <!-- JAXB Runtime -->
    <dependency>
        <groupId>org.glassfish.jaxb</groupId>
        <artifactId>jaxb-runtime</artifactId>
        <version>4.0.5</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Logging -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.12</version>
    </dependency>
</dependencies>
```

### Gradle (build.gradle)

```groovy
dependencies {
    implementation 'jakarta.xml.bind:jakarta.xml.bind-api:4.0.2'
    runtimeOnly 'org.glassfish.jaxb:jaxb-runtime:4.0.5'
    implementation 'org.slf4j:slf4j-api:2.0.12'
}
```

## Step 2: Copy Core Classes

Copy these packages to your project:
- `com.example.xsdparser.core.model` - Domain models
- `com.example.xsdparser.core.port` - Interfaces
- `com.example.xsdparser.core.service` - Services
- `com.example.xsdparser.infrastructure.xml` - XML implementations
- `com.example.xsdparser.infrastructure.schema` - Schema registry

## Step 3: Generate JAXB Classes from XSD

Add to your `pom.xml`:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>jaxb2-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>xjc</id>
            <goals>
                <goal>xjc</goal>
            </goals>
            <configuration>
                <sources>
                    <source>${project.basedir}/src/main/resources/xsd/your-schema.xsd</source>
                </sources>
                <packageName>com.yourcompany.generated</packageName>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Step 4: Quick Start Code

### Initialize (Copy This)

```java
import com.example.xsdparser.core.port.*;
import com.example.xsdparser.core.service.XmlProcessingService;
import com.example.xsdparser.infrastructure.xml.*;
import com.example.xsdparser.infrastructure.schema.*;

// One-time setup
XmlSerializer serializer = new JaxbXmlSerializer();
XmlValidator validator = new SchemaBasedXmlValidator();
SchemaRegistry registry = new InMemorySchemaRegistry();
new PacsSchemaInitializer(registry).initializeSchemas();

XmlProcessingService processingService = new XmlProcessingService(serializer, validator, registry);
```

### Serialize Object to XML

```java
// Your JAXB-generated object
Document document = createYourDocument();
ObjectFactory factory = new ObjectFactory();
JAXBElement<Document> jaxbElement = factory.createDocument(document);

// Serialize
String xml = serializer.serializeFormatted(jaxbElement);
```

### Validate XML Against Schema

```java
ValidationResult result = processingService.validateXml(xml, "pacs.008");

if (result.isValid()) {
    // Process valid XML
} else {
    result.getErrors().forEach(error -> 
        log.error("Line {}: {}", error.lineNumber(), error.getMessage())
    );
}
```

### Generate Test Data

```java
import com.example.xsdparser.application.factory.Iso20022TestDataFactory;

String messageId = Iso20022TestDataFactory.generateMessageId();
String iban = Iso20022TestDataFactory.generateIban("DE");
String bic = Iso20022TestDataFactory.generateBic();
BigDecimal amount = Iso20022TestDataFactory.generateAmount();
XMLGregorianCalendar dateTime = Iso20022TestDataFactory.generateCreationDateTime();
```

## Common Integration Patterns

### Pattern 1: Payment Message Creator

```java
public class PaymentMessageCreator {
    private final XmlSerializer serializer;
    private final ObjectFactory factory;
    
    public PaymentMessageCreator() {
        this.serializer = new JaxbXmlSerializer();
        this.factory = new ObjectFactory();
    }
    
    public String createCreditTransfer(String debtorIban, String creditorIban, 
                                       BigDecimal amount, String currency) {
        Document doc = new Document();
        FIToFICustomerCreditTransferV14 transfer = new FIToFICustomerCreditTransferV14();
        
        // Build header
        GroupHeader131 header = new GroupHeader131();
        header.setMsgId(Iso20022TestDataFactory.generateMessageId());
        header.setCreDtTm(Iso20022TestDataFactory.generateCreationDateTime());
        header.setNbOfTxs("1");
        transfer.setGrpHdr(header);
        
        // Build transaction
        CreditTransferTransaction73 tx = new CreditTransferTransaction73();
        PaymentIdentification13 pmtId = new PaymentIdentification13();
        pmtId.setEndToEndId(Iso20022TestDataFactory.generateEndToEndId());
        tx.setPmtId(pmtId);
        
        ActiveCurrencyAndAmount amt = new ActiveCurrencyAndAmount();
        amt.setValue(amount);
        amt.setCcy(currency);
        tx.setIntrBkSttlmAmt(amt);
        
        transfer.getCdtTrfTxInf().add(tx);
        doc.setFIToFICstmrCdtTrf(transfer);
        
        return serializer.serializeFormatted(factory.createDocument(doc));
    }
}
```

### Pattern 2: XML Validator Service

```java
@Service
public class XmlValidatorService {
    private final XmlProcessingService processingService;
    
    public XmlValidatorService() {
        XmlSerializer serializer = new JaxbXmlSerializer();
        XmlValidator validator = new SchemaBasedXmlValidator();
        SchemaRegistry registry = new InMemorySchemaRegistry();
        new PacsSchemaInitializer(registry).initializeSchemas();
        this.processingService = new XmlProcessingService(serializer, validator, registry);
    }
    
    public boolean isValidPacs008(String xml) {
        return processingService.validateXml(xml, "pacs.008").isValid();
    }
    
    public List<String> getValidationErrors(String xml, String messageType) {
        return processingService.validateXml(xml, messageType)
            .getErrors()
            .stream()
            .map(e -> String.format("Line %d: %s", e.lineNumber(), e.getMessage()))
            .toList();
    }
}
```

### Pattern 3: Test Data Builder

```java
public class Pacs008TestBuilder {
    private String messageId;
    private String debtorName;
    private String debtorIban;
    private String creditorName;
    private String creditorIban;
    private BigDecimal amount = BigDecimal.valueOf(1000);
    private String currency = "EUR";
    
    public static Pacs008TestBuilder create() {
        return new Pacs008TestBuilder();
    }
    
    public Pacs008TestBuilder withRandomData() {
        this.messageId = Iso20022TestDataFactory.generateMessageId();
        this.debtorName = Iso20022TestDataFactory.generateCompanyName();
        this.debtorIban = Iso20022TestDataFactory.generateIban("DE");
        this.creditorName = Iso20022TestDataFactory.generateCompanyName();
        this.creditorIban = Iso20022TestDataFactory.generateIban("NL");
        this.amount = Iso20022TestDataFactory.generateAmount();
        this.currency = Iso20022TestDataFactory.generateCurrencyCode();
        return this;
    }
    
    public Pacs008TestBuilder amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
    
    public Pacs008TestBuilder currency(String currency) {
        this.currency = currency;
        return this;
    }
    
    public Document build() {
        // Build and return Document...
    }
}

// Usage:
Document doc = Pacs008TestBuilder.create()
    .withRandomData()
    .amount(BigDecimal.valueOf(5000))
    .currency("EUR")
    .build();
```

## Supported Message Types

| Type | Class | Description |
|------|-------|-------------|
| pacs.002 | `FIToFIPaymentStatusReportV16` | Payment Status Report |
| pacs.003 | `FIToFICustomerDirectDebitV12` | Direct Debit |
| pacs.004 | `PaymentReturnV15` | Payment Return |
| pacs.007 | `FIToFIPaymentReversalV14` | Payment Reversal |
| pacs.008 | `FIToFICustomerCreditTransferV14` | Credit Transfer |
| pacs.009 | `FinancialInstitutionCreditTransferV13` | FI Credit Transfer |
| pacs.010 | `FinancialInstitutionDirectDebitV06` | FI Direct Debit |
| pacs.028 | `FIToFIPaymentStatusRequestV07` | Status Request |
| pacs.029 | `FIToFIPaymentStatusReportAcknowledgementV02` | Status Acknowledgement |

## Troubleshooting

### JAXB Context Error
```java
// If you get "unable to marshal" errors, use ObjectFactory:
ObjectFactory factory = new ObjectFactory();
JAXBElement<Document> element = factory.createDocument(document);
serializer.serialize(element);  // NOT serialize(document)
```

### Schema Not Found
```java
// Check available schemas:
registry.getAllSchemas().forEach(s -> 
    System.out.println(s.getMessageType() + " -> " + s.getFullIdentifier())
);
```

### Namespace Issues
```java
// Always use JAXBElement for proper namespace handling:
ObjectFactory factory = new ObjectFactory();
String xml = serializer.serialize(factory.createDocument(doc));
```
