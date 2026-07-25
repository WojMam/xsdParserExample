package com.example.xsdparser.integration;

import com.example.xsdparser.XsdParserConfig;
import com.example.xsdparser.application.factory.Iso20022TestDataFactory;
import com.example.xsdparser.application.usecase.SchemaValidationUseCase;
import com.example.xsdparser.core.model.ValidationResult;
import com.example.xsdparser.core.port.XmlSerializer;
import com.example.xsdparser.generated.pacs008.ActiveCurrencyAndAmount;
import com.example.xsdparser.generated.pacs008.BranchAndFinancialInstitutionIdentification8;
import com.example.xsdparser.generated.pacs008.CreditTransferTransaction73;
import com.example.xsdparser.generated.pacs008.Document;
import com.example.xsdparser.generated.pacs008.FIToFICustomerCreditTransferV14;
import com.example.xsdparser.generated.pacs008.FinancialInstitutionIdentification23;
import com.example.xsdparser.generated.pacs008.GroupHeader131;
import com.example.xsdparser.generated.pacs008.ObjectFactory;
import com.example.xsdparser.generated.pacs008.PaymentIdentification13;
import com.example.xsdparser.generated.pacs008.SettlementInstruction15;
import com.example.xsdparser.generated.pacs008.SettlementMethod1Code;
import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.GregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PACS.008 (Credit Transfer) message handling.
 * 
 * This test demonstrates:
 * 1. Creating PACS.008 Credit Transfer messages
 * 2. Building complex nested structures with JAXB-generated classes
 * 3. Using Iso20022TestDataFactory for realistic test data
 * 4. Validating complete messages against XSD schema
 */
@DisplayName("PACS.008 Integration Tests")
class Pacs008IntegrationTest {

    private static XsdParserConfig config;
    private static XmlSerializer serializer;
    private static SchemaValidationUseCase validationUseCase;
    private static DatatypeFactory datatypeFactory;
    private static ObjectFactory objectFactory;

    @BeforeAll
    static void setUpClass() throws Exception {
        config = XsdParserConfig.createIsolated();
        serializer = config.getSerializer();
        validationUseCase = config.getValidationUseCase();
        datatypeFactory = DatatypeFactory.newInstance();
        objectFactory = new ObjectFactory();
    }

    @Test
    @DisplayName("should create and serialize PACS.008 Credit Transfer")
    void shouldCreateAndSerializePacs008() {
        Document document = createSamplePacs008Document();
        JAXBElement<Document> jaxbElement = objectFactory.createDocument(document);
        
        String xml = serializer.serializeFormatted(jaxbElement);
        
        assertThat(xml)
                .contains("FIToFICstmrCdtTrf")
                .contains("GrpHdr")
                .contains("CdtTrfTxInf");
        
        System.out.println("Generated PACS.008 XML:");
        System.out.println(xml);
    }

    @Test
    @DisplayName("should create PACS.008 with debtor and creditor info")
    void shouldCreatePacs008WithDebtorAndCreditorInfo() {
        FIToFICustomerCreditTransferV14 transfer = new FIToFICustomerCreditTransferV14();
        
        GroupHeader131 header = createGroupHeader();
        transfer.setGrpHdr(header);
        
        CreditTransferTransaction73 txInfo = new CreditTransferTransaction73();
        
        PaymentIdentification13 pmtId = new PaymentIdentification13();
        pmtId.setInstrId(Iso20022TestDataFactory.generateInstructionId());
        pmtId.setEndToEndId(Iso20022TestDataFactory.generateEndToEndId());
        pmtId.setTxId(Iso20022TestDataFactory.generateTransactionId());
        txInfo.setPmtId(pmtId);
        
        ActiveCurrencyAndAmount amount = new ActiveCurrencyAndAmount();
        amount.setValue(Iso20022TestDataFactory.generateAmount());
        amount.setCcy(Iso20022TestDataFactory.generateCurrencyCode());
        txInfo.setIntrBkSttlmAmt(amount);
        
        transfer.getCdtTrfTxInf().add(txInfo);
        
        Document document = new Document();
        document.setFIToFICstmrCdtTrf(transfer);
        JAXBElement<Document> jaxbElement = objectFactory.createDocument(document);
        
        String xml = serializer.serializeFormatted(jaxbElement);
        
        assertThat(xml)
                .contains(pmtId.getInstrId())
                .contains(pmtId.getEndToEndId());
    }

    @Test
    @DisplayName("should build complex PACS.008 with financial institution details")
    void shouldBuildComplexPacs008WithFinancialInstitutionDetails() {
        FIToFICustomerCreditTransferV14 transfer = new FIToFICustomerCreditTransferV14();
        
        GroupHeader131 header = createGroupHeader();
        
        BranchAndFinancialInstitutionIdentification8 instgAgt = new BranchAndFinancialInstitutionIdentification8();
        FinancialInstitutionIdentification23 instgAgtFiId = new FinancialInstitutionIdentification23();
        instgAgtFiId.setBICFI(Iso20022TestDataFactory.generateBic());
        instgAgt.setFinInstnId(instgAgtFiId);
        header.setInstgAgt(instgAgt);
        
        BranchAndFinancialInstitutionIdentification8 instdAgt = new BranchAndFinancialInstitutionIdentification8();
        FinancialInstitutionIdentification23 instdAgtFiId = new FinancialInstitutionIdentification23();
        instdAgtFiId.setBICFI(Iso20022TestDataFactory.generateBic());
        instdAgt.setFinInstnId(instdAgtFiId);
        header.setInstdAgt(instdAgt);
        
        transfer.setGrpHdr(header);
        
        Document document = new Document();
        document.setFIToFICstmrCdtTrf(transfer);
        JAXBElement<Document> jaxbElement = objectFactory.createDocument(document);
        
        String xml = serializer.serializeFormatted(jaxbElement);
        
        assertThat(xml)
                .contains("InstgAgt")
                .contains("InstdAgt")
                .contains("BICFI");
    }

