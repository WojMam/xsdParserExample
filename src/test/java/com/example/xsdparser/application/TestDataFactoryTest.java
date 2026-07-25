package com.example.xsdparser.application;

import com.example.xsdparser.application.factory.TestDataFactory;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the TestDataFactory.
 * Demonstrates how to use the factory to create test data dynamically.
 */
@DisplayName("TestDataFactory Tests")
class TestDataFactoryTest {

    private TestDataFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TestDataFactory();
    }

    @Test
    @DisplayName("should create simple object with populated fields")
    void shouldCreateSimpleObjectWithPopulatedFields() {
        SimpleTestObject result = factory.create(SimpleTestObject.class);
        
        assertThat(result).isNotNull();
        assertThat(result.getName()).isNotNull();
        assertThat(result.getAmount()).isNotNull();
    }

    @Test
    @DisplayName("should create object with fixed values instead of random")
    void shouldCreateObjectWithFixedValues() {
        SimpleTestObject result = factory.withFixedValues().create(SimpleTestObject.class);
        
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TestValue");
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("should create object with custom field value")
    void shouldCreateObjectWithCustomFieldValue() {
        SimpleTestObject result = factory
                .withFieldValue("name", "CustomName")
                .withFieldValue("amount", BigDecimal.valueOf(999.99))
                .create(SimpleTestObject.class);
        
        assertThat(result.getName()).isEqualTo("CustomName");
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(999.99));
    }

    @Test
    @DisplayName("should create nested objects up to max depth")
    void shouldCreateNestedObjectsUpToMaxDepth() {
        NestedTestObject result = factory.withMaxDepth(2).create(NestedTestObject.class);
        
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("should use custom type provider")
    void shouldUseCustomTypeProvider() {
        String customValue = "CUSTOM_GENERATED_VALUE";
        
        SimpleTestObject result = factory
                .withTypeProvider(String.class, () -> customValue)
                .create(SimpleTestObject.class);
        
        assertThat(result.getName()).isEqualTo(customValue);
    }

    @Test
    @DisplayName("should use builder pattern for creation")
    void shouldUseBuilderPatternForCreation() {
        SimpleTestObject result = TestDataFactory.builder(SimpleTestObject.class)
                .withFixedValues()
                .with("name", "BuilderTest")
                .maxDepth(3)
                .build();
        
        assertThat(result.getName()).isEqualTo("BuilderTest");
    }

    @Test
    @DisplayName("should handle enum fields")
    void shouldHandleEnumFields() {
        ObjectWithEnum result = factory.create(ObjectWithEnum.class);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isNotNull();
    }

    @Test
    @DisplayName("should populate optional fields when enabled")
    void shouldPopulateOptionalFieldsWhenEnabled() {
        SimpleTestObject result = factory.withOptionalFields().create(SimpleTestObject.class);
        
        assertThat(result).isNotNull();
    }

    @XmlRootElement
    public static class SimpleTestObject {
        private String name;
        private BigDecimal amount;
        private Integer count;

        public SimpleTestObject() {}

        @XmlElement
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @XmlElement
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        @XmlElement
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    @XmlRootElement
    public static class NestedTestObject {
        private String name;
        private SimpleTestObject child;

        public NestedTestObject() {}

        @XmlElement
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @XmlElement
        public SimpleTestObject getChild() { return child; }
        public void setChild(SimpleTestObject child) { this.child = child; }
    }

    @XmlRootElement
    public static class ObjectWithEnum {
        private String name;
        private Status status;

        public ObjectWithEnum() {}

        @XmlElement
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @XmlElement
        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }

        public enum Status {
            PENDING, ACTIVE, COMPLETED, FAILED
        }
    }
}
