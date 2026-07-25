package com.example.xsdparser.application.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Specialized factory for creating ISO 20022 compliant test data.
 * Provides methods to generate valid identifiers, dates, and amounts
 * that conform to ISO 20022 standards.
 */
public class Iso20022TestDataFactory {

    private static final Logger logger = LoggerFactory.getLogger(Iso20022TestDataFactory.class);

    private static final Random random = new Random();
    private static final AtomicLong sequence = new AtomicLong(System.currentTimeMillis());
    private static DatatypeFactory datatypeFactory;

    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            logger.error("Failed to initialize DatatypeFactory", e);
        }
    }

    private Iso20022TestDataFactory() {}

    /**
     * Generates a unique message ID conforming to ISO 20022 standards.
     * Format: YYYYMMDDHHMMSS + sequence + random suffix
     *
     * @return a unique message ID
     */
    public static String generateMessageId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String seq = String.format("%06d", sequence.incrementAndGet() % 1000000);
        String suffix = String.format("%04d", random.nextInt(10000));
        return timestamp + seq + suffix;
    }

    /**
     * Generates a unique instruction ID.
     *
     * @return a unique instruction ID
     */
    public static String generateInstructionId() {
        return "INSTR" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * Generates a unique end-to-end ID.
     *
     * @return a unique end-to-end ID
     */
    public static String generateEndToEndId() {
        return "E2E" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    /**
     * Generates a unique transaction ID.
     *
     * @return a unique transaction ID
     */
    public static String generateTransactionId() {
        return "TXN" + System.nanoTime() + String.format("%04d", random.nextInt(10000));
    }

    /**
     * Generates a valid IBAN for testing purposes.
     * Uses a standard format with checksum calculation.
     *
     * @param countryCode the 2-letter country code (e.g., "DE", "GB", "NL")
     * @return a valid test IBAN
     */
    public static String generateIban(String countryCode) {
        String bankCode = String.format("%08d", random.nextInt(100000000));
        String accountNumber = String.format("%010d", random.nextInt(Integer.MAX_VALUE));
        
        String bban = bankCode + accountNumber;
        String checkDigits = calculateIbanCheckDigits(countryCode, bban);
        
        return countryCode + checkDigits + bban;
    }

    /**
     * Generates a random valid IBAN.
     *
     * @return a valid test IBAN
     */
    public static String generateIban() {
        String[] countries = {"DE", "GB", "NL", "FR", "ES", "IT", "BE", "AT"};
        return generateIban(countries[random.nextInt(countries.length)]);
    }

    private static String calculateIbanCheckDigits(String countryCode, String bban) {
        String rearranged = bban + countryCode + "00";
        StringBuilder numericIban = new StringBuilder();
        
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numericIban.append(Character.toUpperCase(c) - 'A' + 10);
            } else {
                numericIban.append(c);
            }
        }
        
        java.math.BigInteger ibanNumber = new java.math.BigInteger(numericIban.toString());
        int checkDigits = 98 - ibanNumber.mod(java.math.BigInteger.valueOf(97)).intValue();
        
        return String.format("%02d", checkDigits);
    }

    /**
     * Generates a valid BIC (Bank Identifier Code).
     *
     * @return a valid test BIC
     */
    public static String generateBic() {
        String[] bankCodes = {"DEUT", "COBA", "BNPA", "HSBC", "RABO", "INGB"};
        String[] countries = {"DE", "GB", "NL", "FR", "ES", "IT"};
        String[] locations = {"FF", "2L", "AM", "PP", "MM", "XX"};
        
        String bankCode = bankCodes[random.nextInt(bankCodes.length)];
        String country = countries[random.nextInt(countries.length)];
        String location = locations[random.nextInt(locations.length)];
        String branch = random.nextBoolean() ? String.format("%03d", random.nextInt(1000)) : "";
        
        return bankCode + country + location + branch;
    }

    /**
     * Generates a valid LEI (Legal Entity Identifier).
     *
     * @return a valid test LEI (20 characters)
     */
    public static String generateLei() {
        StringBuilder lei = new StringBuilder();
        for (int i = 0; i < 18; i++) {
            if (random.nextBoolean()) {
                lei.append((char) ('A' + random.nextInt(26)));
            } else {
                lei.append(random.nextInt(10));
            }
        }
        lei.append(String.format("%02d", random.nextInt(100)));
        return lei.toString();
    }

    /**
     * Generates a monetary amount suitable for ISO 20022 messages.
     *
     * @param minAmount minimum amount
     * @param maxAmount maximum amount
     * @return a BigDecimal amount with 2 decimal places
     */
    public static BigDecimal generateAmount(double minAmount, double maxAmount) {
        double amount = minAmount + (maxAmount - minAmount) * random.nextDouble();
        return BigDecimal.valueOf(amount).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Generates a standard monetary amount (0.01 to 999999.99).
     *
     * @return a BigDecimal amount
     */
    public static BigDecimal generateAmount() {
        return generateAmount(0.01, 999999.99);
    }

    /**
     * Returns a random currency code from common currencies.
     *
     * @return a 3-letter ISO 4217 currency code
     */
    public static String generateCurrencyCode() {
        String[] currencies = {"EUR", "USD", "GBP", "CHF", "JPY", "SEK", "NOK", "DKK", "PLN", "CZK"};
        return currencies[random.nextInt(currencies.length)];
    }

    /**
     * Generates the current date/time as XMLGregorianCalendar.
     *
     * @return current date/time
     */
    public static XMLGregorianCalendar generateCreationDateTime() {
        if (datatypeFactory == null) {
            return null;
        }
        GregorianCalendar cal = new GregorianCalendar();
        return datatypeFactory.newXMLGregorianCalendar(cal);
    }

    /**
     * Generates a date in the past.
     *
     * @param daysAgo number of days in the past
     * @return XMLGregorianCalendar for the specified date
     */
    public static XMLGregorianCalendar generatePastDate(int daysAgo) {
        if (datatypeFactory == null) {
            return null;
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -daysAgo);
        return datatypeFactory.newXMLGregorianCalendar(cal);
    }

    /**
     * Generates a date in the future.
     *
     * @param daysAhead number of days in the future
     * @return XMLGregorianCalendar for the specified date
     */
    public static XMLGregorianCalendar generateFutureDate(int daysAhead) {
        if (datatypeFactory == null) {
            return null;
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(java.util.Calendar.DAY_OF_MONTH, daysAhead);
        return datatypeFactory.newXMLGregorianCalendar(cal);
    }

    /**
     * Generates a reference number in the format commonly used for payment references.
     *
     * @return a payment reference string
     */
    public static String generatePaymentReference() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%08d", sequence.incrementAndGet() % 100000000);
    }

    /**
     * Generates a clearing system member ID.
     *
     * @return a member ID
     */
    public static String generateClearingMemberId() {
        return String.format("%08d", random.nextInt(100000000));
    }

    /**
     * Returns a random status code for payment status reports.
     *
     * @return a status code (ACCP, ACSC, ACSP, ACTC, ACWC, PART, PDNG, RCVD, RJCT)
     */
    public static String generatePaymentStatusCode() {
        String[] statuses = {"ACCP", "ACSC", "ACSP", "ACTC", "ACWC", "PART", "PDNG", "RCVD", "RJCT"};
        return statuses[random.nextInt(statuses.length)];
    }

    /**
     * Returns a random reason code for payment rejection.
     *
     * @return a reason code
     */
    public static String generateRejectReasonCode() {
        String[] reasons = {"AC01", "AC04", "AC06", "AG01", "AG02", "AM01", "AM02", "AM03", 
                           "BE01", "BE04", "MD01", "MD02", "MS01", "MS02", "RC01", "TM01"};
        return reasons[random.nextInt(reasons.length)];
    }

    /**
     * Generates a random name for testing.
     *
     * @return a test name
     */
    public static String generateName() {
        String[] firstNames = {"John", "Jane", "Michael", "Sarah", "David", "Emma", "James", "Lisa"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia"};
        return firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
    }

    /**
     * Generates a company name for testing.
     *
     * @return a test company name
     */
    public static String generateCompanyName() {
        String[] prefixes = {"Global", "International", "United", "Premier", "First", "National"};
        String[] types = {"Trading", "Holdings", "Services", "Industries", "Solutions", "Corporation"};
        String[] suffixes = {"Ltd", "Inc", "GmbH", "BV", "SA", "AG"};
        
        return prefixes[random.nextInt(prefixes.length)] + " " +
               types[random.nextInt(types.length)] + " " +
               suffixes[random.nextInt(suffixes.length)];
    }

    /**
     * Generates a street address for testing.
     *
     * @return a test street address
     */
    public static String generateStreetAddress() {
        String[] streets = {"Main Street", "High Street", "Park Avenue", "Oak Lane", "Market Square"};
        int number = random.nextInt(500) + 1;
        return number + " " + streets[random.nextInt(streets.length)];
    }

    /**
     * Generates a postal code appropriate for common formats.
     *
     * @return a test postal code
     */
    public static String generatePostalCode() {
        return String.format("%05d", random.nextInt(100000));
    }

    /**
     * Returns a random city name.
     *
     * @return a city name
     */
    public static String generateCity() {
        String[] cities = {"London", "Frankfurt", "Amsterdam", "Paris", "Madrid", "Rome", 
                          "Brussels", "Vienna", "Stockholm", "Copenhagen"};
        return cities[random.nextInt(cities.length)];
    }

    /**
     * Returns a random country code.
     *
     * @return a 2-letter ISO 3166-1 alpha-2 country code
     */
    public static String generateCountryCode() {
        String[] countries = {"DE", "GB", "NL", "FR", "ES", "IT", "BE", "AT", "SE", "DK"};
        return countries[random.nextInt(countries.length)];
    }
}