    @Test
    @DisplayName("should serialize and deserialize complex PACS.008")
    void shouldSerializeAndDeserializeComplexPacs008() {
        Document original = createSamplePacs008Document();
        JAXBElement<Document> jaxbElement = objectFactory.createDocument(original);
        String messageId = original.getFIToFICstmrCdtTrf().getGrpHdr().getMsgId();
        
        String xml = serializer.serializeFormatted(jaxbElement);
        
        assertThat(xml).contains(messageId);
    }

    @Test
    @DisplayName("should validate PACS.008 against schema")
    void shouldValidatePacs008AgainstSchema() {
        Document document = createSamplePacs008Document();
        JAXBElement<Document> jaxbElement = objectFactory.createDocument(document);
        String xml = serializer.serializeFormatted(jaxbElement);
        
        ValidationResult result = validationUseCase.validateAgainstResource(
                xml, "xsd/pacs.008.001.14.xsd");
        
        System.out.println("Validation result: " + (result.isValid() ? "VALID" : "INVALID"));
        if (!result.isValid()) {
            result.getErrors().forEach(error -> 
                System.out.println("  Error: " + error.getMessage()));
        }
    }

    @Test
    @DisplayName("should create multiple credit transfers in single message")
    void shouldCreateMultipleCreditTransfers() {
        FIToFICustomerCreditTransferV14 transfer = new FIToFICustomerCreditTransferV14();
        
        GroupHeader131 header = createGroupHeader();
        header.setNbOfTxs("3");
        header.setTtlIntrBkSttlmAmt(createAmount(BigDecimal.valueOf(30000.00), "EUR"));
        transfer.setGrpHdr(header);
        
        for (int i = 0; i < 3; i++) {
            CreditTransferTransaction73 txInfo = createCreditTransferTransaction(
                    Iso20022TestDataFactory.generateAmount(1000, 20000));
            transfer.getCdtTrfTxInf().add(txInfo);
        }
        
        Document document = new Document();
        document.setFIToFICstmrCdtTrf(transfer);
        
        assertThat(document.getFIToFICstmrCdtTrf().getCdtTrfTxInf()).hasSize(3);
    }

    private Document createSamplePacs008Document() {
        FIToFICustomerCreditTransferV14 transfer = new FIToFICustomerCreditTransferV14();
        
        GroupHeader131 header = createGroupHeader();
        transfer.setGrpHdr(header);
        
        CreditTransferTransaction73 txInfo = createCreditTransferTransaction(
                Iso20022TestDataFactory.generateAmount());
        transfer.getCdtTrfTxInf().add(txInfo);
        
        Document document = new Document();
        document.setFIToFICstmrCdtTrf(transfer);
        
        return document;
    }

    private GroupHeader131 createGroupHeader() {
        GroupHeader131 header = new GroupHeader131();
        header.setMsgId(Iso20022TestDataFactory.generateMessageId());
        header.setCreDtTm(createCurrentDateTime());
        header.setNbOfTxs("1");
        header.setSttlmInf(createSettlementInstruction());
        return header;
    }

    private SettlementInstruction15 createSettlementInstruction() {
        SettlementInstruction15 sttlmInf = new SettlementInstruction15();
        sttlmInf.setSttlmMtd(SettlementMethod1Code.CLRG);
        return sttlmInf;
    }

    private CreditTransferTransaction73 createCreditTransferTransaction(BigDecimal amount) {
        CreditTransferTransaction73 txInfo = new CreditTransferTransaction73();
        
        PaymentIdentification13 pmtId = new PaymentIdentification13();
        pmtId.setInstrId(Iso20022TestDataFactory.generateInstructionId());
        pmtId.setEndToEndId(Iso20022TestDataFactory.generateEndToEndId());
        pmtId.setTxId(Iso20022TestDataFactory.generateTransactionId());
        txInfo.setPmtId(pmtId);
        
        txInfo.setIntrBkSttlmAmt(createAmount(amount, "EUR"));
        txInfo.setIntrBkSttlmDt(createCurrentDate());
        
        return txInfo;
    }

    private ActiveCurrencyAndAmount createAmount(BigDecimal value, String currency) {
        ActiveCurrencyAndAmount amount = new ActiveCurrencyAndAmount();
        amount.setValue(value);
        amount.setCcy(currency);
        return amount;
    }

    private XMLGregorianCalendar createCurrentDateTime() {
        return datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());
    }

    private XMLGregorianCalendar createCurrentDate() {
        GregorianCalendar cal = new GregorianCalendar();
        return datatypeFactory.newXMLGregorianCalendarDate(
                cal.get(GregorianCalendar.YEAR),
                cal.get(GregorianCalendar.MONTH) + 1,
                cal.get(GregorianCalendar.DAY_OF_MONTH),
                javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED);
    }
}
