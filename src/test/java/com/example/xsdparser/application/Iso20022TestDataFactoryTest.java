package com.example.xsdparser.application;

import com.example.xsdparser.application.factory.Iso20022TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Iso20022TestDataFactory.
 * Demonstrates how to generate ISO 20022 compliant test data.
 */
@DisplayName("Iso20022TestDataFactory Tests")
class Iso20022TestDataFactoryTest {

    @Test
    @DisplayName("should generate unique message IDs")
    void shouldGenerateUniqueMessageIds() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            ids.add(Iso20022TestDataFactory.generateMessageId());
        }
        
        assertThat(ids).hasSize(100);
    }

    @Test
    @DisplayName("should generate valid instruction ID format")
    void shouldGenerateValidInstructionIdFormat() {
        String id = Iso20022TestDataFactory.generateInstructionId();
        
        assertThat(id)
                .startsWith("INSTR")
                .hasSize(21);
    }

    @Test
    @DisplayName("should generate valid end-to-end ID format")
    void shouldGenerateValidEndToEndIdFormat() {
        String id = Iso20022TestDataFactory.generateEndToEndId();
        
        assertThat(id)
                .startsWith("E2E")
                .hasSize(23);
    }

    @Test
    @DisplayName("should generate valid IBAN for given country")
    void shouldGenerateValidIbanForCountry() {
        String ibanDE = Iso20022TestDataFactory.generateIban("DE");
        String ibanGB = Iso20022TestDataFactory.generateIban("GB");
        String ibanNL = Iso20022TestDataFactory.generateIban("NL");
        
        assertThat(ibanDE).startsWith("DE").hasSize(22);
        assertThat(ibanGB).startsWith("GB").hasSize(22);
        assertThat(ibanNL).startsWith("NL").hasSize(22);
    }

    @Test
    @DisplayName("should generate random IBAN")
    void shouldGenerateRandomIban() {
        String iban = Iso20022TestDataFactory.generateIban();
        
        assertThat(iban)
                .hasSize(22)
                .matches("[A-Z]{2}[0-9]{20}");
    }

    @Test
    @DisplayName("should generate valid BIC format")
    void shouldGenerateValidBicFormat() {
        String bic = Iso20022TestDataFactory.generateBic();
        
        assertThat(bic)
                .hasSizeBetween(8, 11)
                .matches("[A-Z]{6}[A-Z0-9]{2,5}");
    }

    @Test
    @DisplayName("should generate valid LEI format")
    void shouldGenerateValidLeiFormat() {
        String lei = Iso20022TestDataFactory.generateLei();
        
        assertThat(lei)
                .hasSize(20)
                .matches("[A-Z0-9]{20}");
    }

    @Test
    @DisplayName("should generate amount within specified range")
    void shouldGenerateAmountWithinRange() {
        BigDecimal amount = Iso20022TestDataFactory.generateAmount(100, 500);
        
        assertThat(amount)
                .isGreaterThanOrEqualTo(BigDecimal.valueOf(100))
                .isLessThanOrEqualTo(BigDecimal.valueOf(500))
                .hasScaleOf(2);
    }

    @Test
    @DisplayName("should generate standard amount")
    void shouldGenerateStandardAmount() {
        BigDecimal amount = Iso20022TestDataFactory.generateAmount();
        
        assertThat(amount)
                .isPositive()
                .hasScaleOf(2);
    }

    @Test
    @DisplayName("should generate valid currency code")
    void shouldGenerateValidCurrencyCode() {
        String currency = Iso20022TestDataFactory.generateCurrencyCode();
        
        assertThat(currency)
                .hasSize(3)
                .matches("[A-Z]{3}")
                .isIn("EUR", "USD", "GBP", "CHF", "JPY", "SEK", "NOK", "DKK", "PLN", "CZK");
    }

    @Test
    @DisplayName("should generate creation date time")
    void shouldGenerateCreationDateTime() {
        XMLGregorianCalendar dateTime = Iso20022TestDataFactory.generateCreationDateTime();
        
        assertThat(dateTime).isNotNull();
    }

    @Test
    @DisplayName("should generate past date")
    void shouldGeneratePastDate() {
        XMLGregorianCalendar pastDate = Iso20022TestDataFactory.generatePastDate(30);
        XMLGregorianCalendar now = Iso20022TestDataFactory.generateCreationDateTime();
        
        assertThat(pastDate).isNotNull();
        assertThat(pastDate.compare(now)).isLessThan(0);
    }

    @Test
    @DisplayName("should generate future date")
    void shouldGenerateFutureDate() {
        XMLGregorianCalendar futureDate = Iso20022TestDataFactory.generateFutureDate(30);
        XMLGregorianCalendar now = Iso20022TestDataFactory.generateCreationDateTime();
        
        assertThat(futureDate).isNotNull();
        assertThat(futureDate.compare(now)).isGreaterThan(0);
    }

    @Test
    @DisplayName("should generate payment reference")
    void shouldGeneratePaymentReference() {
        String reference = Iso20022TestDataFactory.generatePaymentReference();
        
        assertThat(reference)
                .startsWith("PAY")
                .hasSize(19);
    }

    @Test
    @DisplayName("should generate valid payment status code")
    void shouldGenerateValidPaymentStatusCode() {
        String status = Iso20022TestDataFactory.generatePaymentStatusCode();
        
        assertThat(status)
                .hasSize(4)
                .isIn("ACCP", "ACSC", "ACSP", "ACTC", "ACWC", "PART", "PDNG", "RCVD", "RJCT");
    }

    @Test
    @DisplayName("should generate valid reject reason code")
    void shouldGenerateValidRejectReasonCode() {
        String reason = Iso20022TestDataFactory.generateRejectReasonCode();
        
        assertThat(reason)
                .hasSize(4)
                .matches("[A-Z]{2}[0-9]{2}");
    }

    @Test
    @DisplayName("should generate name")
    void shouldGenerateName() {
        String name = Iso20022TestDataFactory.generateName();
        
        assertThat(name)
                .isNotBlank()
                .contains(" ");
    }

    @Test
    @DisplayName("should generate company name")
    void shouldGenerateCompanyName() {
        String company = Iso20022TestDataFactory.generateCompanyName();
        
        assertThat(company)
                .isNotBlank()
                .contains(" ");
    }

    @Test
    @DisplayName("should generate street address")
    void shouldGenerateStreetAddress() {
        String address = Iso20022TestDataFactory.generateStreetAddress();
        
        assertThat(address)
                .isNotBlank()
                .containsPattern("\\d+ .*");
    }

    @Test
    @DisplayName("should generate postal code")
    void shouldGeneratePostalCode() {
        String postalCode = Iso20022TestDataFactory.generatePostalCode();
        
        assertThat(postalCode)
                .hasSize(5)
                .matches("\\d{5}");
    }

    @Test
    @DisplayName("should generate city")
    void shouldGenerateCity() {
        String city = Iso20022TestDataFactory.generateCity();
        
        assertThat(city)
                .isNotBlank()
                .isIn("London", "Frankfurt", "Amsterdam", "Paris", "Madrid", "Rome", 
                      "Brussels", "Vienna", "Stockholm", "Copenhagen");
    }

    @Test
    @DisplayName("should generate country code")
    void shouldGenerateCountryCode() {
        String country = Iso20022TestDataFactory.generateCountryCode();
        
        assertThat(country)
                .hasSize(2)
                .matches("[A-Z]{2}");
    }

    @Test
    @DisplayName("should generate clearing member ID")
    void shouldGenerateClearingMemberId() {
        String memberId = Iso20022TestDataFactory.generateClearingMemberId();
        
        assertThat(memberId)
                .hasSize(8)
                .matches("\\d{8}");
    }
}
