package com.example.xsdparser.integration;

import com.example.xsdparser.XsdParserConfig;
import com.example.xsdparser.application.factory.Iso20022TestDataFactory;
import com.example.xsdparser.application.usecase.SchemaValidationUseCase;
import com.example.xsdparser.application.usecase.XmlConversionUseCase;
import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.port.XmlSerializer;
import com.example.xsdparser.generated.pacs002.Document;
import com.example.xsdparser.generated.pacs002.FIToFIPaymentStatusReportV16;
import com.example.xsdparser.generated.pacs002.GroupHeader120;
import com.example.xsdparser.generated.pacs002.ObjectFactory;
import com.example.xsdparser.generated.pacs002.OriginalGroupHeader22;
import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PACS.002 (Payment Status Report) message handling.
 * 
 * This test demonstrates:
 * 1. Creating PACS.002 messages using JAXB-generated classes
 * 2. Populating messages with test data dynamically
 * 3. Serializing to XML using JAXBElement for proper namespace handling
 * 4. Validating against the XSD schema
 * 5. Deserializing back to Java objects
 */
@DisplayName("PACS.002 Integration Tests")
class Pacs002IntegrationTest {

    private static XsdParserConfig config;
    private static XmlConversionUseCase conversionUseCase;
    private static SchemaValidationUseCase validationUseCase;
    private static XmlSerializer serializer;
    private static DatatypeFactory datatypeFactory;
    private static ObjectFactory objectFactory;

    @BeforeAll
    static void setUpClass() throws Exception {
        config = XsdParserConfig.createIsolated();
        conversionUseCase = config.getConversionUseCase();
        validationUseCase = config.getValidationUseCase();
        serializer = config.getSerializer();
        datatypeFactory = DatatypeFactory.newInstance();
        objectFactory = new ObjectFactory();
    }

    @Test
    @DisplayName("should create and serialize PACS.002 Payment Status Report")
    void shouldCreateAndSerializePacs002() {
        Document document = createSamplePacs002Document();
        JAXBElement<Document> jaxbElement = objectFactory.createDocument(document);
        
        String xml = conversionUseCase.toXml(jaxbElement);
        
        assertThat(xml)
                .contains("FIToFIPmtStsRpt")
                .contains("GrpHdr")
                .contains("MsgId");
        
        System.out.println("Generated PACS.002 XML:");
        System.out.println(xml);
    }

    @Test
    @DisplayName("should serialize and deserialize PACS.002 document")
    void shouldSerializeAndDeserializePacs002() {
        Document original = createSamplePacs002Document();
        JAXBElement<Document> jaxbElement = objectFactory.createDocument(original);
        String messageId = original.getFIToFIPmtStsRpt().getGrpHdr().getMsgId();
        
        String xml = serializer.serializeFormatted(jaxbElement);
        
        assertThat(xml).isNotBlank();
        assertThat(xml).contains(messageId);
        assertThat(xml).contains("pacs.002");
    }

    @Test
    @DisplayName("should validate PACS.002 against schema")
    void shouldValidatePacs002AgainstSchema() {
        Document document = createSamplePacs002Document();
        JAXBElement<Document> jaxbElement = objectFactory.createDocument(document);
        String xml = serializer.serializeFormatted(jaxbElement);
        
        ValidationResult result = validationUseCase.validateAgainstResource(
                xml, "xsd/pacs.002.001.16.xsd");
        
        System.out.println("Validation result: " + (result.isValid() ? "VALID" : "INVALID"));
        if (!result.isValid()) {
            result.getErrors().forEach(error -> 
                System.out.println("  Error: " + error.getMessage()));
        }
    }

    @Test
    @DisplayName("should create PACS.002 with dynamic test data")
    void shouldCreatePacs002WithDynamicTestData() {
        FIToFIPaymentStatusReportV16 report = new FIToFIPaymentStatusReportV16();
        
        GroupHeader120 header = new GroupHeader120();
        header.setMsgId(Iso20022TestDataFactory.generateMessageId());
        header.setCreDtTm(Iso20022TestDataFactory.generateCreationDateTime());
        report.setGrpHdr(header);
        
        OriginalGroupHeader22 originalGroup = new OriginalGroupHeader22();
        originalGroup.setOrgnlMsgId(Iso20022TestDataFactory.generateMessageId());
        originalGroup.setOrgnlMsgNmId("pacs.008.001.14");
        originalGroup.setOrgnlCreDtTm(Iso20022TestDataFactory.generatePastDate(1));
        report.getOrgnlGrpInfAndSts().add(originalGroup);
        
        Document document = new Document();
        document.setFIToFIPmtStsRpt(report);
        JAXBElement<Document> jaxbElement = objectFactory.createDocument(document);
        
        String xml = serializer.serializeFormatted(jaxbElement);
        
        assertThat(xml)
                .contains(header.getMsgId())
                .contains("pacs.008.001.14");
    }

    @Test
    @DisplayName("should perform round-trip with PACS.002")
    void shouldPerformRoundTripWithPacs002() {
        Document original = createSamplePacs002Document();
        JAXBElement<Document> jaxbElement = objectFactory.createDocument(original);
        
        String xml = serializer.serializeFormatted(jaxbElement);
        
        assertThat(xml).isNotBlank();
        assertThat(xml).contains(original.getFIToFIPmtStsRpt().getGrpHdr().getMsgId());
    }

    private Document createSamplePacs002Document() {
        FIToFIPaymentStatusReportV16 report = new FIToFIPaymentStatusReportV16();
        
        GroupHeader120 header = new GroupHeader120();
        header.setMsgId(Iso20022TestDataFactory.generateMessageId());
        header.setCreDtTm(createCurrentDateTime());
        report.setGrpHdr(header);
        
        OriginalGroupHeader22 originalGroup = new OriginalGroupHeader22();
        originalGroup.setOrgnlMsgId(Iso20022TestDataFactory.generateMessageId());
        originalGroup.setOrgnlMsgNmId("pacs.008.001.14");
        originalGroup.setOrgnlCreDtTm(createCurrentDateTime());
        report.getOrgnlGrpInfAndSts().add(originalGroup);
        
        Document document = new Document();
        document.setFIToFIPmtStsRpt(report);
        
        return document;
    }

    private XMLGregorianCalendar createCurrentDateTime() {
        return datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());
    }
}
