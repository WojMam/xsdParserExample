# Copy-Paste Code Snippets

> Self-contained, ready-to-use code snippets for common tasks.

## 1. Complete Standalone Example

```java
package com.example.demo;

import com.example.xsdparser.application.factory.Iso20022TestDataFactory;
import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.port.SchemaRegistry;
import com.example.xsdparser.core.port.XmlSerializer;
import com.example.xsdparser.core.port.XmlValidator;
import com.example.xsdparser.core.service.XmlProcessingService;
import com.example.xsdparser.generated.pacs008.*;
import com.example.xsdparser.infrastructure.schema.InMemorySchemaRegistry;
import com.example.xsdparser.infrastructure.schema.PacsSchemaInitializer;
import com.example.xsdparser.infrastructure.xml.JaxbXmlSerializer;
import com.example.xsdparser.infrastructure.xml.SchemaBasedXmlValidator;
import jakarta.xml.bind.JAXBElement;

import java.math.BigDecimal;

public class Pacs008Demo {
    
    public static void main(String[] args) {
        // Setup
        XmlSerializer serializer = new JaxbXmlSerializer();
        XmlValidator validator = new SchemaBasedXmlValidator();
        SchemaRegistry registry = new InMemorySchemaRegistry();
        new PacsSchemaInitializer(registry).initializeSchemas();
        XmlProcessingService service = new XmlProcessingService(serializer, validator, registry);
        
        // Create document
        Document doc = new Document();
        FIToFICustomerCreditTransferV14 transfer = new FIToFICustomerCreditTransferV14();
        
        GroupHeader131 header = new GroupHeader131();
        header.setMsgId(Iso20022TestDataFactory.generateMessageId());
        header.setCreDtTm(Iso20022TestDataFactory.generateCreationDateTime());
        header.setNbOfTxs("1");
        
        SettlementInstruction15 settlement = new SettlementInstruction15();
        settlement.setSttlmMtd(SettlementMethod1Code.CLRG);
        header.setSttlmInf(settlement);
        transfer.setGrpHdr(header);
        
        CreditTransferTransaction73 tx = new CreditTransferTransaction73();
        PaymentIdentification13 pmtId = new PaymentIdentification13();
        pmtId.setInstrId(Iso20022TestDataFactory.generateInstructionId());
        pmtId.setEndToEndId(Iso20022TestDataFactory.generateEndToEndId());
        tx.setPmtId(pmtId);
        
        ActiveCurrencyAndAmount amount = new ActiveCurrencyAndAmount();
        amount.setValue(BigDecimal.valueOf(1000.00));
        amount.setCcy("EUR");
        tx.setIntrBkSttlmAmt(amount);
        
        transfer.getCdtTrfTxInf().add(tx);
        doc.setFIToFICstmrCdtTrf(transfer);
        
        // Serialize
        ObjectFactory factory = new ObjectFactory();
        JAXBElement<Document> element = factory.createDocument(doc);
        String xml = serializer.serializeFormatted(element);
        
        System.out.println(xml);
        
        // Validate
        ValidationResult result = service.validateXml(xml, "pacs.008");
        System.out.println("Valid: " + result.isValid());
    }
}
```

## 2. Message Builder Utility

