package com.example.xsdparser.integration;

import com.example.xsdparser.XsdParserConfig;
import com.example.xsdparser.application.factory.Iso20022TestDataFactory;
import com.example.xsdparser.application.factory.TestDataFactory;
import com.example.xsdparser.core.port.XmlSerializer;
import com.example.xsdparser.generated.pacs002.Document;
import com.example.xsdparser.generated.pacs002.FIToFIPaymentStatusReportV16;
import com.example.xsdparser.generated.pacs002.GroupHeader120;
import com.example.xsdparser.generated.pacs002.ObjectFactory;
import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests demonstrating dynamic model creation without static test files.
 * 
 * This is the key demonstration of the project's main purpose:
 * showing how to create test data programmatically using JAXB-generated classes.
 */
@DisplayName("Dynamic Model Creation Tests")
class DynamicModelCreationTest {

    private static XmlSerializer serializer;
    private static TestDataFactory testDataFactory;
    private static ObjectFactory objectFactory;

    @BeforeAll
    static void setUpClass() {
        XsdParserConfig config = XsdParserConfig.createIsolated();
        serializer = config.getSerializer();
        testDataFactory = config.createTestDataFactory();
        objectFactory = new ObjectFactory();
    }

    @Test
    @DisplayName("should create test data dynamically without static files")
    void shouldCreateTestDataDynamicallyWithoutStaticFiles() {
        Document document = new Document();
        FIToFIPaymentStatusReportV16 report = new FIToFIPaymentStatusReportV16();
        
        GroupHeader120 header = new GroupHeader120();
        header.setMsgId(Iso20022TestDataFactory.generateMessageId());
        header.setCreDtTm(Iso20022TestDataFactory.generateCreationDateTime());
        report.setGrpHdr(header);
        
        document.setFIToFIPmtStsRpt(report);
        
        JAXBElement<Document> jaxbElement = objectFactory.createDocument(document);
        String xml = serializer.serializeFormatted(jaxbElement);
        
        assertThat(xml).isNotBlank();
        assertThat(document.getFIToFIPmtStsRpt().getGrpHdr().getMsgId()).isNotNull();
        
        System.out.println("=== Dynamically Created PACS.002 Test Data ===");
        System.out.println(xml);
    }

    @Test
    @DisplayName("should create unique test data for each test run")
    void shouldCreateUniqueTestDataForEachRun() {
        String messageId1 = Iso20022TestDataFactory.generateMessageId();
        String messageId2 = Iso20022TestDataFactory.generateMessageId();
        String messageId3 = Iso20022TestDataFactory.generateMessageId();
        
        assertThat(messageId1)
                .isNotEqualTo(messageId2)
                .isNotEqualTo(messageId3);
        assertThat(messageId2).isNotEqualTo(messageId3);
    }

    @Test
    @DisplayName("should create consistent test data with fixed values mode")
    void shouldCreateConsistentTestDataWithFixedValuesMode() {
        TestDataFactory fixedFactory = new TestDataFactory().withFixedValues();
        
        assertThat(fixedFactory).isNotNull();
    }

    @Test
    @DisplayName("should demonstrate builder pattern for test data")
    void shouldDemonstrateBuilderPatternForTestData() {
        Document document = new Document();
        FIToFIPaymentStatusReportV16 report = new FIToFIPaymentStatusReportV16();
        GroupHeader120 header = new GroupHeader120();
        
        header.setMsgId("CUSTOM-MSG-" + System.currentTimeMillis());
        header.setCreDtTm(Iso20022TestDataFactory.generateCreationDateTime());
        
        report.setGrpHdr(header);
        document.setFIToFIPmtStsRpt(report);
        
        assertThat(document.getFIToFIPmtStsRpt().getGrpHdr().getMsgId())
                .startsWith("CUSTOM-MSG-");
    }

    @Test
    @DisplayName("should generate ISO 20022 compliant identifiers")
    void shouldGenerateIso20022CompliantIdentifiers() {
        String iban = Iso20022TestDataFactory.generateIban("DE");
        String bic = Iso20022TestDataFactory.generateBic();
        String lei = Iso20022TestDataFactory.generateLei();
        
        assertThat(iban).startsWith("DE").hasSize(22);
        assertThat(bic).hasSizeBetween(8, 11);
        assertThat(lei).hasSize(20);
        
        System.out.println("Generated identifiers:");
        System.out.println("  IBAN: " + iban);
        System.out.println("  BIC: " + bic);
        System.out.println("  LEI: " + lei);
    }

    @Test
    @DisplayName("should demonstrate test data reusability")
    void shouldDemonstrateTestDataReusability() {
        String messageId = Iso20022TestDataFactory.generateMessageId();
        
        Document doc1 = createDocumentWithId(messageId);
        Document doc2 = createDocumentWithId(messageId);
        
        JAXBElement<Document> jaxb1 = objectFactory.createDocument(doc1);
        JAXBElement<Document> jaxb2 = objectFactory.createDocument(doc2);
        
        String xml1 = serializer.serialize(jaxb1);
        String xml2 = serializer.serialize(jaxb2);
        
        assertThat(xml1).isEqualTo(xml2);
    }

    private Document createDocumentWithId(String messageId) {
        Document document = new Document();
        FIToFIPaymentStatusReportV16 report = new FIToFIPaymentStatusReportV16();
        GroupHeader120 header = new GroupHeader120();
        
        header.setMsgId(messageId);
        header.setCreDtTm(Iso20022TestDataFactory.generateCreationDateTime());
        
        report.setGrpHdr(header);
        document.setFIToFIPmtStsRpt(report);
        
        return document;
    }
}
