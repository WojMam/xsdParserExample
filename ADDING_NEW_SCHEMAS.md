# How to Add New XSD Schemas

This guide explains how to add new XSD schemas to the project. The library is designed to be flexible and supports multiple ways to add schemas.

## Method 1: Auto-Discovery (Easiest)

The simplest way to add a new schema - just add the XSD file and configure JAXB:

### Step 1: Add XSD File

Place your XSD file in `src/main/resources/xsd/` with the naming convention:
```
{message-family}.{number}.{version}.xsd
```

Examples:
- `pacs.008.001.14.xsd` (PACS Credit Transfer)
- `pain.001.001.12.xsd` (PAIN Customer Credit Transfer Initiation)
- `camt.053.001.11.xsd` (CAMT Bank to Customer Statement)

### Step 2: Add JAXB Generation to pom.xml

Add a new execution block to the `jaxb2-maven-plugin`:

```xml
<execution>
    <id>xjc-your-schema</id>
    <goals>
        <goal>xjc</goal>
    </goals>
    <configuration>
        <sources>
            <source>${project.basedir}/src/main/resources/xsd/your-schema.xsd</source>
        </sources>
        <packageName>com.example.xsdparser.generated.yourpackage</packageName>
        <clearOutputDir>false</clearOutputDir>
    </configuration>
</execution>
```

### Step 3: Use Auto-Discovery

```java
// Schemas are automatically detected!
XsdParserConfig config = XsdParserConfig.createWithAutoDiscovery();

// Or use the builder
XsdParserConfig config = XsdParserConfig.builder()
    .withAutoDiscovery()
    .build();
```

**That's it!** The schema will be automatically discovered and registered.

---

## Method 2: Programmatic Registration

If you need more control or want to add schemas without auto-discovery:

```java
XsdParserConfig config = XsdParserConfig.builder()
    .addPacsSchema("008", "001.14", "FIToFI Customer Credit Transfer")
    .addPainSchema("001", "001.12", "Customer Credit Transfer Initiation")
    .addCamtSchema("053", "001.11", "Bank to Customer Statement")
    .build();
```

Or with full SchemaDefinition:

```java
SchemaDefinition customSchema = SchemaDefinition.builder()
    .messageType("acmt.007")
    .version("001.03")
    .description("Account Opening Request")
    .namespacePrefix("urn:iso:std:iso:20022:tech:xsd:")  // optional, this is the default
    .build();

XsdParserConfig config = XsdParserConfig.builder()
    .withAutoDiscovery()
    .addSchema(customSchema)
    .build();
```

---

## Method 3: Using FlexibleSchemaInitializer Directly

For maximum control:

```java
SchemaRegistry registry = new InMemorySchemaRegistry();

FlexibleSchemaInitializer.builder(registry)
    .autoDiscover(true)
    .xsdResourcePath("xsd")  // custom path if needed
    .addPacsSchema("008", "001.14", "Credit Transfer")
    .addSchema(SchemaDefinition.builder()
        .messageType("pain.001")
        .version("001.12")
        .description("Customer Credit Transfer")
        .build())
    .build()
    .initialize();
```

---

## Quick Reference: Common Schema Types

### PACS (Payments Clearing and Settlement)
```java
.addPacsSchema("002", "001.16", "Payment Status Report")
.addPacsSchema("003", "001.12", "Direct Debit")
.addPacsSchema("004", "001.15", "Payment Return")
.addPacsSchema("007", "001.14", "Payment Reversal")
.addPacsSchema("008", "001.14", "Credit Transfer")
.addPacsSchema("009", "001.13", "FI Credit Transfer")
.addPacsSchema("010", "001.06", "FI Direct Debit")
.addPacsSchema("028", "001.07", "Status Request")
.addPacsSchema("029", "001.02", "Status Acknowledgement")
```

### PAIN (Payment Initiation)
```java
.addPainSchema("001", "001.12", "Customer Credit Transfer Initiation")
.addPainSchema("002", "001.14", "Customer Payment Status Report")
.addPainSchema("007", "001.12", "Customer Payment Reversal")
.addPainSchema("008", "001.11", "Customer Direct Debit Initiation")
```

### CAMT (Cash Management)
```java
.addCamtSchema("052", "001.11", "Bank to Customer Account Report")
.addCamtSchema("053", "001.11", "Bank to Customer Statement")
.addCamtSchema("054", "001.11", "Bank to Customer Debit Credit Notification")
.addCamtSchema("056", "001.10", "FI to FI Payment Cancellation Request")
.addCamtSchema("029", "001.12", "Resolution of Investigation")
```

---

## Complete Example: Adding pain.001

### 1. Add XSD File
Save `pain.001.001.12.xsd` to `src/main/resources/xsd/`

### 2. Update pom.xml
```xml
<execution>
    <id>xjc-pain001</id>
    <goals>
        <goal>xjc</goal>
    </goals>
    <configuration>
        <sources>
            <source>${project.basedir}/src/main/resources/xsd/pain.001.001.12.xsd</source>
        </sources>
        <packageName>com.example.xsdparser.generated.pain001</packageName>
        <clearOutputDir>false</clearOutputDir>
    </configuration>
</execution>
```

### 3. Run Maven
```bash
mvn clean compile
```

### 4. Use the New Schema
```java
// With auto-discovery (recommended)
XsdParserConfig config = XsdParserConfig.createWithAutoDiscovery();

// Create a document using generated classes
import com.example.xsdparser.generated.pain001.*;

Document doc = new Document();
CustomerCreditTransferInitiationV12 initiation = new CustomerCreditTransferInitiationV12();
// ... build your document

// Serialize
ObjectFactory factory = new ObjectFactory();
String xml = config.getSerializer().serializeFormatted(factory.createDocument(doc));

// Validate
ValidationResult result = config.getProcessingService().validateXml(xml, "pain.001");
```

---

## Supported Namespace Patterns

The auto-discovery supports these filename patterns:
- `pacs.XXX.YYY.ZZ.xsd` → message type: `pacs.XXX`, version: `YYY.ZZ`
- `pain.XXX.YYY.ZZ.xsd` → message type: `pain.XXX`, version: `YYY.ZZ`
- `camt.XXX.YYY.ZZ.xsd` → message type: `camt.XXX`, version: `YYY.ZZ`
- Any `{family}.{number}.{major}.{minor}.xsd` pattern

Default namespace prefix: `urn:iso:std:iso:20022:tech:xsd:`

---

## Troubleshooting

### Schema not found after adding XSD
1. Verify the filename follows the pattern: `family.number.major.minor.xsd`
2. Run `mvn clean compile` to regenerate JAXB classes
3. Check that auto-discovery is enabled: `XsdParserConfig.createWithAutoDiscovery()`

### JAXB generation fails
1. Ensure the XSD is valid
2. Check for namespace conflicts with existing schemas
3. Try adding `<clearOutputDir>false</clearOutputDir>` to your execution

### Namespace not recognized
Provide a custom namespace prefix:
```java
SchemaDefinition.builder()
    .messageType("custom.001")
    .version("001.00")
    .namespacePrefix("http://my.custom.namespace/")
    .build();
```

### List registered schemas
```java
config.getSchemaRegistry().getAllSchemas().forEach(schema -> 
    System.out.println(schema.getFullIdentifier() + " -> " + schema.getNamespace())
);
```