```java
package com.example.util;

import com.example.xsdparser.application.factory.Iso20022TestDataFactory;
import com.example.xsdparser.generated.pacs008.*;
import com.example.xsdparser.infrastructure.xml.JaxbXmlSerializer;
import jakarta.xml.bind.JAXBElement;

import java.math.BigDecimal;

public class Pacs008Builder {
    private final Document document;
    private final FIToFICustomerCreditTransferV14 transfer;
    private final GroupHeader131 header;
    private final ObjectFactory factory;
    
    public Pacs008Builder() {
        this.document = new Document();
        this.transfer = new FIToFICustomerCreditTransferV14();
        this.header = new GroupHeader131();
        this.factory = new ObjectFactory();
        
        // Initialize with defaults
        header.setMsgId(Iso20022TestDataFactory.generateMessageId());
        header.setCreDtTm(Iso20022TestDataFactory.generateCreationDateTime());
        header.setNbOfTxs("0");
        
        SettlementInstruction15 settlement = new SettlementInstruction15();
        settlement.setSttlmMtd(SettlementMethod1Code.CLRG);
        header.setSttlmInf(settlement);
        
        transfer.setGrpHdr(header);
        document.setFIToFICstmrCdtTrf(transfer);
    }
    
    public Pacs008Builder withMessageId(String messageId) {
        header.setMsgId(messageId);
        return this;
    }
    
    public Pacs008Builder addTransaction(BigDecimal amount, String currency) {
        CreditTransferTransaction73 tx = new CreditTransferTransaction73();
        
        PaymentIdentification13 pmtId = new PaymentIdentification13();
        pmtId.setInstrId(Iso20022TestDataFactory.generateInstructionId());
        pmtId.setEndToEndId(Iso20022TestDataFactory.generateEndToEndId());
        pmtId.setTxId(Iso20022TestDataFactory.generateTransactionId());
        tx.setPmtId(pmtId);
        
        ActiveCurrencyAndAmount amt = new ActiveCurrencyAndAmount();
        amt.setValue(amount);
        amt.setCcy(currency);
        tx.setIntrBkSttlmAmt(amt);
        
        transfer.getCdtTrfTxInf().add(tx);
        header.setNbOfTxs(String.valueOf(transfer.getCdtTrfTxInf().size()));
        
        return this;
    }
    
    public Document build() {
        return document;
    }
    
    public String toXml() {
        JAXBElement<Document> element = factory.createDocument(document);
        return new JaxbXmlSerializer().serializeFormatted(element);
    }
}

// Usage:
// String xml = new Pacs008Builder()
//     .addTransaction(BigDecimal.valueOf(1000), "EUR")
//     .addTransaction(BigDecimal.valueOf(2500), "USD")
//     .toXml();
```

## 3. Validation Service

```java
package com.example.service;

import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.port.SchemaRegistry;
import com.example.xsdparser.core.port.XmlValidator;
import com.example.xsdparser.infrastructure.schema.InMemorySchemaRegistry;
import com.example.xsdparser.infrastructure.schema.PacsSchemaInitializer;
import com.example.xsdparser.infrastructure.xml.SchemaBasedXmlValidator;

import java.util.List;
import java.util.stream.Collectors;

public class PaymentValidationService {
    private final XmlValidator validator;
    private final SchemaRegistry registry;
    
    public PaymentValidationService() {
        this.validator = new SchemaBasedXmlValidator();
        this.registry = new InMemorySchemaRegistry();
        new PacsSchemaInitializer(registry).initializeSchemas();
    }
    
    public boolean isValid(String xml, String messageType) {
        return registry.findByMessageType(messageType)
            .map(schema -> validator.validate(xml, schema).isValid())
            .orElse(false);
    }
    
    public List<String> getErrors(String xml, String messageType) {
        return registry.findByMessageType(messageType)
            .map(schema -> validator.validate(xml, schema))
            .map(result -> result.getErrors().stream()
                .map(e -> String.format("[Line %d] %s", e.lineNumber(), e.getMessage()))
                .collect(Collectors.toList()))
            .orElse(List.of("Unknown message type: " + messageType));
    }
    
    public List<String> getSupportedMessageTypes() {
        return registry.getAllSchemas().stream()
            .map(s -> s.getMessageType())
            .sorted()
            .collect(Collectors.toList());
    }
}
```

## 4. Test Data Generator

```java
package com.example.testdata;

import com.example.xsdparser.application.factory.Iso20022TestDataFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

public class TestDataGenerator {
    
    // Generate a complete set of test data for one payment
    public static PaymentTestData generate() {
        return new PaymentTestData(
            Iso20022TestDataFactory.generateMessageId(),
            Iso20022TestDataFactory.generateInstructionId(),
            Iso20022TestDataFactory.generateEndToEndId(),
            Iso20022TestDataFactory.generateTransactionId(),
            Iso20022TestDataFactory.generateIban("DE"),
            Iso20022TestDataFactory.generateIban("NL"),
            Iso20022TestDataFactory.generateBic(),
            Iso20022TestDataFactory.generateBic(),
            Iso20022TestDataFactory.generateAmount(),
            Iso20022TestDataFactory.generateCurrencyCode(),
            Iso20022TestDataFactory.generateCreationDateTime(),
            Iso20022TestDataFactory.generateCompanyName(),
            Iso20022TestDataFactory.generateCompanyName()
        );
    }
    
    public record PaymentTestData(
        String messageId,
        String instructionId,
        String endToEndId,
        String transactionId,
        String debtorIban,
        String creditorIban,
        String debtorBic,
        String creditorBic,
        BigDecimal amount,
        String currency,
        XMLGregorianCalendar creationDateTime,
        String debtorName,
        String creditorName
    ) {}
}
```

## 5. Spring Boot Auto-Configuration

```java
package com.example.config;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Iso20022Configuration {
    
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
    
    @Bean
    public XmlConversionUseCase xmlConversionUseCase(XmlProcessingService service) {
        return new XmlConversionUseCase(service);
    }
    
    @Bean
    public SchemaValidationUseCase schemaValidationUseCase(XmlProcessingService service) {
        return new SchemaValidationUseCase(service);
    }
}
```

## 6. JUnit 5 Test Template

```java
package com.example.test;

import com.example.xsdparser.XsdParserConfig;
import com.example.xsdparser.application.factory.Iso20022TestDataFactory;
import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.port.XmlSerializer;
import com.example.xsdparser.generated.pacs008.*;
import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class Pacs008Test {
    
    private static XsdParserConfig config;
    private static ObjectFactory factory;
    
    @BeforeAll
    static void setUp() {
        config = XsdParserConfig.createIsolated();
        factory = new ObjectFactory();
    }
    
    @Test
    void shouldCreateValidPacs008() {
        // Given
        Document doc = createTestDocument();
        JAXBElement<Document> element = factory.createDocument(doc);
        
        // When
        String xml = config.getSerializer().serializeFormatted(element);
        ValidationResult result = config.getValidationUseCase()
            .validateAgainstResource(xml, "xsd/pacs.008.001.14.xsd");
        
        // Then
        assertThat(xml).isNotBlank();
        assertThat(xml).contains("FIToFICstmrCdtTrf");
    }
    
    @Test
    void shouldGenerateUniqueMessageIds() {
        String id1 = Iso20022TestDataFactory.generateMessageId();
        String id2 = Iso20022TestDataFactory.generateMessageId();
        
        assertThat(id1).isNotEqualTo(id2);
    }
    
    private Document createTestDocument() {
        Document doc = new Document();
        FIToFICustomerCreditTransferV14 transfer = new FIToFICustomerCreditTransferV14();
        
        GroupHeader131 header = new GroupHeader131();
        header.setMsgId(Iso20022TestDataFactory.generateMessageId());
        header.setCreDtTm(Iso20022TestDataFactory.generateCreationDateTime());
        header.setNbOfTxs("1");
        
        SettlementInstruction15 settlement = new SettlementInstruction15();
        settlement.setSttlmMtd(SettlementMethod1Code.CLRG);
        header.setSttlmInf(settlement);
        transfer.setGrpHdr(header);
        
        CreditTransferTransaction73 tx = new CreditTransferTransaction73();
        PaymentIdentification13 pmtId = new PaymentIdentification13();
        pmtId.setEndToEndId(Iso20022TestDataFactory.generateEndToEndId());
        tx.setPmtId(pmtId);
        
        ActiveCurrencyAndAmount amount = new ActiveCurrencyAndAmount();
        amount.setValue(BigDecimal.valueOf(1000));
        amount.setCcy("EUR");
        tx.setIntrBkSttlmAmt(amount);
        
        transfer.getCdtTrfTxInf().add(tx);
        doc.setFIToFICstmrCdtTrf(transfer);
        
        return doc;
    }
}
```

## 7. REST API Example

```java
package com.example.api;

import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.service.XmlProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iso20022")
public class Iso20022Controller {
    
    private final XmlProcessingService processingService;
    
    public Iso20022Controller(XmlProcessingService processingService) {
        this.processingService = processingService;
    }
    
    @PostMapping("/validate/{messageType}")
    public ResponseEntity<?> validate(
            @PathVariable String messageType,
            @RequestBody String xml) {
        
        ValidationResult result = processingService.validateXml(xml, messageType);
        
        if (result.isValid()) {
            return ResponseEntity.ok(Map.of("valid", true));
        }
        
        List<Map<String, Object>> errors = result.getErrors().stream()
            .map(e -> Map.<String, Object>of(
                "line", e.lineNumber(),
                "column", e.columnNumber(),
                "message", e.getMessage(),
                "severity", e.severity().name()
            ))
            .toList();
        
        return ResponseEntity.badRequest().body(Map.of(
            "valid", false,
            "errors", errors
        ));
    }
    
    @GetMapping("/schemas")
    public List<String> listSchemas() {
        return processingService.getSchemaRegistry().getAllSchemas().stream()
            .map(s -> s.getFullIdentifier())
            .sorted()
            .toList();
    }
}
```
